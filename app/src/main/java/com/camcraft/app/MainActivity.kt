package com.camcraft.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.camcraft.app.data.settings.SettingsRepository
import com.camcraft.app.data.settings.ThemeMode
import com.camcraft.app.ui.navigation.NavGraph
import com.camcraft.app.ui.theme.CamCraftTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(applicationContext)

        setContent {
            val userPrefs by settingsRepository.userPreferencesFlow.collectAsState(initial = com.camcraft.app.data.settings.UserPreferences())

            val isDarkTheme = when (userPrefs.themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CamCraftTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                NavGraph(navController = navController)
            }
        }
    }
}
