# PAN & Aadhaar Extractor (India) 🇮🇳

A production-ready Android application for extracting information from PAN and Aadhaar cards using on-device ML Kit OCR. Built with **Kotlin**, **Jetpack Compose**, and **Material 3** design.

## ✨ Features

### Document Recognition
- **Automatic Detection**: Identifies PAN and Aadhaar cards automatically
- **On-Device OCR**: Uses ML Kit Text Recognition for privacy-first processing
- **Smart Parsing**: Extracts relevant fields (Name, DOB, PAN Number, etc.)
- **PAN Validation**: Validates PAN format using regex
- **Aadhaar Masking**: Privacy-compliant masking (XXXX XXXX 1234)

### Monetization Model
- **Free Tier**: 3 scans with view-only access
- **Pro Tier**: ₹249 one-time purchase
  - Unlimited scans
  - Copy extracted data
  - Export to PDF & CSV
  - Save unlimited profiles

### Export Options
- **PDF**: Generate profile summaries
- **CSV**: Bulk export all saved profiles
- **Share**: FileProvider integration for secure sharing

## 🏗️ Architecture

### Tech Stack
- **Language**: Kotlin 1.9.22
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Camera**: CameraX API
- **OCR**: ML Kit Text Recognition
- **Billing**: Google Play Billing Library v6+
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)

### Project Structure
```
app/src/main/java/com/panaadhaar/extractor/india/
├── data/
│   ├── database/        # Room entities & DAOs
│   ├── model/           # Data models
│   └── repository/      # Data repositories
├── ui/
│   ├── navigation/      # Navigation graph
│   ├── screens/         # Compose UI screens
│   └── theme/           # Material 3 theme
├── utils/               # Utilities & helpers
├── MainActivity.kt
├── PanAadhaarApplication.kt
└── ViewModelFactory.kt
```

## 📱 Screens

1. **Splash Screen**: Entitlement check and initialization
2. **Home Screen**: Scan options and recent scans list
3. **Camera Screen**: CameraX capture with torch mode
4. **Results Screen**: Extracted data with copy/save options
5. **Profiles Screen**: Saved profiles management
6. **Export Screen**: PDF & CSV export (Pro only)
7. **Paywall Screen**: Feature comparison and purchase
8. **Settings Screen**: App settings and data management

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Building the Project

1. **Clone the repository**:
   ```bash
   git clone https://github.com/zaheerabbas7892034214-ai/PAN-Aadhaar-Extractor-India-.git
   cd PAN-Aadhaar-Extractor-India-
   ```

2. **Open in Android Studio**:
   - File → Open → Select project directory
   - Wait for Gradle sync to complete

3. **Build and run**:
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

## 🔒 Privacy & Security

- **100% On-Device Processing**: No data sent to external servers
- **No Internet Required**: Fully offline functionality
- **Secure Storage**: Room Database with encryption support
- **Aadhaar Masking**: Compliant with privacy regulations
- **User Control**: Delete all data option

## 📦 Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| Jetpack Compose | 2024.02.00 | Modern UI toolkit |
| Room | 2.6.1 | Local database |
| CameraX | 1.3.1 | Camera integration |
| ML Kit | 16.0.0 | Text recognition |
| Billing | 6.1.0 | In-app purchases |
| iText | 5.5.13.3 | PDF generation |
| Commons CSV | 1.10.0 | CSV export |

## 📄 License

Copyright © 2026. All rights reserved.

## 🤝 Contributing

This is a complete production-ready project. Contributions are welcome for:
- Bug fixes
- Performance improvements
- Additional document types
- UI/UX enhancements

## 📞 Support

For issues or questions, please open an issue on GitHub.

---

**Built with ❤️ for Indian document processing needs**
