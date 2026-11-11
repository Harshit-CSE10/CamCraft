package com.camcraft.app.data.camera

import android.app.Application
import android.location.Location
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.PreviewView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.data.media.MediaRepository
import com.camcraft.app.data.settings.GridMode
import com.camcraft.app.data.settings.SettingsRepository
import com.camcraft.app.data.settings.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class CameraEvent {
    data class ShowToast(val message: String) : CameraEvent()
    object PhotoCaptured : CameraEvent()
}

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val cameraXManager = CameraXManager(application)
    val mediaRepository = MediaRepository(application)
    val settingsRepository = SettingsRepository(application)

    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> = _cameraState.asStateFlow()

    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private val _latestPhoto = MutableStateFlow<MediaItem?>(null)
    val latestPhoto: StateFlow<MediaItem?> = _latestPhoto.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CameraEvent>()
    val eventFlow: SharedFlow<CameraEvent> = _eventFlow.asSharedFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val (hasBack, hasFront) = cameraXManager.checkCameraAvailability()
            val initialLens = if (hasBack) CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
            _cameraState.value = _cameraState.value.copy(
                hasBackCamera = hasBack,
                hasFrontCamera = hasFront,
                lensFacing = initialLens
            )

            settingsRepository.userPreferencesFlow.collect { prefs ->
                _userPreferences.value = prefs
                loadLatestPhoto()
            }
        }
    }

    fun loadLatestPhoto() {
        viewModelScope.launch {
            val photo = mediaRepository.getLatestPhoto(_userPreferences.value)
            _latestPhoto.value = photo
        }
    }

    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        targetRotation: Int
    ) {
        val state = _cameraState.value
        val prefs = _userPreferences.value
        val boundCamera = cameraXManager.bindCamera(
            lifecycleOwner = lifecycleOwner,
            previewView = previewView,
            lensFacing = state.lensFacing,
            flashMode = state.flashMode,
            captureMode = prefs.captureQualityMode,
            targetRotation = targetRotation
        )
        val hasFlash = cameraXManager.hasFlashUnit()
        _cameraState.value = _cameraState.value.copy(
            hasFlashUnit = hasFlash,
            flashMode = if (hasFlash) state.flashMode else ImageCapture.FLASH_MODE_OFF
        )
    }

    fun toggleCameraLens() {
        val current = _cameraState.value.lensFacing
        val next = if (current == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        _cameraState.value = _cameraState.value.copy(lensFacing = next)
    }

    fun toggleFlashMode() {
        if (!_cameraState.value.hasFlashUnit) return
        val nextFlash = when (_cameraState.value.flashMode) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        _cameraState.value = _cameraState.value.copy(flashMode = nextFlash)
        cameraXManager.setFlashMode(nextFlash)
    }

    fun cycleGridMode() {
        viewModelScope.launch {
            val current = _userPreferences.value.gridMode
            val next = when (current) {
                GridMode.NONE -> GridMode.RULE_OF_THIRDS
                GridMode.RULE_OF_THIRDS -> GridMode.GRID_4X4
                GridMode.GRID_4X4 -> GridMode.GOLDEN_RATIO
                GridMode.GOLDEN_RATIO -> GridMode.NONE
            }
            settingsRepository.updateGridMode(next)
        }
    }

    fun cycleTimerDuration() {
        viewModelScope.launch {
            val current = _userPreferences.value.timerDurationSeconds
            val next = when (current) {
                0 -> 3
                3 -> 5
                5 -> 10
                else -> 0
            }
            settingsRepository.updateTimerDuration(next)
        }
    }

    fun cycleZoomPreset() {
        val current = _cameraState.value.zoomRatio
        val next = if (current < 1.9f) 2.0f else if (current < 4.9f) 5.0f else 1.0f
        setZoomRatio(next)
    }

    fun setZoomRatio(ratio: Float) {
        val clamped = ratio.coerceIn(_cameraState.value.minZoomRatio, _cameraState.value.maxZoomRatio)
        _cameraState.value = _cameraState.value.copy(zoomRatio = clamped)
        cameraXManager.setZoomRatio(clamped)
    }

    fun tapToFocus(previewView: PreviewView, x: Float, y: Float) {
        cameraXManager.tapToFocus(previewView, x, y)
    }

    fun triggerPhotoCapture(location: Location? = null) {
        val timerSec = _userPreferences.value.timerDurationSeconds
        if (timerSec > 0) {
            startTimerCountdown(timerSec, location)
        } else {
            executePhotoCapture(location)
        }
    }

    private fun startTimerCountdown(seconds: Int, location: Location?) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _cameraState.value = _cameraState.value.copy(timerSecondsRemaining = i)
                delay(1000)
            }
            _cameraState.value = _cameraState.value.copy(timerSecondsRemaining = null)
            executePhotoCapture(location)
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _cameraState.value = _cameraState.value.copy(timerSecondsRemaining = null)
    }

    private fun executePhotoCapture(location: Location?) {
        if (_cameraState.value.isCapturing) return
        _cameraState.value = _cameraState.value.copy(isCapturing = true)

        viewModelScope.launch {
            val prefs = _userPreferences.value
            val safUriString = prefs.customSafUri

            if (!safUriString.isNullOrBlank()) {
                if (!mediaRepository.safManager.isUriPermissionValid(safUriString)) {
                    settingsRepository.updateSafFolder(null, null)
                    _eventFlow.emit(CameraEvent.ShowToast("Custom folder inaccessible. Saved to Pictures/CamCraft"))
                }
            }

            val outputFileOptions = mediaRepository.mediaStoreManager.createDefaultOutputFileOptions(location)

            cameraXManager.capturePhoto(
                outputFileOptions = outputFileOptions,
                onImageSaved = { results ->
                    viewModelScope.launch {
                        val savedUri = results.savedUri ?: run {
                            _cameraState.value = _cameraState.value.copy(isCapturing = false)
                            _eventFlow.emit(CameraEvent.ShowToast("Capture failed: Null URI returned"))
                            return@launch
                        }

                        mediaRepository.mediaStoreManager.verifyAndPublishMedia(savedUri)

                        // If SAF active, also save to SAF folder
                        val activeSaf = _userPreferences.value.customSafUri
                        if (!activeSaf.isNullOrBlank() && mediaRepository.safManager.isUriPermissionValid(activeSaf)) {
                            getApplication<Application>().contentResolver.openInputStream(savedUri)?.use { input ->
                                mediaRepository.safManager.saveToSafFolder(activeSaf, input)
                            }
                        }

                        _cameraState.value = _cameraState.value.copy(isCapturing = false)
                        loadLatestPhoto()
                        _eventFlow.emit(CameraEvent.PhotoCaptured)
                        _eventFlow.emit(CameraEvent.ShowToast("Photo saved successfully"))
                    }
                },
                onError = { exception ->
                    viewModelScope.launch {
                        _cameraState.value = _cameraState.value.copy(isCapturing = false)
                        _eventFlow.emit(CameraEvent.ShowToast("Capture Error: ${exception.localizedMessage}"))
                    }
                }
            )
        }
    }
}
