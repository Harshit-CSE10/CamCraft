package com.camcraft.app.data.editing

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream
import java.text.DecimalFormat

data class PhotoExifData(
    val make: String? = null,
    val model: String? = null,
    val dateTaken: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val iso: String? = null,
    val exposureTime: String? = null,
    val aperture: String? = null,
    val focalLength: String? = null,
    val flash: String? = null,
    val orientation: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

object ExifUtils {

    fun readExifData(context: Context, uri: Uri): PhotoExifData {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val exif = ExifInterface(inputStream)
                val make = exif.getAttribute(ExifInterface.TAG_MAKE)
                val model = exif.getAttribute(ExifInterface.TAG_MODEL)
                val date = exif.getAttribute(ExifInterface.TAG_DATETIME)
                val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0)
                val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0)
                val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                val exposure = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { formatExposure(it) }
                val aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" }
                val focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" }
                val flashInt = exif.getAttributeInt(ExifInterface.TAG_FLASH, -1)
                val flashStr = if (flashInt != -1) if (flashInt and 1 != 0) "Fired" else "Did not fire" else null
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

                val latLong = exif.latLong

                PhotoExifData(
                    make = make,
                    model = model,
                    dateTaken = date,
                    width = width,
                    height = height,
                    iso = iso,
                    exposureTime = exposure,
                    aperture = aperture,
                    focalLength = focalLength,
                    flash = flashStr,
                    orientation = orientation,
                    latitude = latLong?.get(0),
                    longitude = latLong?.get(1)
                )
            } ?: PhotoExifData()
        } catch (e: Exception) {
            e.printStackTrace()
            PhotoExifData()
        }
    }

    private fun formatExposure(exposureString: String): String {
        return try {
            val exp = exposureString.toDouble()
            if (exp < 1.0 && exp > 0) {
                "1/${DecimalFormat("#").format(1.0 / exp)}s"
            } else {
                "${exp}s"
            }
        } catch (e: Exception) {
            "${exposureString}s"
        }
    }
}
