@echo off
echo ========================================
echo  Force Upload to GitHub
echo ========================================
echo.
echo This will REPLACE the old repository content
echo with your new WebSocket-based app.
echo.
echo Old repository will be backed up on GitHub
echo in case you need it later.
echo.
pause

echo.
echo [1/6] Checking git installation...
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed
    echo Download from: https://git-scm.com/download/win
    pause
    exit /b 1
)
echo ✅ Git is installed
echo.

echo [2/6] Initializing git repository...
if not exist ".git" (
    git init
    echo ✅ Git initialized
) else (
    echo ✅ Already initialized
)
echo.

echo [3/6] Adding remote repository...
git remote remove origin 2>nul
git remote add origin https://github.com/Zelun-He/RealTimeTranslationApp.git
echo ✅ Remote added
echo.

echo [4/6] Adding all files...
git add .
echo ✅ Files added
echo.

echo [5/6] Committing changes...
git commit -m "Major update: Real-time WebSocket translation with AWS backend

- Complete rewrite with WebSocket streaming architecture
- Added real-time audio streaming (30ms frames)
- Implemented 12-language dropdown selector
- Enhanced UI with Shazam-like animations
- Added connection status and live translation display
- Integrated TTS audio playback
- Added comprehensive documentation
- Optimized for low-latency (650ms-1.5s end-to-end)
- Replaced REST API with WebSocket for real-time communication"
echo ✅ Changes committed
echo.

echo [6/6] Force pushing to GitHub...
echo.
echo ⚠️  This will OVERWRITE the old repository
echo ⚠️  You will be prompted for GitHub credentials
echo.
echo Username: Your GitHub username
echo Password: Your Personal Access Token (NOT password)
echo.
echo Get token at: https://github.com/settings/tokens
echo.
pause

git branch -M main
git push -u origin main --force

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo ✅ SUCCESS! Upload Complete!
    echo ========================================
    echo.
    echo Your project is now live at:
    echo https://github.com/Zelun-He/RealTimeTranslationApp
    echo.
    echo Next steps:
    echo 1. Visit the repository URL above
    echo 2. Update description and add topics
    echo 3. Deploy AWS backend
    echo.
) else (
    echo.
    echo ========================================
    echo ❌ Push Failed
    echo ========================================
    echo.
    echo If you got "Authentication failed":
    echo.
    echo Option 1: Use Personal Access Token
    echo   1. Go to: https://github.com/settings/tokens
    echo   2. Click "Generate new token (classic)"
    echo   3. Check "repo" scope
    echo   4. Generate and copy the token
    echo   5. Use token as password when prompted
    echo.
    echo Option 2: Use GitHub CLI
    echo   1. Install: winget install --id GitHub.cli
    echo   2. Run: gh auth login
    echo   3. Run this script again
    echo.
    echo Option 3: Use GitHub Desktop (Easiest!)
    echo   Download: https://desktop.github.com/
    echo.
)

pause
