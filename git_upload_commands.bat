@echo off
echo ========================================
echo  TranslatorApp GitHub Upload Script
echo ========================================
echo.

REM Check if git is installed
git --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Git is not installed or not in PATH
    echo Please install Git from https://git-scm.com/download/win
    pause
    exit /b 1
)

echo Current directory: %CD%
echo.

REM Check if already initialized
if exist ".git" (
    echo Git repository already initialized.
    echo Updating existing repository...
    goto :update_repo
)

echo Step 1: Initializing git repository...
git init
if %errorlevel% neq 0 (
    echo ERROR: Failed to initialize git repository
    pause
    exit /b 1
)

echo Step 2: Adding remote repository...
git remote add origin https://github.com/Zelun-He/RealTimeTranslationApp.git
if %errorlevel% neq 0 (
    echo Remote may already exist, continuing...
    git remote set-url origin https://github.com/Zelun-He/RealTimeTranslationApp.git
)

:update_repo
echo Step 3: Adding all files...
git add .
if %errorlevel% neq 0 (
    echo ERROR: Failed to add files
    pause
    exit /b 1
)

echo Step 4: Committing changes...
git commit -m "Major update: Real-time WebSocket translation with AWS backend - Added WebSocket client for real-time streaming - Implemented audio streaming (30ms frames) - Added 12-language dropdown selector - Enhanced UI with connection status - Added translation display (partial and final) - Integrated TTS audio playback - Added comprehensive documentation - Optimized for low-latency (650ms-1.5s end-to-end)"
if %errorlevel% neq 0 (
    echo WARNING: Commit failed. This might be because there are no changes to commit.
    echo Continuing anyway...
)

echo Step 5: Setting branch to main...
git branch -M main

echo Step 6: Pushing to GitHub...
echo.
echo You will be prompted for GitHub credentials...
echo.
git push -u origin main
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Push failed!
    echo.
    echo Common reasons:
    echo 1. Not authenticated with GitHub
    echo 2. Repository doesn't exist or you don't have access
    echo 3. Need to pull changes first
    echo.
    echo Try running: git pull origin main --allow-unrelated-histories
    echo Then run this script again.
    pause
    exit /b 1
)

echo.
echo ========================================
echo ✅ Upload complete!
echo ========================================
echo.
echo Your project is now at:
echo https://github.com/Zelun-He/RealTimeTranslationApp
echo.
echo Next steps:
echo 1. Go to the repository URL above
echo 2. Update repository description and topics
echo 3. Deploy your AWS backend
echo.
pause
