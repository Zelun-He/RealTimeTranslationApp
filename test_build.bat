@echo off
echo Testing TranslatorApp build...
echo.

echo Step 1: Clean project...
call gradlew.bat clean

echo.
echo Step 2: Build project...
call gradlew.bat assembleDebug

echo.
if %ERRORLEVEL% EQU 0 (
    echo ✅ Build successful!
    echo.
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Next steps:
    echo 1. Update WebSocket URL in TranslationViewModel.kt
    echo 2. Deploy AWS backend
    echo 3. Install and test the app!
) else (
    echo ❌ Build failed with error code %ERRORLEVEL%
    echo.
    echo Check the error messages above.
    echo Common fixes:
    echo - Sync Gradle in Android Studio
    echo - Check internet connection for dependencies
    echo - Update Android SDK if needed
)

pause
