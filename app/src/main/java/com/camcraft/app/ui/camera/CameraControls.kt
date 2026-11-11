package com.camcraft.app.ui.camera

import androidx.camera.core.ImageCapture
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Timer10
import androidx.compose.material.icons.filled.Timer3
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.data.settings.GridMode
import com.camcraft.app.ui.theme.ShutterRed
import com.camcraft.app.ui.theme.WarmAccent

@Composable
fun CameraTopBar(
    hasFlashUnit: Boolean,
    flashMode: Int,
    onToggleFlash: () -> Unit,
    timerDuration: Int,
    onCycleTimer: () -> Unit,
    gridMode: GridMode,
    onCycleGrid: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.Black.copy(alpha = 0.4f),
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode Button
            if (hasFlashUnit) {
                IconButton(onClick = onToggleFlash) {
                    Icon(
                        imageVector = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        },
                        contentDescription = "Flash Mode",
                        tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) WarmAccent else Color.White
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Timer Toggle Button
            IconButton(onClick = onCycleTimer) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (timerDuration) {
                            3 -> Icons.Default.Timer3
                            10 -> Icons.Default.Timer10
                            else -> Icons.Default.Timer
                        },
                        contentDescription = "Timer",
                        tint = if (timerDuration > 0) WarmAccent else Color.White
                    )
                    if (timerDuration > 0) {
                        Text(
                            text = "${timerDuration}s",
                            color = WarmAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Grid Mode Button
            IconButton(onClick = onCycleGrid) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Grid",
                    tint = if (gridMode != GridMode.NONE) WarmAccent else Color.White
                )
            }

            // Settings Button
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun CameraBottomBar(
    latestPhoto: MediaItem?,
    onOpenGallery: () -> Unit,
    isCapturing: Boolean,
    onShutterClick: () -> Unit,
    hasMultipleCameras: Boolean,
    onSwitchCamera: () -> Unit,
    currentZoomRatio: Float,
    onZoomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shutterScale by animateFloatAsState(targetValue = if (isCapturing) 0.85f else 1.0f, label = "ShutterAnim")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zoom Level Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.clickable { onZoomClick() }
        ) {
            Text(
                text = String.format("%.1fx", currentZoomRatio),
                color = WarmAccent,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Latest Photo Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .border(2.dp, WarmAccent, CircleShape)
                    .clickable { onOpenGallery() },
                contentAlignment = Alignment.Center
            ) {
                if (latestPhoto != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(latestPhoto.uri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Latest Photo Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF222222))
                    )
                }
            }

            // Main Shutter Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(shutterScale)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(4.dp, WarmAccent, CircleShape)
                    .clickable(enabled = !isCapturing) { onShutterClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isCapturing) {
                    CircularProgressIndicator(
                        color = ShutterRed,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(68.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(ShutterRed)
                    )
                }
            }

            // Front / Back Camera Switch Button
            if (hasMultipleCameras) {
                IconButton(
                    onClick = onSwitchCamera,
                    enabled = !isCapturing,
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(56.dp))
            }
        }
    }
}
