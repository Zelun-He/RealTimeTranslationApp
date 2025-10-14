# Manual Upload Steps (If Scripts Don't Work)

## What Error Did You Get?

Common errors and solutions:

### Error: "git is not recognized"
**Solution**: Git is not installed
1. Download from: https://git-scm.com/download/win
2. Install and restart terminal
3. Run script again

### Error: "fatal: not a git repository"
**Solution**: Initialize git first
```bash
git init
git remote add origin https://github.com/Zelun-He/RealTimeTranslationApp.git
```

### Error: "Authentication failed"
**Solution**: Login to GitHub
```bash
# Option 1: Use GitHub CLI
gh auth login

# Option 2: Use Personal Access Token
# Go to GitHub Settings > Developer Settings > Personal Access Tokens
# Generate a token and use it as password when prompted
```

### Error: "failed to push"
**Solution**: Force push or pull first
```bash
git pull origin main --allow-unrelated-histories
# Then try again
git push -u origin main
```

---

## 📋 Manual Upload (Step-by-Step)

### Method 1: Command Line (5 Steps)

Open PowerShell in your project folder and run:

```powershell
# Step 1: Initialize git
git init

# Step 2: Add remote
git remote add origin https://github.com/Zelun-He/RealTimeTranslationApp.git

# Step 3: Add files
git add .

# Step 4: Commit
git commit -m "Major update: Real-time WebSocket translation"

# Step 5: Push
git branch -M main
git push -u origin main --force
```

If prompted for credentials:
- **Username**: Your GitHub username
- **Password**: Your Personal Access Token (NOT your GitHub password)

### Method 2: GitHub Desktop (Easiest) ⭐

1. **Download GitHub Desktop**: https://desktop.github.com/
2. **Open GitHub Desktop**
3. **File > Add Local Repository**
4. Choose your `TranslatorApp` folder
5. Click **"Publish repository"**
6. Select your existing repository
7. **Done!** ✅

### Method 3: GitHub Web Upload

1. **Go to**: https://github.com/Zelun-He/RealTimeTranslationApp
2. **Delete old files** (android-app, backend folders)
3. **Click** "Add file" > "Upload files"
4. **Drag your entire project folder**
5. **Commit changes**

---

## 🔑 GitHub Authentication

### Get Personal Access Token:

1. **Go to**: https://github.com/settings/tokens
2. **Click** "Generate new token (classic)"
3. **Give it a name**: "TranslatorApp Upload"
4. **Select scopes**: 
   - ✅ `repo` (all)
5. **Generate token**
6. **Copy the token** (you won't see it again!)
7. **Use it as password** when git asks

### Or Use GitHub CLI:

```bash
# Install GitHub CLI
winget install --id GitHub.cli

# Login
gh auth login

# Then run the upload script again
```

---

## 🎯 Simplest Solution

**Use GitHub Desktop** - it handles authentication automatically and has a visual interface!

Download: https://desktop.github.com/

---

## Still Having Issues?

Tell me the exact error message you got and I'll help you fix it! Common issues:

- ❌ "Permission denied" → Authentication problem
- ❌ "Repository not found" → Wrong URL or no access
- ❌ "Git not found" → Git not installed
- ❌ "Nothing to commit" → No changes detected
- ❌ "Failed to push" → Need to pull first or force push

What error did you see?
