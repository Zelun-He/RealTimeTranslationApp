package com.samanthamalca.translatorapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samanthamalca.translatorapp.services.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TranslationUiState(
    val isConnected: Boolean = false,
    val isRecording: Boolean = false,
    val sourceLanguage: String = "en",
    val targetLanguage: String = "es",
    val partialTranslation: String = "",
    val finalTranslation: String = "",
    val errorMessage: String? = null,
    val connectionState: String = "Disconnected"
)

class TranslationViewModel : ViewModel() {
    
    // WebSocket URL - REPLACE with your actual API Gateway WebSocket URL
    private val websocketUrl = "wss://your-api-gateway-id.execute-api.region.amazonaws.com/prod"
    
    private val webSocketClient = TranslationWebSocketClient(websocketUrl)
    private val audioManager = AudioStreamManager()
    
    private val _uiState = MutableStateFlow(TranslationUiState())
    val uiState: StateFlow<TranslationUiState> = _uiState.asStateFlow()
    
    init {
        // Observe connection state
        viewModelScope.launch {
            webSocketClient.connectionState.collect { state ->
                _uiState.update { current ->
                    current.copy(
                        isConnected = state is ConnectionState.Connected,
                        connectionState = when (state) {
                            is ConnectionState.Connected -> "Connected"
                            is ConnectionState.Connecting -> "Connecting..."
                            is ConnectionState.Disconnected -> "Disconnected"
                            is ConnectionState.Error -> "Error: ${state.message}"
                        },
                        errorMessage = if (state is ConnectionState.Error) state.message else null
                    )
                }
            }
        }
        
        // Observe translation results
        viewModelScope.launch {
            webSocketClient.translationResults.collect { result ->
                when (result) {
                    is TranslationResult.Partial -> {
                        _uiState.update { it.copy(partialTranslation = result.text) }
                    }
                    is TranslationResult.Final -> {
                        _uiState.update { 
                            it.copy(
                                finalTranslation = result.text,
                                partialTranslation = ""
                            )
                        }
                    }
                    is TranslationResult.TTSAudio -> {
                        // Play the audio
                        audioManager.playAudioChunk(result.audioData)
                    }
                    is TranslationResult.Error -> {
                        _uiState.update { it.copy(errorMessage = result.message) }
                    }
                    null -> {}
                }
            }
        }
    }
    
    fun connect() {
        webSocketClient.connect()
    }
    
    fun disconnect() {
        stopRecording()
        webSocketClient.disconnect()
    }
    
    fun startRecording() {
        if (_uiState.value.isConnected) {
            // Send start control message
            webSocketClient.startTranslation(
                sourceLanguage = _uiState.value.sourceLanguage,
                targetLanguage = _uiState.value.targetLanguage,
                enableTTS = true
            )
            
            // Start audio recording
            val started = audioManager.startRecording { audioFrame ->
                // Send audio frames to WebSocket
                webSocketClient.sendAudioFrame(audioFrame)
            }
            
            if (started) {
                _uiState.update { it.copy(isRecording = true) }
            } else {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to start recording. Check microphone permission.")
                }
            }
        } else {
            _uiState.update { 
                it.copy(errorMessage = "Not connected to server. Please wait...")
            }
        }
    }
    
    fun stopRecording() {
        if (_uiState.value.isRecording) {
            webSocketClient.stopTranslation()
            audioManager.stopRecording()
            _uiState.update { it.copy(isRecording = false) }
        }
    }
    
    fun setSourceLanguage(languageCode: String) {
        _uiState.update { it.copy(sourceLanguage = languageCode) }
    }
    
    fun setTargetLanguage(languageCode: String) {
        _uiState.update { it.copy(targetLanguage = languageCode) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioManager.cleanup()
        webSocketClient.cleanup()
    }
}


