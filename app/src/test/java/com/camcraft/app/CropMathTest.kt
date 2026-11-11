package com.camcraft.app

import org.junit.Assert.assertEquals
import org.junit.Test

data class TestRect(val left: Float, val top: Float, val right: Float, val bottom: Float)

class CropMathTest {

    @Test
    fun testNormalizedCropToPixelCoordinates() {
        val bitmapWidth = 4000
        val bitmapHeight = 3000
        val normRect = TestRect(0.25f, 0.25f, 0.75f, 0.75f)

        val cropLeft = (normRect.left * bitmapWidth).toInt()
        val cropTop = (normRect.top * bitmapHeight).toInt()
        val cropRight = (normRect.right * bitmapWidth).toInt()
        val cropBottom = (normRect.bottom * bitmapHeight).toInt()

        assertEquals(1000, cropLeft)
        assertEquals(750, cropTop)
        assertEquals(3000, cropRight)
        assertEquals(2250, cropBottom)
    }

    @Test
    fun testSquareAspectRatioConstraint() {
        val targetRatio = 1.0f
        var currentWidth = 1600f
        var currentHeight = 900f
        val aspect = currentWidth / currentHeight

        if (aspect > targetRatio) {
            currentWidth = currentHeight * targetRatio
        } else {
            currentHeight = currentWidth / targetRatio
        }

        assertEquals(900f, currentWidth, 0.01f)
        assertEquals(900f, currentHeight, 0.01f)
        assertEquals(1.0f, currentWidth / currentHeight, 0.01f)
    }
}
