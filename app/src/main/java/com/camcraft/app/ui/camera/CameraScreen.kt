package com.camcraft.app.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import com.camcraft.app.data.camera.CameraEvent
import com.camcraft.app.data.camera.CameraViewModel
import com.camcraft.app.ui.components.CameraPermissionDeniedContent
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    val cameraState by viewModel.cameraState.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val latestPhoto by viewModel.latestPhoto.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Camera permission is required to take photos", Toast.LENGTH_LONG).show()
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(cameraState.lensFacing, cameraState.flashMode) {
        previewViewRef?.let { pView ->
            val rotation = pView.display?.rotation ?: android.view.Surface.ROTATION_0
            viewModel.bindCamera(lifecycleOwner, pView, rotation)
        }
    }

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is CameraEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                CameraEvent.PhotoCaptured -> {
                    // Shutter capture feedback
                }
            }
        }
    }

    if (!hasCameraPermission) {
        CameraPermissionDeniedContent(
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // CameraX Preview Surface
            PreviewViewContainer(
                onPreviewViewCreated = { previewView ->
                    previewViewRef = previewView
                    val rotation = previewView.display?.rotation ?: android.view.Surface.ROTATION_0
                    coroutineScope.launch {
                        viewModel.bindCamera(lifecycleOwner, previewView, rotation)
                    }
                },
                onTapToFocus = { previewView, x, y ->
                    viewModel.tapToFocus(previewView, x, y)
                },
                onZoomChanged = { newZoom ->
                    viewModel.setZoomRatio(newZoom)
                },
                currentZoomRatio = cameraState.zoomRatio
            )

            // Grid Overlay
            GridOverlay(gridMode = userPreferences.gridMode)

            // Top Bar Controls
            CameraTopBar(
                hasFlashUnit = cameraState.hasFlashUnit,
                flashMode = cameraState.flashMode,
                onToggleFlash = { viewModel.toggleFlashMode() },
                timerDuration = userPreferences.timerDurationSeconds,
                onCycleTimer = { viewModel.cycleTimerDuration() },
                gridMode = userPreferences.gridMode,
                onCycleGrid = { viewModel.cycleGridMode() },
                onNavigateToSettings = onNavigateToSettings,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            // Bottom Bar Controls
            CameraBottomBar(
                latestPhoto = latestPhoto,
                onOpenGallery = onNavigateToGallery,
                isCapturing = cameraState.isCapturing,
                onShutterClick = { viewModel.triggerPhotoCapture() },
                hasMultipleCameras = cameraState.hasBackCamera && cameraState.hasFrontCamera,
                onSwitchCamera = { viewModel.toggleCameraLens() },
                currentZoomRatio = cameraState.zoomRatio,
                onZoomClick = { viewModel.cycleZoomPreset() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Timer Countdown Overlay
            cameraState.timerSecondsRemaining?.let { seconds ->
                TimerOverlay(
                    secondsRemaining = seconds,
                    onCancel = { viewModel.cancelTimer() }
                )
            }
        }
    }
}
