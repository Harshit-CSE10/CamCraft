package com.camcraft.app.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.camcraft.app.data.media.MediaItem
import com.camcraft.app.ui.theme.ShutterRed
import com.camcraft.app.ui.theme.WarmAccent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    initialIndex: Int,
    viewModel: GalleryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEditor: (MediaItem) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.photos.isEmpty()) {
        onNavigateBack()
        return
    }

    val initialClamped = initialIndex.coerceIn(0, uiState.photos.size - 1)
    val pagerState = rememberPagerState(initialPage = initialClamped, pageCount = { uiState.photos.size })
    val currentPhoto = uiState.photos.getOrNull(pagerState.currentPage) ?: uiState.photos[0]

    var showMetadataSheet by remember { mutableStateOf(false) }
    var photoToDelete by remember { mutableStateOf<MediaItem?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${pagerState.currentPage + 1} of ${uiState.photos.size}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { sharePhoto(context, currentPhoto.uri) }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { onNavigateToEditor(currentPhoto) }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = WarmAccent)
                    }
                    IconButton(onClick = { showMetadataSheet = true }) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Info", tint = Color.White)
                    }
                    IconButton(onClick = { photoToDelete = currentPhoto }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = ShutterRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                )
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val photo = uiState.photos[page]
                ZoomableImage(photo = photo)
            }

            if (showMetadataSheet) {
                MetadataBottomSheet(
                    photo = currentPhoto,
                    sheetState = sheetState,
                    onDismiss = { showMetadataSheet = false }
                )
            }

            photoToDelete?.let { photo ->
                AlertDialog(
                    onDismissRequest = { photoToDelete = null },
                    title = { Text(text = "Delete Photo?") },
                    text = { Text(text = "Are you sure you want to permanently delete '${photo.displayName}'?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                photoToDelete = null
                                viewModel.confirmDeletePhoto(photo)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ShutterRed)
                        ) {
                            Text(text = "Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { photoToDelete = null }) {
                            Text(text = "Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(photo: MediaItem) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 4f)
                    if (scale > 1f) {
                        offsetX += pan.x
                        offsetY += pan.y
                    } else {
                        offsetX = 0f
                        offsetY = 0f
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photo.uri)
                .crossfade(true)
                .build(),
            contentDescription = photo.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
        )
    }
}

fun sharePhoto(context: Context, uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
}
