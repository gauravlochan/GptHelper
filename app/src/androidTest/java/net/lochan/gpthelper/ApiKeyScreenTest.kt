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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import kotlinx.coroutines.flow.MutableStateFlow

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
        val viewModel = mock(ApiKeyViewModel::class.java)
        whenever(viewModel.apiKeyState).thenReturn(MutableStateFlow(ApiKeyState.NotConfigured))

        // Act
        composeTestRule.setContent {
            ApiKeyScreen(
                onNavigateBack = {},
                viewModel = viewModel
            )
        }

        // Assert
        composeTestRule.onNodeWithText("ChatGPT API Key").assertExists()
        composeTestRule.onNodeWithText("Save API Key").assertExists().assertIsNotEnabled()
    }

    @Test
    fun whenApiKeyEntered_saveButtonBecomesEnabled() {
        // Arrange
        val viewModel = mock(ApiKeyViewModel::class.java)
        whenever(viewModel.apiKeyState).thenReturn(MutableStateFlow(ApiKeyState.NotConfigured))

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
        val viewModel = mock(ApiKeyViewModel::class.java)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        whenever(viewModel.apiKeyState).thenReturn(stateFlow)

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
        composeTestRule.onNodeWithText("Validating API key...").assertExists()
        composeTestRule.onAllNodesWithContentDescription("CircularProgressIndicator").assertExists()
    }

    @Test
    fun whenInvalidKey_showsErrorState() {
        // Arrange
        val viewModel = mock(ApiKeyViewModel::class.java)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        whenever(viewModel.apiKeyState).thenReturn(stateFlow)

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
        composeTestRule.onNodeWithText("Invalid API key. Please check and try again.").assertExists()
    }

    @Test
    fun whenValidKey_showsSuccessState() {
        // Arrange
        val viewModel = mock(ApiKeyViewModel::class.java)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.NotConfigured)
        whenever(viewModel.apiKeyState).thenReturn(stateFlow)

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
        composeTestRule.onNodeWithText("API key is configured and valid.").assertExists()
        composeTestRule.onNodeWithText("Clear API Key").assertExists()
    }

    @Test
    fun whenConfigured_clearButtonRemovesKey() {
        // Arrange
        val viewModel = mock(ApiKeyViewModel::class.java)
        val stateFlow = MutableStateFlow<ApiKeyState>(ApiKeyState.Configured)
        whenever(viewModel.apiKeyState).thenReturn(stateFlow)

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
        verify(viewModel).clearApiKey()
    }

    @Test
    fun backButton_triggersNavigation() {
        // Arrange
        var navigationTriggered = false
        val viewModel = mock(ApiKeyViewModel::class.java)
        whenever(viewModel.apiKeyState).thenReturn(MutableStateFlow(ApiKeyState.NotConfigured))

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