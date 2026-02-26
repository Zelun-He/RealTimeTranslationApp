package com.samanthamalca.translatorapp.components

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun CircularDesign(
    modifier: Modifier = Modifier,
    onSpeechResult: (String) -> Unit = {} // Callback for translation with default empty lambda
) {
    var isPressed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }
    
    // Animation states - Enhanced for better visual feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.15f else 1f,  // Slightly reduced scale for smoother effect
        animationSpec = tween(250), label = "scale"  // Faster animation
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 10f else 0f,  // Reduced rotation for subtler effect
        animationSpec = tween(250), label = "rotation"
    )

    // Initialize speech recognizer
    DisposableEffect(Unit) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let {
                    onSpeechResult(it)
                }
            }
            override fun onError(error: Int) { /* Handle errors */ }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        
        onDispose { speechRecognizer?.destroy() }
    }

    Canvas(
        modifier = modifier
            .size(280.dp)  // Increased from 200dp to 280dp
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { 
                        isPressed = true
                        try {
                            speechRecognizer?.startListening(
                                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                }
                            )
                        } catch (e: SecurityException) {
                            // Handle permission error
                        } finally {
                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                                speechRecognizer?.stopListening()
                            }
                        }
                    }
                )
            }
            .graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radiusStep = 28f  // Increased from 20f to 28f for better proportions
        
        val colorsLeft = listOf(
            Color(0xFF0F172A), // Darkest Blue (added for depth)
            Color(0xFF1E3A8A), // Dark Blue
            Color(0xFF3B82F6), // Blue
            Color(0xFF60A5FA), // Light Blue
            Color(0xFF93C5FD)  // Lighter Blue
        )
        
        val colorsRight = listOf(
            Color(0xFF581C87), // Darkest Purple (added for depth)
            Color(0xFF7C3AED), // Purple
            Color(0xFFA855F7), // Light Purple
            Color(0xFFC084FC), // Lighter Purple
            Color(0xFFE9D5FF)  // Lightest Purple
        )

        // Draw left arcs
        colorsLeft.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = 90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radiusStep * (colorsLeft.size - index), center.y - radiusStep * (colorsLeft.size - index)),
                size = Size(radiusStep * (colorsLeft.size - index) * 2, radiusStep * (colorsLeft.size - index) * 2)
            )
        }

        // Draw right arcs
        colorsRight.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - radiusStep * (colorsRight.size - index), center.y - radiusStep * (colorsRight.size - index)),
                size = Size(radiusStep * (colorsRight.size - index) * 2, radiusStep * (colorsRight.size - index) * 2)
            )
        }
    }
}