package com.camcraft.app.data.settings

enum class ImageFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

enum class GridMode(val label: String) {
    NONE("Off"),
    RULE_OF_THIRDS("3x3 Grid"),
    GRID_4X4("4x4 Grid"),
    GOLDEN_RATIO("Golden Ratio")
}

enum class ThemeMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    SYSTEM("System Default")
}

data class UserPreferences(
    val customSafUri: String? = null,
    val customSafName: String? = null,
    val imageFormat: ImageFormat = ImageFormat.JPEG,
    val jpegQuality: Int = 95,
    val captureQualityMode: Int = 0, // 0 = MAXIMIZE_QUALITY, 1 = MINIMIZE_LATENCY
    val timerDurationSeconds: Int = 0, // 0, 3, 5, 10
    val gridMode: GridMode = GridMode.RULE_OF_THIRDS,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val enableGeotagging: Boolean = false,
    val burstModeEnabled: Boolean = false
)
