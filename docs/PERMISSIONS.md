# CamCraft Permission Matrix & Security Audit

CamCraft follows strict privacy guidelines: zero network requests (`INTERNET` permission omitted), zero analytics, and minimum required permissions per Android SDK level.

---

## 🔒 Permission Matrix by API Level

| Permission | API Level Range | Why Needed | When Requested | Denial Handling |
|---|---|---|---|---|
| `android.permission.CAMERA` | API 26+ (All) | Real-time CameraX preview surface and high-resolution photo capture | When opening Camera screen for the first time | Displays rationale screen with explanation and "Open System Settings" shortcut |
| `READ_EXTERNAL_STORAGE` | API 26 - 32 | Querying MediaStore gallery photos | When entering Gallery screen | Displays empty gallery with manual retry permission launcher |
| `WRITE_EXTERNAL_STORAGE` | API 26 - 28 (`maxSdkVersion="28"`) | Writing legacy MediaStore photos to `Pictures/CamCraft` | When capturing photos on API 26-28 | Fallbacks to app private directory if denied |
| `READ_MEDIA_IMAGES` | API 33+ (Android 13+) | Granular reading of gallery photos | When opening Gallery screen | Prompts user for photo access; respects partial selection |
| `READ_MEDIA_VISUAL_USER_SELECTED` | API 34+ (Android 14+) | Partial photo access selection | Handled automatically by system permission dialog | Queries only user-selected photos from MediaStore |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | API 26+ (All) | Optional GPS Geotagging in EXIF metadata | **Opt-In Only**: Only when user toggles "GPS Geotagging" in Settings | Disables geotagging toggle gracefully; captures photos without GPS tags |

---

## 🚫 Strictly Prohibited Permissions
- **NO `INTERNET`**: CamCraft cannot transmit any data off device.
- **NO `MANAGE_EXTERNAL_STORAGE`**: CamCraft works strictly within Scoped Storage standards and SAF.
- **NO `RECORD_AUDIO`**: Photo capture requires no audio permission.
