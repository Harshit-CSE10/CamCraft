package com.camcraft.app.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.app.data.media.SafManager
import com.camcraft.app.data.settings.GridMode
import com.camcraft.app.data.settings.ImageFormat
import com.camcraft.app.data.settings.SettingsRepository
import com.camcraft.app.data.settings.ThemeMode
import com.camcraft.app.data.settings.UserPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SettingsEvent {
    data class ShowToast(val message: String) : SettingsEvent()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)
    val safManager = SafManager(application)

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow: SharedFlow<SettingsEvent> = _eventFlow.asSharedFlow()

    init {
        viewModelScope.launch {
            settingsRepository.userPreferencesFlow.collect { prefs ->
                _userPreferences.value = prefs
            }
        }
    }

    fun onSafFolderSelected(treeUri: Uri) {
        viewModelScope.launch {
            val success = safManager.persistUriPermission(treeUri)
            if (success) {
                val folderName = treeUri.lastPathSegment?.split(":")?.lastOrNull() ?: "Custom SAF Folder"
                settingsRepository.updateSafFolder(treeUri.toString(), folderName)
                _eventFlow.emit(SettingsEvent.ShowToast("Save location set to '$folderName'"))
            } else {
                _eventFlow.emit(SettingsEvent.ShowToast("Failed to acquire SAF folder permissions"))
            }
        }
    }

    fun resetSafFolder() {
        viewModelScope.launch {
            settingsRepository.updateSafFolder(null, null)
            _eventFlow.emit(SettingsEvent.ShowToast("Reset save location to default (Pictures/CamCraft)"))
        }
    }

    fun setImageFormat(format: ImageFormat) {
        viewModelScope.launch { settingsRepository.updateImageFormat(format) }
    }

    fun setJpegQuality(quality: Int) {
        viewModelScope.launch { settingsRepository.updateJpegQuality(quality) }
    }

    fun setCaptureQualityMode(mode: Int) {
        viewModelScope.launch { settingsRepository.updateCaptureQualityMode(mode) }
    }

    fun setTimerDuration(seconds: Int) {
        viewModelScope.launch { settingsRepository.updateTimerDuration(seconds) }
    }

    fun setGridMode(gridMode: GridMode) {
        viewModelScope.launch { settingsRepository.updateGridMode(gridMode) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { settingsRepository.updateThemeMode(themeMode) }
    }

    fun setGeotagging(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateGeotagging(enabled) }
    }

    fun setBurstMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateBurstMode(enabled) }
    }
}
