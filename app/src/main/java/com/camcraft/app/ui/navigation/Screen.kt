package com.camcraft.app.ui.navigation

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
    object Settings : Screen("settings")
    object Detail : Screen("detail/{mediaId}") {
        fun createRoute(mediaId: Long) = "detail/$mediaId"
    }
    object Editor : Screen("editor/{mediaUri}") {
        fun createRoute(encodedUri: String) = "editor/$encodedUri"
    }
}
