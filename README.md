# CamCraft - Native Professional Photography App for Android

CamCraft is a complete, real, native Android camera application built using **CameraX**, **Jetpack Compose (Material 3)**, **Scoped Storage / MediaStore**, **Storage Access Framework (SAF)**, and **AndroidX ExifInterface**.

Every feature uses real device hardware APIs, real files, and true system contracts with no fake or mocked operations.

---

## 🌟 Key Features

### 1. Landing Screen
- Photography-inspired dark UI with custom gold accents and smooth transitions.
- Quick navigation to **Open Camera**, **Saved Photos**, and **Settings**.
- Live badge displaying saved photo counts and latest shot preview.

### 2. CameraX Preview & Photo Capture
- High-performance `PreviewView` bound to Android lifecycle.
- Front/Back camera switching with hardware capability detection.
- Dynamic Flash modes (**Auto**, **On**, **Off**) with graceful fallback for cameras without a flash unit.
- Real-time **Tap-to-Focus** with animated focus ring and **Pinch-to-Zoom** gestures.
- Full-resolution photo capture via `ImageCapture` with real-time feedback and shutter animation.

### 3. Advanced Storage Engine (Scoped Storage & SAF)
- Default saving to `Pictures/CamCraft` via MediaStore.
- **Custom SAF Folder**: Choose any local or SD card folder via `ACTION_OPEN_DOCUMENT_TREE`.
- Persisted tree permissions (`takePersistableUriPermission`), automatic launch re-validation, and silent fallback to `Pictures/CamCraft` if SAF folder is unmounted or deleted.
- Full compatibility with Android 8.0 (API 26) through Android 15 (API 35).

### 4. Saved Photos Gallery & Detail Viewer
- Adaptive grid layout powered by Coil thumbnail loading.
- Detailed viewer with swipeable `HorizontalPager`, pinch-zoom, and metadata inspector.
- **Scoped Storage Deletion**: Uses `MediaStore.createDeleteRequest` (API 30+), `RecoverableSecurityException` (API 29), and SAF contracts.
- Native sharing via `ACTION_SEND` using `content://` URIs and `FileProvider`.

### 5. Non-Destructive Image Editing
- Custom Compose interactive crop overlay supporting **Freeform**, **1:1**, **4:3**, **16:9**, and **3:2** ratios.
- 90-degree step rotation and full-resolution bitmap processing.
- Preserves original EXIF metadata tags and standardizes orientation.
- Always saves as a **new copy** (never overwriting the original).

### 6. Geotagging, Composition Grids & Self-Timer
- Self-timer (Off, 3s, 5s, 10s) with live visual countdown overlay and cancellation.
- Composition grid overlays (3x3 Rule of Thirds, 4x4, Golden Ratio).
- Opt-in GPS Geotagging with runtime location permission requests.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin 2.0 (Kotlin DSL)
- **UI Framework:** Jetpack Compose (Material 3) + Navigation Compose
- **Camera Pipeline:** AndroidX CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`)
- **Storage:** MediaStore API + Storage Access Framework (`DocumentFile`) + `FileProvider`
- **Preferences:** DataStore Preferences
- **Image Loading:** Coil (`coil-compose`)
- **Metadata:** AndroidX `ExifInterface`
- **Architecture:** MVVM with unidirectional data flow (StateFlow + SharedFlow + Coroutines)

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 35 (minSdk 26)

### Build Command
```bash
./gradlew assembleDebug
```

### Run Unit & UI Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 📱 Documentation Index
- [`docs/SETUP.md`](docs/SETUP.md): SDK setup, developer options, USB debugging, adb installation.
- [`docs/PERMISSIONS.md`](docs/PERMISSIONS.md): Complete permissions matrix per API level.
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md): System architecture, package layout, and lifecycle sequence diagrams.
- [`docs/TESTING.md`](docs/TESTING.md): Verification report, emulator vs hardware testing, manual QA checklist.
- [`docs/DECISIONS.md`](docs/DECISIONS.md): Architectural decisions log.

---

## ⚠️ Known Hardware Limitations
- Flash modes depend on physical hardware flash units.
- GPS Geotagging requires an active GPS fix or coarse location provider.
