# CamCraft

CamCraft is an Android camera app written in Kotlin with Jetpack Compose and CameraX. It captures photos with the device camera, saves them to shared storage or a folder you choose, and includes a gallery with basic crop and rotate editing. Photos stay on the device. The app does not request the INTERNET permission.

## Features

### Camera
- Live preview using CameraX `PreviewView`, bound to the Android lifecycle
- Front and back camera switching (hidden on devices with a single camera)
- Flash modes Auto, On and Off, shown only when the selected camera has a flash unit
- Tap to focus and pinch to zoom
- Full-resolution capture with `ImageCapture`, with shutter animation and error feedback
- Self-timer with 3, 5 and 10 second options, including a countdown and cancel
- Grid overlays: rule of thirds, 4x4 and golden ratio
- Optional GPS geotagging, off by default. Location permission is requested only when you turn it on.

### Storage
- Photos are saved to `Pictures/CamCraft` through MediaStore by default
- A custom save folder can be chosen with the Storage Access Framework (`ACTION_OPEN_DOCUMENT_TREE`)
- The folder permission is persisted with `takePersistableUriPermission` and checked on every launch. If the folder is no longer available, the app falls back to `Pictures/CamCraft` and shows a message.
- Supports Android 8.0 (API 26) through Android 15 (API 35)

### Gallery
- Grid layout with thumbnails loaded by Coil
- Photo viewer with swipe navigation, pinch-zoom and a metadata panel (timestamps, dimensions, EXIF)
- Delete with confirmation, using `MediaStore.createDeleteRequest` on API 30+, `RecoverableSecurityException` handling on API 29, and `DocumentsContract` for files in a SAF folder
- Sharing through the Android share sheet with `content://` URIs

### Editing
- Crop with freeform, 1:1, 4:3, 16:9 and 3:2 ratios
- Rotation in 90-degree steps
- Edits are applied to the full-resolution image, not the preview
- Edited photos are saved as new files. The original is never overwritten, and EXIF data is kept with the orientation tag corrected.

## Tech stack

- Kotlin and Jetpack Compose (Material 3), Navigation Compose
- CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`)
- MediaStore, Storage Access Framework (`DocumentFile`), `FileProvider`
- DataStore Preferences for settings
- Coil for image loading
- AndroidX `ExifInterface`
- MVVM with StateFlow, SharedFlow and Coroutines
- Gradle Kotlin DSL with a version catalog

## Building

Requirements:
- Android Studio Ladybug or newer
- JDK 17 or newer
- Android SDK 35 (minSdk is 26)

Build a debug APK:

    ./gradlew assembleDebug

Run the tests:

    ./gradlew test
    ./gradlew connectedAndroidTest

`connectedAndroidTest` needs a connected device or running emulator.

## Documentation

- [docs/SETUP.md](docs/SETUP.md): SDK setup, developer options, USB debugging, installing with adb
- [docs/PERMISSIONS.md](docs/PERMISSIONS.md): permissions used and how they differ by API level
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md): package layout, data flow and lifecycle handling
- [docs/TESTING.md](docs/TESTING.md): test results, emulator limitations, manual QA checklist
- [docs/DECISIONS.md](docs/DECISIONS.md): design decisions and their reasons

## Known limitations

- Flash availability depends on the device hardware.
- Captured photos are JPEG. CameraX does not output PNG or RAW through `ImageCapture`.
- The emulator cannot properly test flash, real front and back sensors, full-resolution capture or GPS. Test these on a physical device.
- GPS Geotagging requires an active GPS fix or coarse location provider.
