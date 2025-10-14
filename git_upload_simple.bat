@echo off
REM Simple Git Upload - Step by Step with Pause

echo ========================================
echo  TranslatorApp GitHub Upload
echo ========================================
echo.
echo This script will upload your project to:
echo https://github.com/Zelun-He/RealTimeTranslationApp
echo.
pause

echo.
echo [1/5] Initializing git repository...
git init
echo.
pause

echo.
echo [2/5] Adding remote repository...
git remote add origin https://github.com/Zelun-He/RealTimeTranslationApp.git 2>nul
git remote set-url origin https://github.com/Zelun-He/RealTimeTranslationApp.git
echo.
pause

echo.
echo [3/5] Adding all files...
git add .
echo Files added successfully!
echo.
pause

echo.
echo [4/5] Committing changes...
git commit -m "Major update: Real-time WebSocket translation with AWS backend"
echo.
pause

echo.
echo [5/5] Pushing to GitHub...
echo You may be prompted for GitHub credentials...
echo.
git branch -M main
git push -u origin main --force
echo.

if %errorlevel% equ 0 (
    echo ========================================
    echo ✅ SUCCESS! Project uploaded to GitHub!
    echo ========================================
    echo.
    echo View your project at:
    echo https://github.com/Zelun-He/RealTimeTranslationApp
) else (
    echo ========================================
    echo ❌ Upload failed
    echo ========================================
    echo.
    echo Try these solutions:
    echo 1. Make sure you're logged into GitHub
    echo 2. Run: gh auth login
    echo 3. Or use GitHub Desktop instead
)

echo.
pause
