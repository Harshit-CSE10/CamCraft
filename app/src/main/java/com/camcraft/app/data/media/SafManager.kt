package com.camcraft.app.data.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SafManager(private val context: Context) {

    fun persistUriPermission(treeUri: Uri): Boolean {
        return try {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isUriPermissionValid(treeUriString: String?): Boolean {
        if (treeUriString.isNullOrBlank()) return false
        val treeUri = Uri.parse(treeUriString)
        val persistedPermissions = context.contentResolver.persistedUriPermissions
        val hasPermission = persistedPermissions.any {
            it.uri == treeUri && it.isWritePermission && it.isReadPermission
        }
        if (!hasPermission) return false

        val docFile = DocumentFile.fromTreeUri(context, treeUri)
        return docFile != null && docFile.exists() && docFile.canWrite()
    }

    suspend fun saveToSafFolder(
        treeUriString: String,
        sourceInputStream: InputStream,
        mimeType: String = "image/jpeg",
        extension: String = "jpg"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val treeUri = Uri.parse(treeUriString)
            val docDir = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext null
            if (!docDir.exists() || !docDir.canWrite()) return@withContext null

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
            val filename = "CAMCRAFT_$timestamp.$extension"

            val newFile = docDir.createFile(mimeType, filename) ?: return@withContext null

            context.contentResolver.openOutputStream(newFile.uri)?.use { outputStream ->
                sourceInputStream.copyTo(outputStream)
            }
            newFile.uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun querySafPhotos(treeUriString: String): List<MediaItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<MediaItem>()
        if (!isUriPermissionValid(treeUriString)) return@withContext list

        val treeUri = Uri.parse(treeUriString)
        val docDir = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext list

        try {
            docDir.listFiles().forEach { file ->
                if (file.isFile && (file.type?.startsWith("image/") == true || file.name?.endsWith(".jpg") == true || file.name?.endsWith(".png") == true)) {
                    list.add(
                        MediaItem(
                            id = file.uri.hashCode().toLong(),
                            uri = file.uri,
                            displayName = file.name ?: "SAF_Photo.jpg",
                            dateTaken = file.lastModified(),
                            size = file.length(),
                            width = 0,
                            height = 0,
                            mimeType = file.type ?: "image/jpeg",
                            isSafFile = true,
                            relativePath = docDir.name
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    fun deleteSafDocument(docUri: Uri): Boolean {
        return try {
            val doc = DocumentFile.fromSingleUri(context, docUri)
            doc?.delete() == true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
