#!/bin/bash

echo "========================================"
echo " TranslatorApp GitHub Upload Script"
echo "========================================"
echo

echo "Step 1: Cloning your existing repository..."
git clone https://github.com/Zelun-He/RealTimeTranslationApp.git temp_repo
cd temp_repo

echo
echo "Step 2: Removing old files..."
rm -rf android-app/
rm -rf backend/
rm -rf translation-app/

echo
echo "Step 3: Copying new project files..."
cp -r ../* . 2>/dev/null || true
rm -rf temp_repo

echo
echo "Step 4: Adding files to git..."
git add .

echo
echo "Step 5: Committing changes..."
git commit -m "Major update: Real-time WebSocket translation with AWS backend

- Added WebSocket client for real-time streaming
- Implemented audio streaming (30ms frames)
- Added 12-language dropdown selector
- Enhanced UI with connection status
- Added translation display (partial + final)
- Integrated TTS audio playback
- Added comprehensive documentation
- Optimized for low-latency (650ms-1.5s end-to-end)"

echo
echo "Step 6: Pushing to GitHub..."
git push origin main

echo
echo "✅ Upload complete!"
echo
echo "Next steps:"
echo "1. Go to https://github.com/Zelun-He/RealTimeTranslationApp"
echo "2. Update repository description and topics"
echo "3. Deploy your AWS backend"
echo
