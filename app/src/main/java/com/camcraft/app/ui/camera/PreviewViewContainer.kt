package com.camcraft.app.ui.camera

import android.view.ViewGroup
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.camcraft.app.ui.theme.WarmAccent
import kotlinx.coroutines.launch

@Composable
fun PreviewViewContainer(
    onPreviewViewCreated: (PreviewView) -> Unit,
    onTapToFocus: (PreviewView, Float, Float) -> Unit,
    onZoomChanged: (Float) -> Unit,
    currentZoomRatio: Float,
    modifier: Modifier = Modifier
) {
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val focusRingScale = remember { Animatable(1.5f) }
    val focusRingAlpha = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    if (zoom != 1f) {
                        onZoomChanged(currentZoomRatio * zoom)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    previewViewRef?.let { pView ->
                        focusPoint = offset
                        onTapToFocus(pView, offset.x, offset.y)
                        coroutineScope.launch {
                            focusRingScale.snapTo(1.6f)
                            focusRingAlpha.snapTo(1f)
                            launch { focusRingScale.animateTo(1.0f, tween(300)) }
                            launch { focusRingAlpha.animateTo(0.0f, tween(800)) }
                        }
                    }
                }
            }
    ) {
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    onPreviewViewCreated(this)
                    previewViewRef = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Focus Ring Animation
        focusPoint?.let { point ->
            if (focusRingAlpha.value > 0.01f) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = WarmAccent.copy(alpha = focusRingAlpha.value),
                        radius = 36.dp.toPx() * focusRingScale.value,
                        center = point,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }
    }
}
