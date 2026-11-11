package com.camcraft.app.ui.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.camcraft.app.data.settings.GridMode
import com.camcraft.app.ui.theme.CameraGridLine

@Composable
fun GridOverlay(
    gridMode: GridMode,
    modifier: Modifier = Modifier
) {
    if (gridMode == GridMode.NONE) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val strokeWidth = 1.dp.toPx()

        when (gridMode) {
            GridMode.RULE_OF_THIRDS -> {
                drawLine(CameraGridLine, Offset(width / 3f, 0f), Offset(width / 3f, height), strokeWidth)
                drawLine(CameraGridLine, Offset(2 * width / 3f, 0f), Offset(2 * width / 3f, height), strokeWidth)
                drawLine(CameraGridLine, Offset(0f, height / 3f), Offset(width, height / 3f), strokeWidth)
                drawLine(CameraGridLine, Offset(0f, 2 * height / 3f), Offset(width, 2 * height / 3f), strokeWidth)
            }
            GridMode.GRID_4X4 -> {
                for (i in 1..3) {
                    val x = width * (i / 4f)
                    val y = height * (i / 4f)
                    drawLine(CameraGridLine, Offset(x, 0f), Offset(x, height), strokeWidth)
                    drawLine(CameraGridLine, Offset(0f, y), Offset(width, y), strokeWidth)
                }
            }
            GridMode.GOLDEN_RATIO -> {
                val x1 = width * 0.382f
                val x2 = width * 0.618f
                val y1 = height * 0.382f
                val y2 = height * 0.618f

                drawLine(CameraGridLine, Offset(x1, 0f), Offset(x1, height), strokeWidth)
                drawLine(CameraGridLine, Offset(x2, 0f), Offset(x2, height), strokeWidth)
                drawLine(CameraGridLine, Offset(0f, y1), Offset(width, y1), strokeWidth)
                drawLine(CameraGridLine, Offset(0f, y2), Offset(width, y2), strokeWidth)
            }
            GridMode.NONE -> {}
        }
    }
}
