package com.camcraft.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ExifOrientationTest {

    @Test
    fun testRotationDegreesNormalization() {
        val initialExifRot = 90
        val userRot = 90
        val total = (initialExifRot + userRot) % 360
        assertEquals(180, total)
    }

    @Test
    fun test360DegreeWrapAround() {
        val initialExifRot = 270
        val userRot = 180
        val total = (initialExifRot + userRot) % 360
        assertEquals(90, total)
    }
}
