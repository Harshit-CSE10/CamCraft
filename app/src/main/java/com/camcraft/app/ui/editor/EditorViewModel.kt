package com.camcraft.app.ui.editor

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camcraft.app.data.editing.ImageEditor
import com.camcraft.app.data.settings.ImageFormat
import com.camcraft.app.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed class EditorEvent {
    data class ShowToast(val message: String) : EditorEvent()
    data class SaveCompleted(val newUri: Uri) : EditorEvent()
}

data class EditorUiState(
    val previewBitmap: Bitmap? = null,
    val isLoadingPreview: Boolean = true,
    val rotationDegrees: Int = 0,
    val cropRectNormalized: Rect = Rect(0f, 0f, 1f, 1f),
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.FREE,
    val isSaving: Boolean = false
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val imageEditor = ImageEditor(application)
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<EditorEvent>()
    val eventFlow: SharedFlow<EditorEvent> = _eventFlow.asSharedFlow()

    private var sourceUri: Uri? = null

    fun loadSourceImage(uri: Uri) {
        sourceUri = uri
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingPreview = true)
            val bitmap = imageEditor.loadPreviewBitmap(uri)
            _uiState.value = _uiState.value.copy(
                previewBitmap = bitmap,
                isLoadingPreview = false
            )
        }
    }

    fun rotate90Degrees() {
        val nextRotation = (_uiState.value.rotationDegrees + 90) % 360
        _uiState.value = _uiState.value.copy(rotationDegrees = nextRotation)
    }

    fun setAspectRatioMode(mode: AspectRatioMode) {
        _uiState.value = _uiState.value.copy(
            aspectRatioMode = mode,
            cropRectNormalized = Rect(0f, 0f, 1f, 1f) // Reset crop box for new ratio
        )
    }

    fun updateCropRect(rect: Rect) {
        _uiState.value = _uiState.value.copy(cropRectNormalized = rect)
    }

    fun resetEdits() {
        _uiState.value = _uiState.value.copy(
            rotationDegrees = 0,
            cropRectNormalized = Rect(0f, 0f, 1f, 1f),
            aspectRatioMode = AspectRatioMode.FREE
        )
    }

    fun saveEditedCopy() {
        val uri = sourceUri ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            val prefs = settingsRepository.userPreferencesFlow.first()
            val normRect = _uiState.value.cropRectNormalized
            val rectF = RectF(normRect.left, normRect.top, normRect.right, normRect.bottom)

            val newUri = imageEditor.saveEditedCopy(
                sourceUri = uri,
                cropRectNormalized = rectF,
                rotationDegrees = _uiState.value.rotationDegrees,
                format = prefs.imageFormat,
                quality = prefs.jpegQuality
            )

            _uiState.value = _uiState.value.copy(isSaving = false)

            if (newUri != null) {
                _eventFlow.emit(EditorEvent.ShowToast("Saved edited copy"))
                _eventFlow.emit(EditorEvent.SaveCompleted(newUri))
            } else {
                _eventFlow.emit(EditorEvent.ShowToast("Failed to save edited copy"))
            }
        }
    }
}
