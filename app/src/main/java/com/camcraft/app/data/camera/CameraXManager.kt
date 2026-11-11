package com.camcraft.app.data.camera

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.Surface
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraXManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    var imageCapture: ImageCapture? = null
        private set
    var preview: Preview? = null
        private set

    suspend fun getCameraProvider(): ProcessCameraProvider = withContext(Dispatchers.Main) {
        cameraProvider?.let { return@withContext it }
        suspendCancellableCoroutine { continuation ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    val provider = future.get()
                    cameraProvider = provider
                    continuation.resume(provider)
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    }

    suspend fun checkCameraAvailability(): Pair<Boolean, Boolean> = withContext(Dispatchers.Main) {
        val provider = getCameraProvider()
        val hasBack = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
        val hasFront = provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
        Pair(hasBack, hasFront)
    }

    suspend fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int,
        flashMode: Int,
        captureMode: Int,
        targetRotation: Int
    ): Camera = withContext(Dispatchers.Main) {
        val provider = getCameraProvider()
        provider.unbindAll()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        preview = Preview.Builder()
            .setTargetRotation(targetRotation)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(captureMode)
            .setFlashMode(flashMode)
            .setTargetRotation(targetRotation)
            .build()

        val boundCamera = provider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        camera = boundCamera
        boundCamera
    }

    fun setFlashMode(flashMode: Int) {
        imageCapture?.flashMode = flashMode
    }

    fun hasFlashUnit(): Boolean {
        return camera?.cameraInfo?.hasFlashUnit() ?: false
    }

    fun tapToFocus(previewView: PreviewView, x: Float, y: Float) {
        val boundCamera = camera ?: return
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
            .setAutoCancelDuration(3, TimeUnit.SECONDS)
            .build()
        boundCamera.cameraControl.startFocusAndMetering(action)
    }

    fun setZoomRatio(ratio: Float) {
        val boundCamera = camera ?: return
        val minRatio = boundCamera.cameraInfo.zoomState.value?.minZoomRatio ?: 1f
        val maxRatio = boundCamera.cameraInfo.zoomState.value?.maxZoomRatio ?: 5f
        val clamped = ratio.coerceIn(minRatio, maxRatio)
        boundCamera.cameraControl.setZoomRatio(clamped)
    }

    fun capturePhoto(
        outputFileOptions: ImageCapture.OutputFileOptions,
        onImageSaved: (ImageCapture.OutputFileResults) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(ImageCaptureException(ImageCapture.ERROR_CAMERA_CLOSED, "Camera not initialized", null))
            return
        }

        capture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onImageSaved(outputFileResults)
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }
}
