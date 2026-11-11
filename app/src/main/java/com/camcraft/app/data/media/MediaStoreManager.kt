package com.camcraft.app.data.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MediaStoreManager(private val context: Context) {

    companion object {
        const val RELATIVE_PATH_DEFAULT = "Pictures/CamCraft"
        const val FOLDER_NAME = "CamCraft"
    }

    private val contentResolver = context.contentResolver

    fun createDefaultOutputFileOptions(
        location: android.location.Location? = null
    ): ImageCapture.OutputFileOptions {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val displayName = "CAMCRAFT_$timestamp.jpg"

        val metadata = ImageCapture.Metadata().apply {
            if (location != null) {
                this.location = location
            }
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH_DEFAULT)
                put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
                .setMetadata(metadata)
                .build()
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val camcraftDir = File(picturesDir, FOLDER_NAME)
            if (!camcraftDir.exists()) {
                camcraftDir.mkdirs()
            }
            val file = File(camcraftDir, displayName)
            ImageCapture.OutputFileOptions.Builder(file)
                .setMetadata(metadata)
                .build()
        }
    }

    suspend fun verifyAndPublishMedia(savedUri: Uri): Uri = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            contentResolver.update(savedUri, contentValues, null, null)
        } else {
            val path = getRealPathFromUri(savedUri)
            if (path != null) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(path),
                        arrayOf("image/jpeg")
                    ) { _, _ ->
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            }
        }
        savedUri
    }

    fun getRealPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        return try {
            contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (cursor.moveToFirst()) cursor.getString(columnIndex) else null
            }
        } catch (e: Exception) {
            uri.path
        }
    }

    suspend fun queryCamCraftPhotos(): List<MediaItem> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<MediaItem>()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.MIME_TYPE
        )

        val selection: String?
        val selectionArgs: Array<String>?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            selectionArgs = arrayOf("%$RELATIVE_PATH_DEFAULT%")
        } else {
            selection = "${MediaStore.Images.Media.DATA} LIKE ?"
            selectionArgs = arrayOf("%/$FOLDER_NAME/%")
        }

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = Uri.withAppendedPath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id.toString()
                    )
                    photos.add(
                        MediaItem(
                            id = id,
                            uri = contentUri,
                            displayName = cursor.getString(nameColumn) ?: "Photo_$id.jpg",
                            dateTaken = cursor.getLong(dateColumn),
                            size = cursor.getLong(sizeColumn),
                            width = cursor.getInt(widthColumn),
                            height = cursor.getInt(heightColumn),
                            mimeType = cursor.getString(mimeColumn) ?: "image/jpeg",
                            isSafFile = false,
                            relativePath = RELATIVE_PATH_DEFAULT
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        photos
    }
}
