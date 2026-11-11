package com.camcraft.app.data.media

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.camcraft.app.data.settings.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class DeleteResult {
    object Success : DeleteResult()
    data class RequiresPermission(val intentSender: IntentSender) : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}

class MediaRepository(private val context: Context) {

    val mediaStoreManager = MediaStoreManager(context)
    val safManager = SafManager(context)

    suspend fun getAllPhotos(prefs: UserPreferences): List<MediaItem> = withContext(Dispatchers.IO) {
        val mediaStorePhotos = mediaStoreManager.queryCamCraftPhotos()
        val customSafUri = prefs.customSafUri

        val safPhotos = if (!customSafUri.isNullOrBlank() && safManager.isUriPermissionValid(customSafUri)) {
            safManager.querySafPhotos(customSafUri)
        } else emptyList()

        // Combine and sort newest first
        (mediaStorePhotos + safPhotos).sortedByDescending { it.dateTaken }
    }

    suspend fun getLatestPhoto(prefs: UserPreferences): MediaItem? = withContext(Dispatchers.IO) {
        getAllPhotos(prefs).firstOrNull()
    }

    suspend fun deletePhoto(item: MediaItem): DeleteResult = withContext(Dispatchers.IO) {
        if (item.isSafFile) {
            val deleted = safManager.deleteSafDocument(item.uri)
            return@withContext if (deleted) DeleteResult.Success else DeleteResult.Error("Failed to delete SAF file.")
        }

        try {
            val rows = context.contentResolver.delete(item.uri, null, null)
            if (rows > 0) {
                DeleteResult.Success
            } else {
                DeleteResult.Error("Photo could not be deleted from MediaStore.")
            }
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(item.uri))
                DeleteResult.RequiresPermission(pendingIntent.intentSender)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val recoverableSecurityException = securityException as? android.app.RecoverableSecurityException
                if (recoverableSecurityException != null) {
                    DeleteResult.RequiresPermission(recoverableSecurityException.userAction.actionIntent.intentSender)
                } else {
                    DeleteResult.Error(securityException.localizedMessage ?: "Permission denied for photo deletion.")
                }
            } else {
                DeleteResult.Error(securityException.localizedMessage ?: "Security exception during delete.")
            }
        } catch (e: Exception) {
            DeleteResult.Error(e.localizedMessage ?: "Error deleting photo.")
        }
    }
}
