package com.samanthamalca.translatorapp

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<TranslationViewModel>()

    private var startListeningAfterPermission = false

    private val micPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.setMicPermissionGranted()
                if (startListeningAfterPermission) {
                    viewModel.setPressing(true)
                }
            } else {
                viewModel.onPermissionDenied()
            }
            startListeningAfterPermission = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                TranslatorRealtimeScreen(
                    uiState = viewModel.uiState,
                    onPressStart = { requestStartListening() },
                    onPressEnd = { viewModel.setPressing(false) },
                    onSelectSourceLanguage = { viewModel.setSourceLanguage(it) },
                    onSelectTargetLanguage = { viewModel.setTargetLanguage(it) }
                )
            }
        }
    }

    private fun requestStartListening() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.setMicPermissionGranted()
            viewModel.setPressing(true)
        } else {
            startListeningAfterPermission = true
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

data class LanguageOption(val code: String, val label: String)

data class TranslationUiState(
    val isListening: Boolean = false,
    val isTranslating: Boolean = false,
    val sourceText: String = "",
    val translatedText: String = "",
    val sourceLanguage: LanguageOption = TranslationViewModel.supportedLanguages.first(),
    val targetLanguage: LanguageOption =
        TranslationViewModel.supportedLanguages.firstOrNull { it.code == "es" }
            ?: TranslationViewModel.supportedLanguages.first(),
    val errorMessage: String? = null
)

class TranslationViewModel(application: Application) : AndroidViewModel(application) {
    private val logTag = "TranslatorApp"

    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var isPressing = false
    private var isRecognizerActive = false
    private var micPermissionGranted = false
    private var translationJob: Job? = null
    private var lastStartListeningAtMs = 0L

    private var currentTranslator: Translator? = null
    private var currentTranslatorSourceCode: String? = null
    private var currentTranslatorTargetCode: String? = null

    init {
        initializeSpeechRecognizer()
    }

    fun setMicPermissionGranted() {
        micPermissionGranted = true
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onPermissionDenied() {
        _uiState.update {
            it.copy(errorMessage = "Microphone permission is required for hold-to-translate.")
        }
    }

    fun setTargetLanguage(language: LanguageOption) {
        _uiState.update {
            it.copy(targetLanguage = language, errorMessage = null)
        }

        val sourceText = _uiState.value.sourceText
        if (sourceText.isNotBlank()) {
            requestTranslation(sourceText)
        }
    }
    
    fun setSourceLanguage(language: LanguageOption) {
        _uiState.update {
            it.copy(sourceLanguage = language, errorMessage = null)
        }

        val sourceText = _uiState.value.sourceText
        if (sourceText.isNotBlank()) {
            requestTranslation(sourceText)
        }
    }

    fun setPressing(pressing: Boolean) {
        isPressing = pressing
        if (!micPermissionGranted) {
            _uiState.update {
                it.copy(isListening = false, errorMessage = "Grant microphone permission to continue.")
            }
            return
        }

        if (pressing) {
            startListeningIfNeeded()
        } else {
            stopListening()
        }
    }

    private fun initializeSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(getApplication())) {
            _uiState.update {
                it.copy(errorMessage = "Speech recognition is not available on this device.")
            }
            return
        }

        if (speechRecognizer != null) return

        val appContext = getApplication<Application>()
        val preferredService = ComponentName(
            "com.google.android.googlequicksearchbox",
            "com.google.android.voicesearch.serviceapi.GoogleRecognitionService"
        )

        val recognizer = runCatching {
            SpeechRecognizer.createSpeechRecognizer(appContext, preferredService)
        }.getOrElse {
            SpeechRecognizer.createSpeechRecognizer(appContext)
        }

        speechRecognizer = recognizer.apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) = Unit

                override fun onBeginningOfSpeech() = Unit

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    isRecognizerActive = false
                    _uiState.update { it.copy(isListening = false) }
                }

                override fun onError(error: Int) {
                    isRecognizerActive = false
                    _uiState.update { it.copy(isListening = false) }
                    Log.w(logTag, "SpeechRecognizer error=$error")

                    val errorMessage = speechErrorMessage(error)
                    if (errorMessage != null) {
                        _uiState.update { it.copy(errorMessage = errorMessage) }
                    }

                }

                override fun onResults(results: Bundle?) {
                    val spokenText = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()

                    if (spokenText.isNotBlank()) {
                        Log.d(logTag, "Final speech result: $spokenText")
                        _uiState.update { it.copy(sourceText = spokenText, errorMessage = null) }
                        requestTranslation(spokenText)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialText = partialResults
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                        .orEmpty()

                    if (partialText.isNotBlank()) {
                        Log.d(logTag, "Partial speech result: $partialText")
                        _uiState.update { it.copy(sourceText = partialText, errorMessage = null) }
                        requestTranslation(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    private fun startListeningIfNeeded() {
        if (isRecognizerActive) return
        if (speechRecognizer == null) {
            initializeSpeechRecognizer()
        }
        val now = android.os.SystemClock.elapsedRealtime()
        val elapsedSinceLastStart = now - lastStartListeningAtMs
        if (elapsedSinceLastStart < MIN_START_INTERVAL_MS) {
            _uiState.update {
                it.copy(errorMessage = "Please wait a second, then hold and speak again.")
            }
            return
        }

        val inputLanguageTag = Locale.forLanguageTag(_uiState.value.sourceLanguage.code).toLanguageTag()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, inputLanguageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, inputLanguageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplication<Application>().packageName)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        runCatching {
            speechRecognizer?.startListening(intent)
            isRecognizerActive = true
            lastStartListeningAtMs = now
            Log.d(logTag, "Started speech listening for input=${_uiState.value.sourceLanguage.code}")
            _uiState.update { it.copy(isListening = true, errorMessage = null) }
        }.onFailure { throwable ->
            Log.e(logTag, "startListening failed", throwable)
            _uiState.update {
                it.copy(
                    isListening = false,
                    errorMessage = "Could not start listening: ${throwable.message ?: "unknown error"}"
                )
            }
        }
    }

    private fun stopListening() {
        if (isRecognizerActive) {
            speechRecognizer?.stopListening()
        }
        isRecognizerActive = false
        _uiState.update { it.copy(isListening = false) }
    }

    private fun requestTranslation(text: String) {
        val sourceLanguageTag = _uiState.value.sourceLanguage.code
        val targetLanguageCode = _uiState.value.targetLanguage.code
        val sourceLanguageCode = TranslateLanguage.fromLanguageTag(sourceLanguageTag)
            ?: TranslateLanguage.ENGLISH
        val mlKitTargetLanguage = TranslateLanguage.fromLanguageTag(targetLanguageCode)

        if (mlKitTargetLanguage == null) {
            _uiState.update { it.copy(errorMessage = "Unsupported target language: $targetLanguageCode") }
            return
        }

        if (sourceLanguageCode == mlKitTargetLanguage) {
            _uiState.update { it.copy(translatedText = text, isTranslating = false) }
            return
        }

        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true, errorMessage = null) }

            try {
                val translator = getOrCreateTranslator(sourceLanguageCode, mlKitTargetLanguage)
                translator.downloadModelIfNeeded().awaitResult()
                val translated = translator.translate(text).awaitResult()
                Log.d(logTag, "Translated '$text' -> '$translated'")

                _uiState.update {
                    it.copy(translatedText = translated, isTranslating = false)
                }
            } catch (exception: Exception) {
                Log.e(logTag, "Translation failed", exception)
                _uiState.update {
                    it.copy(
                        isTranslating = false,
                        errorMessage = "Translation failed: ${exception.message ?: "unknown error"}"
                    )
                }
            }
        }
    }

    private fun getOrCreateTranslator(sourceCode: String, targetCode: String): Translator {
        val existingTranslator = currentTranslator
        if (
            existingTranslator != null &&
            currentTranslatorSourceCode == sourceCode &&
            currentTranslatorTargetCode == targetCode
        ) {
            return existingTranslator
        }

        currentTranslator?.close()

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()

        return Translation.getClient(options).also {
            currentTranslator = it
            currentTranslatorSourceCode = sourceCode
            currentTranslatorTargetCode = targetCode
        }
    }

    private fun speechErrorMessage(error: Int): String? {
        return when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> null
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Keep holding and speak clearly."
            SpeechRecognizer.ERROR_AUDIO -> "Microphone audio error."
            SpeechRecognizer.ERROR_CLIENT -> "Speech client error. Try again."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is missing."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Speech recognition needs network access."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Release and hold again."
            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS ->
                "Too many recognition requests. Release, wait 2 seconds, then hold again."
            SpeechRecognizer.ERROR_SERVER -> "Speech server error. Try again."
            else -> "Speech recognition error code: $error"
        }
    }

    override fun onCleared() {
        stopListening()
        translationJob?.cancel()
        currentTranslator?.close()
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onCleared()
    }

    companion object {
        private const val MIN_START_INTERVAL_MS = 1500L
        val supportedLanguages = listOf(
            LanguageOption("en", "English"),
            LanguageOption("es", "Spanish"),
            LanguageOption("fr", "French"),
            LanguageOption("de", "German"),
            LanguageOption("it", "Italian"),
            LanguageOption("pt", "Portuguese"),
            LanguageOption("ja", "Japanese"),
            LanguageOption("ko", "Korean"),
            LanguageOption("zh", "Chinese"),
            LanguageOption("ar", "Arabic"),
            LanguageOption("hi", "Hindi"),
            LanguageOption("ru", "Russian")
        )
    }
}

@Composable
private fun TranslatorRealtimeScreen(
    uiState: StateFlow<TranslationUiState>,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    onSelectSourceLanguage: (LanguageOption) -> Unit,
    onSelectTargetLanguage: (LanguageOption) -> Unit
) {
    val state by uiState.collectAsState(initial = TranslationUiState())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF05070D))
    ) {
        TopLeftLanguageDropdown(
            selectedSourceLanguage = state.sourceLanguage,
            selectedTargetLanguage = state.targetLanguage,
            options = TranslationViewModel.supportedLanguages,
            onSelectSourceLanguage = onSelectSourceLanguage,
            onSelectTargetLanguage = onSelectTargetLanguage
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                HoldToTranslateButton(
                    isListening = state.isListening,
                    onPressStart = onPressStart,
                    onPressEnd = onPressEnd
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Press and hold to translate. Release to stop.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                ClearTranslationBubble(
                    sourceText = state.sourceText,
                    translatedText = state.translatedText,
                    isListening = state.isListening,
                    isTranslating = state.isTranslating,
                    errorMessage = state.errorMessage
                )
            }
        }
    }
}

@Composable
private fun ClearTranslationBubble(
    sourceText: String,
    translatedText: String,
    isListening: Boolean,
    isTranslating: Boolean,
    errorMessage: String?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.35f),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = if (isListening) "Listening..." else "Ready",
                color = if (isListening) Color(0xFF8CC8FF) else Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sourceText.isBlank()) "Spoken text will appear here." else sourceText,
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (translatedText.isBlank()) "Translation will appear here." else translatedText,
                color = Color(0xFF9CC8FF),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            if (isTranslating) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Translating...",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 13.sp
                )
            }
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF7B7B),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun HoldToTranslateButton(
    isListening: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse-scale"
    )

    val buttonScale = if (isListening) animatedScale else 1f

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(170.dp)
            .scale(buttonScale)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A74FF).copy(alpha = 0.35f),
                        Color.Transparent
                    )
                ),
                shape = CircleShape
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3F7DFF), Color(0xFF835BFF))
                    ),
                    shape = CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                .pointerInteropFilter { motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            onPressStart()
                            true
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            onPressEnd()
                            true
                        }

                        else -> true
                    }
                }
        ) {
            Text(
                text = if (isListening) "LIVE" else "HOLD",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
private fun TopLeftLanguageDropdown(
    selectedSourceLanguage: LanguageOption,
    selectedTargetLanguage: LanguageOption,
    options: List<LanguageOption>,
    onSelectSourceLanguage: (LanguageOption) -> Unit,
    onSelectTargetLanguage: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .padding(14.dp)
    ) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1E2B40),
                contentColor = Color.White
            )
        ) {
            Text("Languages", fontSize = 13.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Input (${selectedSourceLanguage.label})") },
                onClick = {},
                enabled = false
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelectSourceLanguage(option)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Output (${selectedTargetLanguage.label})") },
                onClick = {},
                enabled = false
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelectTargetLanguage(option)
                    }
                )
            }
        }
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }
}
