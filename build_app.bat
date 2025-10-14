@echo off
echo Building TranslatorApp...
echo.
gradlew.bat assembleDebug
echo.
if %ERRORLEVEL% EQU 0 (
    echo Build successful!
    echo APK location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo Build failed with error code %ERRORLEVEL%
)
pause

