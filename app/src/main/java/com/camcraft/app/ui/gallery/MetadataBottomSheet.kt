package com.camcraft.app.ui.gallery

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.camcraft.app.data.editing.ExifUtils
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.ui.theme.SubtitleGray
import com.camcraft.app.ui.theme.WarmAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataBottomSheet(
    photo: MediaItem,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val exifData = ExifUtils.readExifData(context, photo.uri)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Photo Information",
                style = MaterialTheme.typography.titleLarge,
                color = WarmAccent,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            MetadataRow("File Name", photo.displayName)
            MetadataRow("Date Taken", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(photo.dateTaken)))
            MetadataRow("File Size", formatFileSize(photo.size))
            MetadataRow("Resolution", if (exifData.width > 0) "${exifData.width} × ${exifData.height}" else "Unknown")
            MetadataRow("MIME Type", photo.mimeType)
            MetadataRow("Storage", if (photo.isSafFile) "Custom SAF Folder (${photo.relativePath})" else "Pictures/CamCraft")

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "EXIF Details",
                style = MaterialTheme.typography.labelLarge,
                color = WarmAccent
            )
            Spacer(modifier = Modifier.height(8.dp))

            MetadataRow("Device", listOfNotNull(exifData.make, exifData.model).joinToString(" ").ifBlank { "Standard Camera" })
            MetadataRow("ISO", exifData.iso ?: "N/A")
            MetadataRow("Aperture", exifData.aperture ?: "N/A")
            MetadataRow("Exposure", exifData.exposureTime ?: "N/A")
            MetadataRow("Focal Length", exifData.focalLength ?: "N/A")
            MetadataRow("Flash", exifData.flash ?: "N/A")

            if (exifData.latitude != null && exifData.longitude != null) {
                MetadataRow("GPS Location", String.format(Locale.US, "%.5f, %.5f", exifData.latitude, exifData.longitude))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = SubtitleGray,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.US, "%.2f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
