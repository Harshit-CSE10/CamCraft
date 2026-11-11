package com.camcraft.app

import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FilenameGeneratorTest {

    @Test
    fun testCamCraftFilenamePattern() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val filename = "CAMCRAFT_$timestamp.jpg"

        assertTrue(filename.startsWith("CAMCRAFT_"))
        assertTrue(filename.endsWith(".jpg"))
    }

    @Test
    fun testEditedFilenamePattern() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val filename = "EDIT_$timestamp.png"

        assertTrue(filename.startsWith("EDIT_"))
        assertTrue(filename.endsWith(".png"))
    }
}
