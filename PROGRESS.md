# CamCraft Progress & Implementation Checklist

## Stage 1: Core Setup, Landing Screen, Permission Flow, Camera Preview & Real Capture
- [x] Project configuration (Gradle Version Catalog, dependencies, manifest permissions)
- [x] Dark photography theme & Material 3 styling
- [x] Navigation structure (Landing, Camera, Gallery, Editor, Settings)
- [x] Landing screen UI with quick navigation
- [x] Camera permission handler with rationale & settings shortcut
- [x] CameraX binding with PreviewView & ImageCapture
- [x] Shutter button, real photo capture & feedback

## Stage 2: Camera Controls, Hardware Interop & Lifecycle Hardening
- [x] Front/back camera switching & hardware check
- [x] Flash modes (Auto/On/Off) with front camera fallback
- [x] Tap-to-focus and pinch-to-zoom support
- [x] Rotation and target rotation handling
- [x] Lifecycle unbind/rebind hardening (no camera session leaks)

## Stage 3: MediaStore Storage, Custom SAF Folder & Thumbnail Integration
- [x] MediaStore saving (`Pictures/CamCraft`) with API level compatibility (26-34+)
- [x] SAF custom folder selector (`ACTION_OPEN_DOCUMENT_TREE`) with persisted permissions
- [x] SAF uri validation & fallback strategy
- [x] Latest-photo thumbnail preview in Camera screen
- [x] MediaScanner integration for device gallery visibility

## Stage 4: Saved Photos Gallery, Detail Viewer & Safe Deletion
- [x] Media item repository (combining MediaStore & SAF sources)
- [x] Responsive grid gallery UI with Coil loading & empty/error states
- [x] Detail screen viewer with `HorizontalPager` & zoom support
- [x] Scoped storage safe deletion (MediaStore API 30+ delete requests, API 29 RecoverableSecurityException, SAF contract)

## Stage 5: Non-destructive Image Editing, EXIF & Sharing
- [x] Custom Compose Crop overlay (Freeform, 1:1, 4:3, 16:9, 3:2)
- [x] 90-degree rotation & full-res processing without OOM
- [x] Save non-destructive copy with corrected EXIF metadata
- [x] Detailed Metadata bottom sheet (EXIF attributes, ISO, aperture, GPS)
- [x] Native Share via `ACTION_SEND` using `content://` URIs

## Stage 6: Advanced Camera Features & Settings Persistence
- [x] DataStore preferences repository
- [x] Self-timer (Off, 3s, 5s, 10s) with visual countdown overlay
- [x] Composition grid overlay (Rule of Thirds, 4x4, Golden Ratio)
- [x] Opt-in GPS Geotagging with runtime location permission handling
- [x] Burst capture implementation / UI safety toggle
- [x] Full Settings screen (save location, format, quality, theme, grid, timer)

## Stage 7: Quality Assurance, Automated Testing & Documentation
- [x] Unit tests (ViewModels, EXIF parsing, crop math, filename generation, storage fallback)
- [x] Instrumented Compose UI & MediaStore tests
- [x] Complete documentation (`README.md`, `SETUP.md`, `PERMISSIONS.md`, `ARCHITECTURE.md`, `TESTING.md`, `DECISIONS.md`)
- [x] Final Gradle assembleDebug verification
