package com.camcraft.app.data.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture

data class CameraState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val flashMode: Int = ImageCapture.FLASH_MODE_OFF,
    val hasFlashUnit: Boolean = false,
    val hasFrontCamera: Boolean = true,
    val hasBackCamera: Boolean = true,
    val isCapturing: Boolean = false,
    val zoomLinear: Float = 0f,
    val zoomRatio: Float = 1f,
    val minZoomRatio: Float = 1f,
    val maxZoomRatio: Float = 5f,
    val timerSecondsRemaining: Int? = null,
    val burstProgress: Pair<Int, Int>? = null // e.g. 3 of 10
)
