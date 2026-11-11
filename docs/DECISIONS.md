# Technical Decisions Log - CamCraft

## 1. Single-Activity Architecture with Navigation Compose
- **Decision:** Use a single `MainActivity` with Navigation Compose for UI transitions.
- **Rationale:** Ensures unified lifecycle handling, simplified state sharing across screens via ViewModel/StateFlow, and zero Activity overhead.

## 2. Storage Strategy by API Level
- **Decision:**
  - API 26-28: Write directly to `Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)/CamCraft` using MediaScanner scan file.
  - API 29-34+: Use `ContentResolver` + `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` with `RELATIVE_PATH = "Pictures/CamCraft"`.
  - Custom Folder (SAF): Store tree URI via `takePersistableUriPermission(READ | WRITE)` in DataStore. Validate tree validity before every write/read. Fall back to `Pictures/CamCraft` if SAF tree is unmounted, revoked, or inaccessible.

## 3. Image Editing Engine
- **Decision:** Custom Compose `CropOverlay` UI with low-res preview rendering, applying full-resolution transformations (crop & 90° steps rotation) on `Dispatchers.IO` using `BitmapRegionDecoder` / downsampled sampling to prevent `OutOfMemoryError`.
- **Rationale:** Strict prohibition on 3rd-party crop libraries. Keeps the APK lightweight, performant, and fully compliant with scoped storage.

## 4. CameraX Lifecycle Binding
- **Decision:** Bind `Preview` and `ImageCapture` together in a single `LaunchedEffect` keyed on `lensFacing`, `flashMode`, and `aspectRatio`. Always call `ProcessCameraProvider.unbindAll()` prior to rebinding.
- **Rationale:** Prevents background camera leaks, handles device orientation changes cleanly, and guarantees smooth surface updates.

## 5. Burst Capture Handling
- **Decision:** Provide a sequential burst capture with bounded count (max 10 photos) on `Dispatchers.IO` with locked capture UI.
- **Rationale:** Guarantees camera buffer stability without dropping frames or triggering camera provider deadlocks on lower-end devices.
