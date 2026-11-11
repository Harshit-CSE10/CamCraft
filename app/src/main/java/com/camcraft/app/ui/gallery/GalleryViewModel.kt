package com.camcraft.app.ui.gallery

import android.app.Application
import android.content.IntentSender
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.app.data.media.DeleteResult
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.data.media.MediaRepository
import com.camcraft.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class GalleryEvent {
    data class ShowToast(val message: String) : GalleryEvent()
    data class LaunchIntentSender(val intentSender: IntentSender) : GalleryEvent()
}

data class GalleryUiState(
    val photos: List<MediaItem> = emptyList(),
    val isLoading: Boolean = true,
    val selectedPhotoIndex: Int = 0,
    val photoToDelete: MediaItem? = null
)

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val mediaRepository = MediaRepository(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<GalleryEvent>()
    val eventFlow: SharedFlow<GalleryEvent> = _eventFlow.asSharedFlow()

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val prefs = settingsRepository.userPreferencesFlow.first()
            val photos = mediaRepository.getAllPhotos(prefs)
            _uiState.value = _uiState.value.copy(
                photos = photos,
                isLoading = false
            )
        }
    }

    fun requestDeletePhoto(item: MediaItem) {
        _uiState.value = _uiState.value.copy(photoToDelete = item)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(photoToDelete = null)
    }

    fun confirmDeletePhoto(item: MediaItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(photoToDelete = null)
            when (val result = mediaRepository.deletePhoto(item)) {
                is DeleteResult.Success -> {
                    _eventFlow.emit(GalleryEvent.ShowToast("Photo deleted"))
                    loadPhotos()
                }
                is DeleteResult.RequiresPermission -> {
                    _eventFlow.emit(GalleryEvent.LaunchIntentSender(result.intentSender))
                }
                is DeleteResult.Error -> {
                    _eventFlow.emit(GalleryEvent.ShowToast(result.message))
                }
            }
        }
    }

    fun onIntentSenderResult(success: Boolean) {
        if (success) {
            viewModelScope.launch {
                _eventFlow.emit(GalleryEvent.ShowToast("Photo deleted"))
                loadPhotos()
            }
        } else {
            viewModelScope.launch {
                _eventFlow.emit(GalleryEvent.ShowToast("Delete operation cancelled"))
            }
        }
    }
}
