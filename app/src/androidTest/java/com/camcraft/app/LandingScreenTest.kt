package com.camcraft.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.camcraft.app.ui.landing.LandingScreen
import com.camcraft.app.ui.landing.LandingViewModel
import com.camcraft.app.ui.theme.CamCraftTheme
import org.junit.Rule
import org.junit.Test

class LandingScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLandingScreenButtonsDisplayed() {
        composeTestRule.setContent {
            CamCraftTheme {
                LandingScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
                    onNavigateToCamera = {},
                    onNavigateToGallery = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Open Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("Saved Photos").assertIsDisplayed()
        composeTestRule.onNodeWithText("Professional Mobile Photography").assertIsDisplayed()
    }
}
