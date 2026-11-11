# CamCraft Verification & Testing Report

## 🧪 1. Verified by Automated Build & Tests
The following logic has been verified via unit tests and automated Gradle build (`./gradlew assembleDebug`):

- **Unit Tests (`./gradlew test`)**:
  - `ExifOrientationTest`: EXIF orientation angle calculation and 360-degree wrap-around math.
  - `CropMathTest`: Normalized crop box to bitmap pixel conversion and square aspect ratio constraints.
  - `FilenameGeneratorTest`: CamCraft filename pattern formatting (`CAMCRAFT_timestamp.jpg` and `EDIT_timestamp.png`).
- **Instrumented Tests (`./gradlew connectedAndroidTest`)**:
  - `LandingScreenTest`: Compose UI test verifying Landing screen components and buttons.
- **Build Verification**:
  - `./gradlew assembleDebug` succeeds with 0 errors.

---

## 📱 2. Hardware vs Emulator Verification Notice

| Feature Area | Verifiable in Emulator | Requires Physical Device |
|---|---|---|
| Camera Preview Surface | ✅ Emulator virtual camera | ✅ Physical camera sensor |
| Photo Capture & File Write | ✅ Saves to MediaStore / SAF | ✅ Physical resolution capture |
| Front / Back Switching | ⚠️ Standard emulation | ✅ Real Dual-Camera Sensors |
| Hardware Flash Modes | ❌ No physical flash unit | ✅ Real Flash LED |
| Pinch-to-Zoom & Focus | ✅ Simulated gesture | ✅ Real Optical / Digital Zoom |
| Geotagging EXIF | ⚠️ Extended controls location | ✅ Real GPS Fix |

---

## 📋 3. Manual QA Checklist

- [x] **App Launch & Theme**: Launch app, verify photography dark theme renders cleanly.
- [x] **Camera Permission**: Tap "Open Camera", grant permission, verify camera preview starts.
- [x] **Permission Denial**: Revoke camera permission in Settings, verify rationale screen displays with "Open System Settings".
- [x] **Photo Capture**: Tap shutter button, verify shutter animation, progress indicator, and file creation in `Pictures/CamCraft`.
- [x] **Camera Switch**: Tap camera switch button, verify front and back camera flip smoothly.
- [x] **Flash Modes**: Cycle flash modes (Auto -> On -> Off), verify icon updates.
- [x] **Grid Overlay**: Cycle grid overlays (3x3 -> 4x4 -> Golden Ratio -> Off).
- [x] **Self-Timer**: Set 3s timer, tap shutter, verify countdown numbers (3..2..1) and photo capture.
- [x] **Gallery Grid**: Tap thumbnail, verify photos display in newest-first order with Coil.
- [x] **Detail & Zoom**: Tap photo in gallery, swipe horizontal pager, pinch-to-zoom.
- [x] **EXIF Inspector**: Open metadata bottom sheet, verify date, size, resolution, and EXIF tags.
- [x] **Photo Editing**: Tap Edit, drag crop handles, select 1:1 aspect ratio, rotate 90°, tap Save Copy, verify new file created in gallery.
- [x] **Safe Deletion**: Delete photo in detail viewer, confirm dialog, verify item removed from MediaStore.
- [x] **SAF Custom Folder**: Open Settings, choose custom folder via SAF tree picker, capture photo, verify photo written into SAF directory.
