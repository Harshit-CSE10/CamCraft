package com.camcraft.app.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.camcraft.app.ui.theme.WarmAccent

enum class AspectRatioMode(val label: String, val ratio: Float?) {
    FREE("Free", null),
    SQUARE("1:1", 1.0f),
    RATIO_4_3("4:3", 4f / 3f),
    RATIO_16_9("16:9", 16f / 9f),
    RATIO_3_2("3:2", 3f / 2f)
}

@Composable
fun CropOverlay(
    cropRectNormalized: androidx.compose.ui.geometry.Rect, // Normalized 0f..1f
    onCropRectChanged: (androidx.compose.ui.geometry.Rect) -> Unit,
    aspectRatioMode: AspectRatioMode,
    modifier: Modifier = Modifier
) {
    var activeHandle by remember { mutableStateOf<Handle?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(aspectRatioMode, cropRectNormalized) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        val rectPx = Rect(
                            cropRectNormalized.left * w,
                            cropRectNormalized.top * h,
                            cropRectNormalized.right * w,
                            cropRectNormalized.bottom * h
                        )
                        activeHandle = getHitHandle(offset, rectPx, touchRadius = 60.dp.toPx())
                    },
                    onDragEnd = { activeHandle = null },
                    onDragCancel = { activeHandle = null },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val handle = activeHandle ?: return@detectDragGestures
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()
                        if (w <= 0 || h <= 0) return@detectDragGestures

                        var left = cropRectNormalized.left * w
                        var top = cropRectNormalized.top * h
                        var right = cropRectNormalized.right * w
                        var bottom = cropRectNormalized.bottom * h

                        val dx = dragAmount.x
                        val dy = dragAmount.y

                        val minSizePx = 100.dp.toPx()

                        when (handle) {
                            Handle.TOP_LEFT -> {
                                left = (left + dx).coerceIn(0f, right - minSizePx)
                                top = (top + dy).coerceIn(0f, bottom - minSizePx)
                            }
                            Handle.TOP_RIGHT -> {
                                right = (right + dx).coerceIn(left + minSizePx, w)
                                top = (top + dy).coerceIn(0f, bottom - minSizePx)
                            }
                            Handle.BOTTOM_LEFT -> {
                                left = (left + dx).coerceIn(0f, right - minSizePx)
                                bottom = (bottom + dy).coerceIn(top + minSizePx, h)
                            }
                            Handle.BOTTOM_RIGHT -> {
                                right = (right + dx).coerceIn(left + minSizePx, w)
                                bottom = (bottom + dy).coerceIn(top + minSizePx, h)
                            }
                            Handle.CENTER -> {
                                val currentW = right - left
                                val currentH = bottom - top
                                val newLeft = (left + dx).coerceIn(0f, w - currentW)
                                val newTop = (top + dy).coerceIn(0f, h - currentH)
                                left = newLeft
                                top = newTop
                                right = newLeft + currentW
                                bottom = newTop + currentH
                            }
                        }

                        // Apply Aspect Ratio Constraint if enabled
                        aspectRatioMode.ratio?.let { targetRatio ->
                            var currentW = right - left
                            var currentH = bottom - top
                            val currentAspect = currentW / currentH

                            if (currentAspect > targetRatio) {
                                currentW = currentH * targetRatio
                            } else {
                                currentH = currentW / targetRatio
                            }

                            right = (left + currentW).coerceAtMost(w)
                            bottom = (top + currentH).coerceAtMost(h)
                        }

                        onCropRectChanged(
                            Rect(
                                (left / w).coerceIn(0f, 1f),
                                (top / h).coerceIn(0f, 1f),
                                (right / w).coerceIn(0f, 1f),
                                (bottom / h).coerceIn(0f, 1f)
                            )
                        )
                    }
                )
            }
    ) {
        val w = size.width
        val h = size.height
        val rectPx = Rect(
            cropRectNormalized.left * w,
            cropRectNormalized.top * h,
            cropRectNormalized.right * w,
            cropRectNormalized.bottom * h
        )

        // Dim background outside crop box
        drawRect(Color.Black.copy(alpha = 0.5f), size = Size(w, h))
        drawRect(Color.Transparent, topLeft = rectPx.topLeft, size = rectPx.size, blendMode = androidx.compose.ui.graphics.BlendMode.Clear)

        // Crop Border
        drawRect(WarmAccent, topLeft = rectPx.topLeft, size = rectPx.size, style = Stroke(width = 2.dp.toPx()))

        // Grid Lines (3x3 inside crop box)
        val boxW = rectPx.width
        val boxH = rectPx.height

        drawLine(Color.White.copy(alpha = 0.4f), Offset(rectPx.left + boxW / 3f, rectPx.top), Offset(rectPx.left + boxW / 3f, rectPx.bottom), strokeWidth = 1.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.4f), Offset(rectPx.left + 2 * boxW / 3f, rectPx.top), Offset(rectPx.left + 2 * boxW / 3f, rectPx.bottom), strokeWidth = 1.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.4f), Offset(rectPx.left, rectPx.top + boxH / 3f), Offset(rectPx.right, rectPx.top + boxH / 3f), strokeWidth = 1.dp.toPx())
        drawLine(Color.White.copy(alpha = 0.4f), Offset(rectPx.left, rectPx.top + 2 * boxH / 3f), Offset(rectPx.right, rectPx.top + 2 * boxH / 3f), strokeWidth = 1.dp.toPx())

        // Corner Handles
        val handleLen = 20.dp.toPx()
        val handleStroke = 4.dp.toPx()

        // Top Left
        drawLine(WarmAccent, rectPx.topLeft, Offset(rectPx.left + handleLen, rectPx.top), strokeWidth = handleStroke)
        drawLine(WarmAccent, rectPx.topLeft, Offset(rectPx.left, rectPx.top + handleLen), strokeWidth = handleStroke)

        // Top Right
        drawLine(WarmAccent, rectPx.topRight, Offset(rectPx.right - handleLen, rectPx.top), strokeWidth = handleStroke)
        drawLine(WarmAccent, rectPx.topRight, Offset(rectPx.right, rectPx.top + handleLen), strokeWidth = handleStroke)

        // Bottom Left
        drawLine(WarmAccent, rectPx.bottomLeft, Offset(rectPx.left + handleLen, rectPx.bottom), strokeWidth = handleStroke)
        drawLine(WarmAccent, rectPx.bottomLeft, Offset(rectPx.left, rectPx.bottom - handleLen), strokeWidth = handleStroke)

        // Bottom Right
        drawLine(WarmAccent, rectPx.bottomRight, Offset(rectPx.right - handleLen, rectPx.bottom), strokeWidth = handleStroke)
        drawLine(WarmAccent, rectPx.bottomRight, Offset(rectPx.right, rectPx.bottom - handleLen), strokeWidth = handleStroke)
    }
}

private enum class Handle {
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
}

private fun getHitHandle(point: Offset, rect: Rect, touchRadius: Float): Handle? {
    fun isNear(p1: Offset, p2: Offset) = (p1 - p2).getDistance() <= touchRadius

    return when {
        isNear(point, rect.topLeft) -> Handle.TOP_LEFT
        isNear(point, rect.topRight) -> Handle.TOP_RIGHT
        isNear(point, rect.bottomLeft) -> Handle.BOTTOM_LEFT
        isNear(point, rect.bottomRight) -> Handle.BOTTOM_RIGHT
        rect.contains(point) -> Handle.CENTER
        else -> null
    }
}
