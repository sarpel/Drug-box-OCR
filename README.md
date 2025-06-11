# 📱 Simple Box Name OCR Android App

**A minimal, focused OCR application that does exactly what you need:**

1. 📷 **Camera text reading** (CameraX + Gemini OCR)
2. 🔍 **Database comparison** (your provided database file)  
3. 📝 **Log output** (simple session history)
4. 📋 **Clipboard copy** (instant copy of matched result)

## 🎯 Core Features

### ✅ **Simplicity First**
- **Under 500 lines** of core logic (vs 10,000+ enterprise version)
- **3 screens**: Home, Camera, Settings
- **No complex databases** - just load your text file
- **Direct clipboard copy** - one tap and it's copied

### ✅ **Essential Functionality**
- **CameraX integration** for reliable photo capture
- **Gemini API OCR** for accurate text extraction
- **String similarity matching** against your database
- **Recent scan history** for quick reference
- **Settings** for API key and matching sensitivity

## 🏗️ Simple Architecture

```
📱 Simple Android App (3 screens)
├── 🏠 Home Screen
│   ├── Load database file (any .txt/.csv)
│   ├── View recent scans
│   └── Navigate to camera
│
├── 📷 Camera Screen  
│   ├── CameraX preview
│   ├── Capture photo
│   ├── OCR processing (Gemini API)
│   ├── Find best database match
│   └── Copy result to clipboard
│
└── ⚙️ Settings Screen
    ├── Gemini API key
    ├── Matching sensitivity
    └── Test API connection
```

## 🚀 Quick Setup

### 1. **Get Gemini API Key**
- Go to [Google AI Studio](https://aistudio.google.com/)
- Create a new API key
- Enter it in the Settings screen

### 2. **Prepare Your Database**
- Create a simple text file with your box names
- One name per line
- Load it via the "Load Database" button

### 3. **Start Scanning**
- Tap the camera button
- Point at a box label
- Tap capture
- Result automatically copied to clipboard!

## 📂 Database Format

Your database file can be any text format:
```
Box Name 1
Another Box Name
Product ABC-123
Special Item XYZ
...
```

The app will find the closest match using intelligent string similarity.

## 🛠️ Technical Stack

- **Language**: Kotlin 100%
- **UI**: Jetpack Compose + Material 3
- **Camera**: CameraX
- **OCR**: Google Gemini API
- **Architecture**: MVVM + Repository pattern
- **DI**: Hilt
- **Minimum SDK**: Android 7.0 (API 24)

## 📱 App Structure

### Core Components:
- `MainActivity.kt` - Single activity with navigation
- `HomeScreen.kt` - Database management and recent scans
- `CameraScreen.kt` - Camera capture and OCR processing
- `SettingsScreen.kt` - API key and preferences

### Repositories:
- `OCRRepository.kt` - Handles Gemini API calls
- `InMemoryDatabaseRepository.kt` - Database loading and matching
- `ScanHistoryRepository.kt` - Recent scan tracking
- `SettingsRepository.kt` - App preferences

## 🔄 Simple Workflow

1. **Load** your database file (one time setup)
2. **Scan** box labels with camera
3. **Match** against database automatically  
4. **Copy** result to clipboard instantly
5. **Paste** anywhere you need it!

## 🆚 vs Enterprise Version

| Feature | Simple App | Enterprise Version |
|---------|------------|-------------------|
| **Code Lines** | <500 lines | 10,000+ lines |
| **Setup Time** | 5 minutes | Hours |
| **Complexity** | Minimal | Full enterprise platform |
| **Use Case** | Personal/small team | Enterprise medical platform |
| **Database** | Simple text file | 16,632 drug database |
| **AI Models** | Gemini only | Multi-model support |
| **Windows Integration** | None | Full automation |

## 📝 Notes

- **Enterprise version archived** in `box-name-ocr-archive/` folder
- **Focused on core need**: Camera → OCR → Match → Copy → Clipboard
- **Easy to modify** and extend for your specific requirements
- **Perfect for** personal use, small teams, simple workflows

---

**This is exactly what you asked for: simple, focused, and gets the job done!** 🎯
