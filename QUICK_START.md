# Quick Start Guide - Real-Time Translation App

## 🎯 What You Have Now

A fully functional Android app with:
- ✅ WebSocket client for AWS API Gateway
- ✅ Real-time audio streaming (30ms frames)
- ✅ Translation display (partial + final)
- ✅ TTS audio playback
- ✅ Language selection (12 languages)
- ✅ Beautiful animated UI

## 🚀 To Make It Work:

### 1. Update WebSocket URL ⚡

**File**: `app/src/main/java/com/samanthamalca/translatorapp/viewmodel/TranslationViewModel.kt`

**Line 25**, replace:
```kotlin
private val websocketUrl = "wss://your-api-gateway-id.execute-api.region.amazonaws.com/prod"
```

With your actual AWS API Gateway WebSocket URL.

### 2. Deploy AWS Backend 🌐

You need to set up:

#### API Gateway WebSocket Routes:
- `$connect` → wsConnect Lambda
- `$default` → wsDefault Lambda  
- `$disconnect` → wsDisconnect Lambda

#### Lambda Functions (Kotlin/Java):
- **wsConnect**: Store connection in DynamoDB
- **wsDefault**: Handle audio frames & control messages
  - Receive binary audio → push to STT
  - Get partial results → translate (MT)
  - Generate TTS → stream back
- **wsDisconnect**: Cleanup session

#### DynamoDB Tables:
- `sessions`: Session state & buffers
- `connections`: ConnectionId mapping

#### Lambda Configuration:
- Memory: 1024-2048 MB
- Timeout: 900s
- Provisioned Concurrency: 1-3
- Environment: API keys for STT/MT/TTS

### 3. Build & Run 📱

```bash
# In Android Studio:
1. Sync Gradle (dependencies will download)
2. Build > Clean Project
3. Build > Rebuild Project
4. Run on device/emulator
```

### 4. Grant Permissions 🎤

On first launch, the app will request:
- **Microphone permission** (for audio recording)

## 📱 How to Use

1. **Launch app** → Connects to WebSocket automatically
2. **Wait for green "Connected"** status
3. **Tap hamburger menu** (top left) → Select target language
4. **Tap/hold circular button** → Start speaking
5. **See real-time translation** appear on screen
6. **Hear TTS audio** of translation
7. **Release button** → Stop recording

## 🎨 UI Elements

- **Top Left**: Language dropdown menu
- **Top Center**: Connection status (Green/Yellow/Red)
- **Upper Screen**: Translation text display
  - Blue = Partial (real-time)
  - White = Final (complete)
- **Center**: Large circular recording button
- **Bottom**: "● Recording..." indicator

## 📊 What Happens Behind the Scenes

```
[You speak] 
    ↓ (microphone)
[30ms audio frames] 
    ↓ (WebSocket binary)
[Lambda receives] 
    ↓ (STT streaming)
[Speech-to-text] 
    ↓ (debounce 100-150ms)
[Machine translation] 
    ↓ (MT API)
[Translated text] 
    ↓ (TTS synthesis)
[Audio playback]
    ↓ (speaker)
[You hear translation]
```

All in **~600ms-1.5s** end-to-end!

## 🔧 Configuration Options

### Change Audio Settings
**File**: `AudioStreamManager.kt`
- `sampleRate`: Default 16000 Hz
- `frameDurationMs`: Default 30ms
- Adjust for quality vs. latency

### Change Debounce Window
**Lambda backend**: Adjust translation trigger timing
- Lower (50-100ms): Faster, more updates
- Higher (150-200ms): Slower, fewer API calls

### Enable/Disable TTS
**File**: `TranslationViewModel.kt` line 90:
```kotlin
webSocketClient.startTranslation(
    sourceLanguage = _uiState.value.sourceLanguage,
    targetLanguage = _uiState.value.targetLanguage,
    enableTTS = true  // Change to false to disable audio
)
```

## 🐛 Common Issues

### App shows "Connecting..." forever
→ Check WebSocket URL is correct
→ Verify API Gateway is deployed
→ Check internet connection

### "Failed to start recording"
→ Grant microphone permission in Settings
→ Check device microphone works

### No translation appears
→ Verify Lambda is running (CloudWatch)
→ Check STT/MT API keys
→ Monitor Lambda logs

### Translations delayed
→ Increase Lambda memory
→ Enable Provisioned Concurrency
→ Check debounce settings

## 📚 File Structure

```
app/src/main/java/com/samanthamalca/translatorapp/
├── MainActivity.kt                      # Main UI
├── components/
│   ├── CircularDesign.kt               # Recording button
│   ├── PulsatingWaves.kt               # Button animation
│   ├── ShazamScreenEffect.kt           # Background effect
│   └── LanguageDropdownMenu.kt         # Language selector
├── services/
│   ├── TranslationWebSocketClient.kt   # WebSocket handler
│   └── AudioStreamManager.kt           # Audio recording/playback
└── viewmodel/
    └── TranslationViewModel.kt         # State management
```

## 🎯 Next Steps

1. ✅ Update WebSocket URL
2. ✅ Deploy AWS backend (see WEBSOCKET_INTEGRATION.md)
3. ✅ Build and install app
4. ✅ Test with real device
5. ✅ Monitor CloudWatch for issues
6. ✅ Optimize latency settings
7. ✅ Add more languages if needed

## 💡 Pro Tips

- **Test locally first**: Use ngrok to tunnel to local Lambda
- **Monitor costs**: STT/MT/TTS APIs charge per request
- **Cache translations**: Store common phrases in DynamoDB
- **Add analytics**: Track usage patterns
- **Implement feedback**: Let users rate translations

---

**You're ready to go!** 🚀🌍🎤

Just update that WebSocket URL and deploy your backend!


