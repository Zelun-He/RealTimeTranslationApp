# TranslatorApp
Real Time Translation Mobile App 🌍🎤✨

## Project Overview
A production-ready Android application with **real-time speech-to-speech translation** using AWS WebSocket streaming, Jetpack Compose, and modern animation effects.

### 🚀 Key Features
- **Real-time audio streaming** via WebSocket (30ms frames)
- **Live translation display** (partial + final results)
- **Text-to-Speech playback** of translations
- **12 language support** with visual selector
- **Beautiful animated UI** with Shazam-like effects
- **Low-latency architecture** (~650ms-1.5s end-to-end)

## Features
- **Shazam-like Screen Effect**: Pulsating waves and ripple animations
- **Speech Recognition**: Tap and hold the circular button to record audio for translation
- **Material 3 Design**: Modern UI with dark theme support
- **Custom Animations**: Interactive components with smooth transitions

## Tech Stack
- **Kotlin**: Primary programming language
- **Jetpack Compose**: Modern Android UI toolkit
- **Material 3**: Latest Material Design components
- **Android Gradle Plugin 8.5.0**
- **Gradle 8.10**
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/samanthamalca/translatorapp/
│   │   │   ├── MainActivity.kt           # Main entry point
│   │   │   ├── components/               # Reusable UI components
│   │   │   │   ├── CircularDesign.kt     # Main interactive button with speech recognition
│   │   │   │   ├── PulsatingWaves.kt     # Animated waves effect
│   │   │   │   ├── ShazamScreenEffect.kt # Background ripple animation
│   │   │   │   ├── CustomTopMenu.kt      # Top menu button
│   │   │   │   └── CurvedLines.kt        # Decorative curved lines
│   │   │   └── ui/theme/                 # Theme configuration
│   │   │       ├── Color.kt              # Color definitions
│   │   │       ├── Theme.kt              # Material 3 theme
│   │   │       └── Type.kt               # Typography styles
│   │   ├── AndroidManifest.xml
│   │   └── res/                          # Resources (drawables, values, etc.)
│   ├── androidTest/                      # Instrumented tests
│   └── test/                             # Unit tests
└── build.gradle.kts                      # App module build configuration
```

## Setup and Installation

### Prerequisites
- **Android Studio Hedgehog (2023.1.1) or later**
- **JDK 11 or later** (Note: AGP 8.5+ requires Java 17, but this project is configured for Java 11)
- **Android SDK 35** installed via Android Studio SDK Manager

### Quick Setup

⚡ **See [QUICK_START.md](QUICK_START.md) for detailed setup instructions!**

1. **Update WebSocket URL** in `TranslationViewModel.kt` (line 25)
   ```kotlin
   private val websocketUrl = "wss://YOUR_API_ID.execute-api.REGION.amazonaws.com/prod"
   ```

2. **Open in Android Studio**
   - Sync Gradle (dependencies auto-download)
   - Build > Rebuild Project

3. **Deploy AWS Backend** (see [WEBSOCKET_INTEGRATION.md](WEBSOCKET_INTEGRATION.md))
   - API Gateway WebSocket
   - 3 Lambda functions (connect/default/disconnect)
   - 2 DynamoDB tables

4. **Run the App**
   - Connect device or start emulator (API 24+)
   - Grant microphone permission
   - Tap circular button to start translation!

### Required Permissions
- `android.permission.RECORD_AUDIO` - For audio recording
- `android.permission.INTERNET` - For WebSocket connection

## Troubleshooting

### Gradle Build Issues
If you encounter Gradle errors:
1. Ensure you have internet connectivity for initial dependency downloads
2. Try: File > Invalidate Caches > Invalidate and Restart
3. Check that Java 11+ is configured as the Gradle JDK

### Java Version Issues
If you see "Android Gradle plugin requires Java 17":
- Go to File > Settings > Build, Execution, Deployment > Build Tools > Gradle
- Set "Gradle JDK" to Java 17 or later
- Or download JDK 17 from https://adoptium.net/

### Missing Dependencies
If dependencies fail to download:
- Check your internet connection
- Try: `./gradlew clean build --refresh-dependencies`

## Key Components

### CircularDesign
The main interactive button that:
- Responds to tap and hold gestures
- Initiates speech recognition
- Features animated scaling and rotation effects
- Displays blue and purple gradient arcs

### ShazamScreenEffect
Background animation that creates:
- Infinite ripple effects from the center
- White expanding circles that fade out
- Continuous loop for visual appeal

### PulsatingWaves
Animated circles around the main button:
- Triggered by tap interactions
- Multiple waves with varying opacity
- Smooth transitions using Compose animations

## Development

### Building from Command Line
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

### Running Tests
```bash
# Unit tests
gradlew.bat test

# Instrumented tests
gradlew.bat connectedAndroidTest
```

## License
This is a sample project for educational purposes.

## Contact
For questions or issues, please refer to the project repository.
