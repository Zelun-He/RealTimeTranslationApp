@echo off
echo Opening project in Android Studio...
echo.
echo Looking for Android Studio installation...

set STUDIO_PATH=
if exist "C:\Program Files\Android\Android Studio\bin\studio64.exe" (
    set STUDIO_PATH=C:\Program Files\Android\Android Studio\bin\studio64.exe
)
if exist "%LOCALAPPDATA%\Programs\Android Studio\bin\studio64.exe" (
    set STUDIO_PATH=%LOCALAPPDATA%\Programs\Android Studio\bin\studio64.exe
)

if defined STUDIO_PATH (
    echo Found Android Studio at: %STUDIO_PATH%
    echo Opening project...
    start "" "%STUDIO_PATH%" "%~dp0"
) else (
    echo Android Studio not found!
    echo Please install Android Studio or open this folder manually in your IDE.
    echo Project location: %~dp0
    pause
)


