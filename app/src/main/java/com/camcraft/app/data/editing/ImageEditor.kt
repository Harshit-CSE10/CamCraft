package com.camcraft.app.data.editing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.camcraft.app.data.settings.ImageFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageEditor(private val context: Context) {

    suspend fun loadPreviewBitmap(uri: Uri, maxDimension: Int = 1280): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var sampleSize = 1
            while ((options.outWidth / sampleSize) > maxDimension || (options.outHeight / sampleSize) > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }

            // Apply EXIF rotation if necessary for preview
            val orientation = getExifOrientation(uri)
            if (orientation != 0 && bitmap != null) {
                bitmap = rotateBitmap(bitmap, orientation)
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExifOrientation(uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) bitmap.recycle()
        return rotated
    }

    suspend fun saveEditedCopy(
        sourceUri: Uri,
        cropRectNormalized: RectF, // Normalized 0f..1f relative to original source
        rotationDegrees: Int,
        format: ImageFormat = ImageFormat.JPEG,
        quality: Int = 95
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fullBitmap = loadFullBitmapSafe(sourceUri) ?: return@withContext null

            // 1. Apply total rotation (EXIF orientation + user rotation)
            val initialExifRot = getExifOrientation(sourceUri)
            val totalRotation = (initialExifRot + rotationDegrees) % 360

            var processedBitmap = if (totalRotation != 0) {
                rotateBitmap(fullBitmap, totalRotation)
            } else {
                fullBitmap
            }

            // 2. Apply Crop if cropRectNormalized is valid
            if (cropRectNormalized.left > 0f || cropRectNormalized.top > 0f || cropRectNormalized.right < 1f || cropRectNormalized.bottom < 1f) {
                val cropLeft = (cropRectNormalized.left * processedBitmap.width).toInt().coerceIn(0, processedBitmap.width - 1)
                val cropTop = (cropRectNormalized.top * processedBitmap.height).toInt().coerceIn(0, processedBitmap.height - 1)
                val cropRight = (cropRectNormalized.right * processedBitmap.width).toInt().coerceIn(cropLeft + 1, processedBitmap.width)
                val cropBottom = (cropRectNormalized.bottom * processedBitmap.height).toInt().coerceIn(cropTop + 1, processedBitmap.height)

                val croppedWidth = cropRight - cropLeft
                val croppedHeight = cropBottom - cropTop

                val croppedBitmap = Bitmap.createBitmap(processedBitmap, cropLeft, cropTop, croppedWidth, croppedHeight)
                if (croppedBitmap != processedBitmap) {
                    processedBitmap.recycle()
                    processedBitmap = croppedBitmap
                }
            }

            // 3. Save into MediaStore as a new copy
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val filename = "EDIT_$timestamp.${format.extension}"

            val compressFormat = when (format) {
                ImageFormat.PNG -> Bitmap.CompressFormat.PNG
                ImageFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }

            val savedUri: Uri?
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, format.mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/CamCraft")
                    put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        processedBitmap.compress(compressFormat, quality, outputStream)
                    }

                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)

                    copyExifTags(sourceUri, uri, processedBitmap.width, processedBitmap.height)
                }
                savedUri = uri
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dir = File(picturesDir, "CamCraft")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)

                FileOutputStream(file).use { outputStream ->
                    processedBitmap.compress(compressFormat, quality, outputStream)
                }

                savedUri = Uri.fromFile(file)
                copyExifTags(sourceUri, savedUri, processedBitmap.width, processedBitmap.height)
            }

            processedBitmap.recycle()
            savedUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun loadFullBitmapSafe(uri: Uri): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        var sampleSize = 1
        // Cap max memory footprint to ~4000px on side to prevent OOM
        while ((options.outWidth / sampleSize) > 4096 || (options.outHeight / sampleSize) > 4096) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOptions)
        }
    }

    private fun copyExifTags(sourceUri: Uri, targetUri: Uri, width: Int, height: Int) {
        try {
            val sourceInput = context.contentResolver.openInputStream(sourceUri) ?: return
            val sourceExif = ExifInterface(sourceInput)
            sourceInput.close()

            val pfd = context.contentResolver.openFileDescriptor(targetUri, "rw") ?: return
            val targetExif = ExifInterface(pfd.fileDescriptor)

            val tagsToCopy = arrayOf(
                ExifInterface.TAG_MAKE,
                ExifInterface.TAG_MODEL,
                ExifInterface.TAG_EXPOSURE_TIME,
                ExifInterface.TAG_F_NUMBER,
                ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                ExifInterface.TAG_FOCAL_LENGTH,
                ExifInterface.TAG_FLASH,
                ExifInterface.TAG_GPS_LATITUDE,
                ExifInterface.TAG_GPS_LATITUDE_REF,
                ExifInterface.TAG_GPS_LONGITUDE,
                ExifInterface.TAG_GPS_LONGITUDE_REF
            )

            for (tag in tagsToCopy) {
                sourceExif.getAttribute(tag)?.let { value ->
                    targetExif.setAttribute(tag, value)
                }
            }

            targetExif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, width.toString())
            targetExif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, height.toString())
            targetExif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL.toString())
            targetExif.saveAttributes()
            pfd.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
