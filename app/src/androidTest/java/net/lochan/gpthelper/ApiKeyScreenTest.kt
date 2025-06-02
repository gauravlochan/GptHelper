package net.lochan.gpthelper

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import net.lochan.gpthelper.ui.ApiKeyScreen
import net.lochan.gpthelper.viewmodel.ApiKeyState
import net.lochan.gpthelper.viewmodel.ApiKeyViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import io.mockk.mockk
import io.mockk.verify
import io.mockk.every
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.test.onNodeWithTag

/**
 * UI tests for the API key management screen.
 * Tests various scenarios including:
 * - Initial state
 * - Entering and validating API key
 * - Error states
 * - Navigation
 */
@RunWith(AndroidJUnit4::class)
class ApiKeyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun initialState_showsEmptyInput() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        every { viewModel.apiKeyState } returns MutableStateFlow(ApiKeyState.NotConfigured)

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Assert
        composeTestRule.onNodeWithText("ChatGPT API Key").assertIsDisplayed()
        composeTestRule.onNodeWithText("Save API Key").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun whenApiKeyEntered_saveButtonBecomesEnabled() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        every { viewModel.apiKeyState } returns MutableStateFlow(ApiKeyState.NotConfigured)

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Enter API key
        composeTestRule.onNodeWithText("ChatGPT API Key")
            .performTextInput("test-api-key")

        // Assert
        composeTestRule.onNodeWithText("Save API Key").assertIsEnabled()
    }

    @Test
    fun whenValidating_showsLoadingState() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        every { viewModel.apiKeyState } returns stateFlow

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Enter API key and trigger validation
        composeTestRule.onNodeWithText("ChatGPT API Key")
            .performTextInput("test-api-key")
        composeTestRule.onNodeWithText("Save API Key").performClick()

        // Update state to Validating
        stateFlow.value = ApiKeyState.Validating

        // Assert
        composeTestRule.onNodeWithTag("loading_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun whenInvalidKey_showsErrorState() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        every { viewModel.apiKeyState } returns stateFlow

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Enter API key and trigger validation
        composeTestRule.onNodeWithText("ChatGPT API Key")
            .performTextInput("invalid-key")
        composeTestRule.onNodeWithText("Save API Key").performClick()

        // Update state to Invalid
        stateFlow.value = ApiKeyState.Invalid

        // Assert
        composeTestRule.onNodeWithText("Invalid API key. Please check and try again.").assertIsDisplayed()
    }

    @Test
    fun whenValidKey_showsSuccessState() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        every { viewModel.apiKeyState } returns stateFlow

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Enter API key and trigger validation
        composeTestRule.onNodeWithText("ChatGPT API Key")
            .performTextInput("valid-key")
        composeTestRule.onNodeWithText("Save API Key").performClick()

        // Update state to Configured
        stateFlow.value = ApiKeyState.Configured

        // Assert
        composeTestRule.onNodeWithText("API key is configured and valid.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear API Key").assertIsDisplayed()
    }

    @Test
    fun whenConfigured_clearButtonRemovesKey() {
        // Arrange
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.Configured)
        every { viewModel.apiKeyState } returns stateFlow

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Click clear button
        composeTestRule.onNodeWithText("Clear API Key").performClick()

        // Assert
        verify { viewModel.clearApiKey() }
    }

    @Test
    fun backButton_triggersNavigation() {
        // Arrange
        var navigationTriggered = false
        val viewModel = mockk<ApiKeyViewModel>(relaxed = true)
        every { viewModel.apiKeyState } returns MutableStateFlow(ApiKeyState.NotConfigured)

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = { navigationTriggered = true },
                viewModel = viewModel
            )
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Assert
        assert(navigationTriggered)
    }
} 