package com.samanthamalca.translatorapp.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

@Serializable
data class ControlMessage(
    val action: String, // "start" or "stop"
    val sourceLanguage: String,
    val targetLanguage: String,
    val enableTTS: Boolean = true
)

@Serializable
data class TranslationResponse(
    val type: String, // "partial", "final", "tts_audio"
    val text: String? = null,
    val confidence: Float? = null,
    val audioChunk: String? = null // Base64 encoded
)

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

sealed class TranslationResult {
    data class Partial(val text: String) : TranslationResult()
    data class Final(val text: String, val confidence: Float) : TranslationResult()
    data class TTSAudio(val audioData: ByteArray) : TranslationResult()
    data class Error(val message: String) : TranslationResult()
}

class TranslationWebSocketClient(
    private val websocketUrl: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // No timeout for streaming
        .build()

    private val json = Json { ignoreUnknownKeys = true }
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _translationResults = MutableStateFlow<TranslationResult?>(null)
    val translationResults: StateFlow<TranslationResult?> = _translationResults

    fun connect() {
        if (
            _connectionState.value is ConnectionState.Connected ||
            _connectionState.value is ConnectionState.Connecting
        ) {
            return
        }

        _translationResults.value = null
        _connectionState.value = ConnectionState.Connecting

        val request = Request.Builder()
            .url(websocketUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
                println("WebSocket connected: ${response.message}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleTextMessage(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                handleBinaryMessage(bytes.toByteArray())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                this@TranslationWebSocketClient.webSocket = null
                _connectionState.value = ConnectionState.Disconnected
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                this@TranslationWebSocketClient.webSocket = null
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                this@TranslationWebSocketClient.webSocket = null
                _connectionState.value = ConnectionState.Error(t.message ?: "Unknown error")
                println("WebSocket error: ${t.message}")
            }
        })
    }

    private fun handleTextMessage(text: String) {
        try {
            val response = json.decodeFromString<TranslationResponse>(text)
            when (response.type) {
                "partial" -> {
                    response.text?.let {
                        _translationResults.value = TranslationResult.Partial(it)
                    }
                }
                "final" -> {
                    response.text?.let {
                        _translationResults.value = TranslationResult.Final(
                            it,
                            response.confidence ?: 1.0f
                        )
                    }
                }
                "tts_audio" -> {
                    response.audioChunk?.let { base64Audio ->
                        val audioBytes = android.util.Base64.decode(
                            base64Audio,
                            android.util.Base64.DEFAULT
                        )
                        _translationResults.value = TranslationResult.TTSAudio(audioBytes)
                    }
                }
            }
        } catch (e: Exception) {
            _translationResults.value = TranslationResult.Error("Failed to parse response: ${e.message}")
        }
    }

    private fun handleBinaryMessage(bytes: ByteArray) {
        // Handle TTS audio chunks if sent as binary
        _translationResults.value = TranslationResult.TTSAudio(bytes)
    }

    fun startTranslation(sourceLanguage: String, targetLanguage: String, enableTTS: Boolean = true) {
        val controlMessage = ControlMessage(
            action = "start",
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            enableTTS = enableTTS
        )
        sendTextMessage(json.encodeToString(controlMessage))
    }

    fun stopTranslation() {
        val controlMessage = ControlMessage(
            action = "stop",
            sourceLanguage = "",
            targetLanguage = ""
        )
        sendTextMessage(json.encodeToString(controlMessage))
    }

    fun sendAudioFrame(audioData: ByteArray) {
        if (_connectionState.value !is ConnectionState.Connected) return
        webSocket?.send(audioData.toByteString())
    }

    private fun sendTextMessage(message: String) {
        if (_connectionState.value !is ConnectionState.Connected) {
            _translationResults.value = TranslationResult.Error("Cannot send message while disconnected.")
            return
        }
        webSocket?.send(message)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closing connection")
        webSocket = null
        _translationResults.value = null
        _connectionState.value = ConnectionState.Disconnected
    }

    fun cleanup() {
        disconnect()
        client.dispatcher.executorService.shutdown()
    }
}


