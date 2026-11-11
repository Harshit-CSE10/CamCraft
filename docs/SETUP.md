# CamCraft Setup & Deployment Guide

## Prerequisites
1. **JDK Version**: Java 17+ (JDK 17 or JDK 21 recommended).
2. **Android Studio**: Android Studio Hedgehog / Iguana / Ladybug or newer.
3. **Android SDK Target**: SDK 35 (`compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`).

---

## 🛠️ Building the Project

### Command Line (macOS / Linux / Windows)
```bash
# Clean project build
./gradlew clean

# Build Debug APK
./gradlew assembleDebug

# Run Unit Tests
./gradlew test
```

The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Running on a Physical Hardware Device

### 1. Enable Developer Options on Device
1. Open **Settings** -> **About Phone**.
2. Tap **Build Number** 7 times until you see `"You are now a developer!"`.

### 2. Enable USB Debugging
1. Open **Settings** -> **System** -> **Developer Options**.
2. Enable **USB Debugging**.

### 3. Install & Launch via ADB
Connect device via USB and run:
```bash
adb devices
# Verify device is listed

adb install -r app/build/outputs/apk/debug/app-debug.apk
# Launch app
adb shell am start -n com.camcraft.app/.MainActivity
```
