package com.samanthamalca.translatorapp.services

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.*

class AudioStreamManager {
    // Recording configuration
    private val sampleRate = 16000 // 16kHz for speech
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    
    // Playback configuration
    private val playbackChannelConfig = AudioFormat.CHANNEL_OUT_MONO
    
    // Frame duration: 20-40ms as specified
    private val frameDurationMs = 30 // 30ms frames
    private val frameSizeBytes = (sampleRate * frameDurationMs / 1000) * 2 // 16-bit = 2 bytes per sample
    
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var isRecording = false
    private var isPlaying = false
    
    private val recordingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val playbackScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordingJob: Job? = null
    
    private val minBufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        channelConfig,
        audioFormat
    )

    fun startRecording(onAudioFrame: (ByteArray) -> Unit): Boolean {
        if (isRecording) return false
        if (minBufferSize <= 0) return false

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                channelConfig,
                audioFormat,
                minBufferSize * 2
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                return false
            }

            audioRecord?.startRecording()
            if (audioRecord?.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.release()
                audioRecord = null
                return false
            }
            isRecording = true

            recordingJob = recordingScope.launch {
                val buffer = ByteArray(frameSizeBytes)
                
                while (isActive && isRecording) {
                    val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                    
                    if (readBytes > 0) {
                        // Send audio frame to WebSocket
                        val frame = buffer.copyOf(readBytes)
                        onAudioFrame(frame)
                    }
                }
            }

            return true
        } catch (e: SecurityException) {
            println("AudioStreamManager: Missing RECORD_AUDIO permission")
            return false
        } catch (e: Exception) {
            println("AudioStreamManager: Failed to start recording: ${e.message}")
            return false
        }
    }

    fun stopRecording() {
        isRecording = false
        recordingJob?.cancel()
        
        audioRecord?.apply {
            runCatching { stop() }
            runCatching { release() }
        }
        audioRecord = null
    }

    fun initializePlayback(): Boolean {
        try {
            val minPlaybackBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                playbackChannelConfig,
                audioFormat
            )

            audioTrack = AudioTrack.Builder()
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(sampleRate)
                        .setChannelMask(playbackChannelConfig)
                        .setEncoding(audioFormat)
                        .build()
                )
                .setBufferSizeInBytes(minPlaybackBufferSize * 2)
                .build()

            audioTrack?.play()
            isPlaying = true
            return true
        } catch (e: Exception) {
            println("AudioStreamManager: Failed to initialize playback: ${e.message}")
            return false
        }
    }

    fun playAudioChunk(audioData: ByteArray) {
        if (!isPlaying) {
            val playbackReady = initializePlayback()
            if (!playbackReady) return
        }

        playbackScope.launch {
            audioTrack?.write(audioData, 0, audioData.size)
        }
    }

    fun stopPlayback() {
        isPlaying = false
        audioTrack?.apply {
            runCatching { stop() }
            runCatching { release() }
        }
        audioTrack = null
    }

    fun cleanup() {
        stopRecording()
        stopPlayback()
        recordingScope.cancel()
        playbackScope.cancel()
    }
}


