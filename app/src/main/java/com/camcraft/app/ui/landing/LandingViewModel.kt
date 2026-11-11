package com.camcraft.app.ui.landing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.data.media.MediaRepository
import com.camcraft.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LandingUiState(
    val latestPhoto: MediaItem? = null,
    val totalPhotosCount: Int = 0,
    val isLoading: Boolean = true
)

class LandingViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(LandingUiState())
    val uiState: StateFlow<LandingUiState> = _uiState.asStateFlow()

    fun loadLandingData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prefs = settingsRepository.userPreferencesFlow.first()
            val photos = mediaRepository.getAllPhotos(prefs)
            _uiState.value = LandingUiState(
                latestPhoto = photos.firstOrNull(),
                totalPhotosCount = photos.size,
                isLoading = false
            )
        }
    }
}
