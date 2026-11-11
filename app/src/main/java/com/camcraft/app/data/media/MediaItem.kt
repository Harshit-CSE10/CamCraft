package com.camcraft.app.data.media

import android.net.Uri

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTaken: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val isSafFile: Boolean = false,
    val relativePath: String? = null
)
