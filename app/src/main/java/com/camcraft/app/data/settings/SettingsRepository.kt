package com.camcraft.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "camcraft_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SAF_URI = stringPreferencesKey("saf_uri")
        val SAF_NAME = stringPreferencesKey("saf_name")
        val IMAGE_FORMAT = stringPreferencesKey("image_format")
        val JPEG_QUALITY = intPreferencesKey("jpeg_quality")
        val CAPTURE_QUALITY_MODE = intPreferencesKey("capture_quality_mode")
        val TIMER_DURATION = intPreferencesKey("timer_duration")
        val GRID_MODE = stringPreferencesKey("grid_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GEOTAGGING = booleanPreferencesKey("geotagging")
        val BURST_MODE = booleanPreferencesKey("burst_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            customSafUri = prefs[Keys.SAF_URI],
            customSafName = prefs[Keys.SAF_NAME],
            imageFormat = ImageFormat.entries.firstOrNull { it.name == prefs[Keys.IMAGE_FORMAT] } ?: ImageFormat.JPEG,
            jpegQuality = prefs[Keys.JPEG_QUALITY] ?: 95,
            captureQualityMode = prefs[Keys.CAPTURE_QUALITY_MODE] ?: 0,
            timerDurationSeconds = prefs[Keys.TIMER_DURATION] ?: 0,
            gridMode = GridMode.entries.firstOrNull { it.name == prefs[Keys.GRID_MODE] } ?: GridMode.RULE_OF_THIRDS,
            themeMode = ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] } ?: ThemeMode.DARK,
            enableGeotagging = prefs[Keys.GEOTAGGING] ?: false,
            burstModeEnabled = prefs[Keys.BURST_MODE] ?: false
        )
    }

    suspend fun updateSafFolder(uri: String?, name: String?) {
        context.dataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(Keys.SAF_URI)
                prefs.remove(Keys.SAF_NAME)
            } else {
                prefs[Keys.SAF_URI] = uri
                prefs[Keys.SAF_NAME] = name ?: "Custom Folder"
            }
        }
    }

    suspend fun updateImageFormat(format: ImageFormat) {
        context.dataStore.edit { prefs -> prefs[Keys.IMAGE_FORMAT] = format.name }
    }

    suspend fun updateJpegQuality(quality: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.JPEG_QUALITY] = quality.coerceIn(80, 100) }
    }

    suspend fun updateCaptureQualityMode(mode: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.CAPTURE_QUALITY_MODE] = mode }
    }

    suspend fun updateTimerDuration(seconds: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.TIMER_DURATION] = seconds }
    }

    suspend fun updateGridMode(gridMode: GridMode) {
        context.dataStore.edit { prefs -> prefs[Keys.GRID_MODE] = gridMode.name }
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { prefs -> prefs[Keys.THEME_MODE] = themeMode.name }
    }

    suspend fun updateGeotagging(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.GEOTAGGING] = enabled }
    }

    suspend fun updateBurstMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.BURST_MODE] = enabled }
    }
}
