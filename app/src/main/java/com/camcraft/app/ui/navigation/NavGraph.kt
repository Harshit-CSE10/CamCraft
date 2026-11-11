package com.camcraft.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.camcraft.app.data.camera.CameraViewModel
import com.camcraft.app.ui.camera.CameraScreen
import com.camcraft.app.ui.editor.EditorScreen
import com.camcraft.app.ui.editor.EditorViewModel
import com.camcraft.app.ui.gallery.DetailScreen
import com.camcraft.app.ui.gallery.GalleryScreen
import com.camcraft.app.ui.gallery.GalleryViewModel
import com.camcraft.app.ui.landing.LandingScreen
import com.camcraft.app.ui.landing.LandingViewModel
import com.camcraft.app.ui.settings.SettingsScreen
import com.camcraft.app.ui.settings.SettingsViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route,
        modifier = modifier
    ) {
        composable(Screen.Landing.route) {
            val landingViewModel: LandingViewModel = viewModel()
            LandingScreen(
                viewModel = landingViewModel,
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Camera.route) {
            val cameraViewModel: CameraViewModel = viewModel()
            CameraScreen(
                viewModel = cameraViewModel,
                onNavigateToGallery = { navController.navigate(Screen.Gallery.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Gallery.route) {
            val galleryViewModel: GalleryViewModel = viewModel()
            GalleryScreen(
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { _, index ->
                    navController.navigate(Screen.Detail.createRoute(index.toLong()))
                },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) }
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mediaIndex = backStackEntry.arguments?.getLong("mediaId")?.toInt() ?: 0
            val galleryViewModel: GalleryViewModel = viewModel()
            DetailScreen(
                initialIndex = mediaIndex,
                viewModel = galleryViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditor = { photo ->
                    val encodedUri = Uri.encode(photo.uri.toString())
                    navController.navigate(Screen.Editor.createRoute(encodedUri))
                }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("mediaUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("mediaUri") ?: ""
            val imageUri = Uri.parse(Uri.decode(encodedUri))
            val editorViewModel: EditorViewModel = viewModel()
            EditorScreen(
                imageUri = imageUri,
                viewModel = editorViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSaveSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
