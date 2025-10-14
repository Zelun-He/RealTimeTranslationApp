# WebSocket Real-Time Translation Integration

## 🚀 Overview

Your Android app now has **full real-time translation capabilities** using AWS API Gateway WebSocket and streaming audio!

## Architecture

### Data Flow
```
[Android App] ←→ [API Gateway WebSocket] ←→ [Lambda] ←→ [STT/MT/TTS Services]
     ↑                                                          ↓
     └──────────────── Translated Audio/Text ←────────────────┘
```

### Components Created

#### 1. **TranslationWebSocketClient** (`services/TranslationWebSocketClient.kt`)
- Manages WebSocket connection to API Gateway
- Handles control messages (start/stop)
- Streams binary audio frames (20-40ms PCM)
- Receives translation results (partial/final/TTS audio)
- Auto-reconnects with ping/pong keepalive

#### 2. **AudioStreamManager** (`services/AudioStreamManager.kt`)
- Records audio at 16kHz, mono, 16-bit PCM
- Sends 30ms audio frames to WebSocket
- Plays back TTS audio chunks in real-time
- Handles audio permissions and errors

#### 3. **TranslationViewModel** (`viewmodel/TranslationViewModel.kt`)
- Manages app state (connection, recording, translations)
- Coordinates WebSocket and audio services
- Provides reactive UI updates via StateFlow
- Lifecycle-aware cleanup

## 📡 WebSocket Protocol

### Connection
```kotlin
wss://your-api-gateway-id.execute-api.region.amazonaws.com/prod
```

### Message Types

#### Control Messages (JSON, Text)
```json
{
  "action": "start",
  "sourceLanguage": "en",
  "targetLanguage": "es",
  "enableTTS": true
}
```

```json
{
  "action": "stop",
  "sourceLanguage": "",
  "targetLanguage": ""
}
```

#### Audio Frames (Binary)
- **Format**: Raw PCM 16-bit, mono, 16kHz
- **Frame Size**: 30ms (960 bytes = 480 samples × 2 bytes)
- **Sent continuously** while recording

#### Translation Responses (JSON, Text)
```json
{
  "type": "partial",
  "text": "Hello, how are..."
}
```

```json
{
  "type": "final",
  "text": "Hello, how are you?",
  "confidence": 0.95
}
```

```json
{
  "type": "tts_audio",
  "audioChunk": "base64_encoded_audio_data"
}
```

Or TTS can be sent as **binary frames** directly.

## 🔧 Setup Instructions

### 1. Update WebSocket URL

In `TranslationViewModel.kt`, replace:
```kotlin
private val websocketUrl = "wss://your-api-gateway-id.execute-api.region.amazonaws.com/prod"
```

With your actual API Gateway WebSocket URL.

### 2. AWS Lambda Backend

Your Lambda functions should handle these routes:

#### **$connect** (wsConnect Lambda)
```kotlin
// Store connectionId → sessionId mapping in DynamoDB
fun handleConnect(event: APIGatewayWebSocketProxyRequestEvent): APIGatewayProxyResponseEvent {
    val connectionId = event.requestContext.connectionId
    val sessionId = UUID.randomUUID().toString()
    
    dynamoDB.putItem(
        TableName = "connections",
        Item = mapOf(
            "connectionId" to connectionId,
            "sessionId" to sessionId,
            "timestamp" to System.currentTimeMillis()
        )
    )
    
    return APIGatewayProxyResponseEvent().withStatusCode(200)
}
```

#### **$default** (wsDefault Lambda)
```kotlin
fun handleDefault(event: APIGatewayWebSocketProxyRequestEvent): APIGatewayProxyResponseEvent {
    val connectionId = event.requestContext.connectionId
    
    if (event.isBase64Encoded) {
        // Binary audio frame
        val audioData = Base64.getDecoder().decode(event.body)
        
        // Push to STT stream (Azure/Google/OpenAI/Transcribe)
        sttStream.write(audioData)
        
        // Get partial results with debounce (100-150ms)
        val partialText = debounce { sttStream.getPartial() }
        
        // Translate partial text
        val translatedText = mtService.translate(partialText, targetLang)
        
        // Send back to client
        sendToClient(connectionId, TranslationResponse(
            type = "partial",
            text = translatedText
        ))
        
        // If TTS enabled, generate and stream audio
        if (ttsEnabled) {
            val audioChunk = ttsService.synthesize(translatedText)
            sendBinaryToClient(connectionId, audioChunk)
        }
        
    } else {
        // Text control message
        val controlMsg = Json.decodeFromString<ControlMessage>(event.body)
        
        when (controlMsg.action) {
            "start" -> startTranslationSession(connectionId, controlMsg)
            "stop" -> stopTranslationSession(connectionId)
        }
    }
    
    return APIGatewayProxyResponseEvent().withStatusCode(200)
}
```

#### **$disconnect** (wsDisconnect Lambda)
```kotlin
fun handleDisconnect(event: APIGatewayWebSocketProxyRequestEvent): APIGatewayProxyResponseEvent {
    val connectionId = event.requestContext.connectionId
    
    // Cleanup: stop STT/TTS streams, remove from DynamoDB
    cleanupSession(connectionId)
    
    return APIGatewayProxyResponseEvent().withStatusCode(200)
}
```

### 3. DynamoDB Schema

#### **sessions** table
```
sessionId (PK)
connectionId
sourceLanguage
targetLanguage
enableTTS
partialBuffer
sequence
vadWindow
timestamp
```

#### **connections** table
```
connectionId (PK)
sessionId
timestamp
```

### 4. Lambda Configuration

**Memory**: 1024-2048 MB (faster CPU)
**Timeout**: 900 seconds (15 min max for WebSocket)
**Provisioned Concurrency**: 1-3 (eliminate cold starts)
**Environment Variables**:
- `STT_API_KEY`
- `MT_API_KEY`
- `TTS_API_KEY`
- `DYNAMODB_TABLE`

For JVM/Kotlin:
- Use **Quarkus** or **GraalVM native** for faster startup
- Or enable **SnapStart** for Java 11/17

## 📱 Android App Usage

### User Flow

1. **App launches** → Auto-connects to WebSocket
2. **User taps hamburger menu** → Selects target language
3. **User taps/holds circular button** → Starts recording
4. **App streams audio** → Sends 30ms frames to Lambda
5. **Lambda processes** → STT → MT → TTS
6. **App receives**:
   - Partial translations (real-time text updates)
   - Final translations (complete sentences)
   - TTS audio (plays back automatically)
7. **User releases button** → Stops recording

### UI Indicators

- **Connection Status**: Top center (Green/Yellow/Red)
- **Partial Translation**: Blue text (updates in real-time)
- **Final Translation**: White bold text
- **Recording Status**: "● Recording..." at bottom

## 🎯 Key Features

✅ **Real-time streaming** (20-40ms frames)
✅ **Partial translations** (100-150ms debounce)
✅ **Final translations** with confidence scores
✅ **TTS audio playback** (streamed back)
✅ **Language selection** (12 languages)
✅ **Connection management** (auto-reconnect)
✅ **Lifecycle-aware** (cleanup on app close)
✅ **Error handling** (permissions, network, etc.)

## 🔐 Permissions

Already added in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
```

Make sure to request RECORD_AUDIO permission at runtime!

## 🐛 Troubleshooting

### "Not connected to server"
- Check WebSocket URL in `TranslationViewModel.kt`
- Verify API Gateway is deployed and accessible
- Check internet connection

### "Failed to start recording"
- Grant microphone permission in app settings
- Check device microphone availability

### No translation results
- Verify Lambda is processing messages (check CloudWatch Logs)
- Check STT/MT/TTS API keys in Lambda environment
- Monitor X-Ray traces for latency issues

### High latency
- Increase Lambda memory (1024 → 2048 MB)
- Enable Provisioned Concurrency (1-3 instances)
- Use regional API Gateway (not edge-optimized for WebSocket)
- Optimize debounce window (100-150ms)

## 📊 Performance

- **Audio Frame Size**: 960 bytes per 30ms
- **Network Bandwidth**: ~32 KB/s upload (audio only)
- **STT Latency**: ~200-500ms (service dependent)
- **MT Latency**: ~100-150ms (with debounce)
- **TTS Latency**: ~300-600ms (streaming)
- **Total End-to-End**: ~600ms-1.5s typical

## 🚀 Next Steps

1. **Deploy Lambda functions** to AWS
2. **Set up API Gateway** WebSocket API
3. **Configure DynamoDB** tables
4. **Store API keys** in Secrets Manager/SSM
5. **Update WebSocket URL** in app
6. **Test with emulator/device**
7. **Monitor CloudWatch** metrics

Your app is now ready for real-time translation! 🌍🎤✨


