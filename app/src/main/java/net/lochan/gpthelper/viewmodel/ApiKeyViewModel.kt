package net.lochan.gpthelper.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.lochan.gpthelper.api.ChatGptService

/**
 * ViewModel for managing the ChatGPT API key.
 * This class handles all operations related to the API key:
 * - Loading the saved key on startup
 * - Validating new API keys
 * - Saving valid API keys
 * - Clearing saved API keys
 * 
 * The ViewModel uses StateFlow to expose the current state of the API key configuration,
 * which the UI can observe to show appropriate feedback to the user.
 */
class ApiKeyViewModel(application: Application) : AndroidViewModel(application) {
    // Initialize the ChatGPT service with the application context
    private val chatGptService = ChatGptService(application)
    
    // StateFlow to hold and expose the current API key state
    // Initial state is set to Initial, which will be updated in init block
    private val _apiKeyState = MutableStateFlow<ApiKeyState>(ApiKeyState.Initial)
    val apiKeyState: StateFlow<ApiKeyState> = _apiKeyState.asStateFlow()

    init {
        // On initialization, attempt to load and validate the saved API key
        // This ensures the app starts with the correct state if a key was previously saved
        if (chatGptService.initializeWithSavedKey()) {
            _apiKeyState.value = ApiKeyState.Configured
        } else {
            _apiKeyState.value = ApiKeyState.NotConfigured
        }
    }

    /**
     * Validates and saves the provided API key.
     * This function:
     * 1. Updates the state to Validating
     * 2. Attempts to validate the key with the ChatGPT API
     * 3. If valid, saves the key and updates state to Configured
     * 4. If invalid, updates state to Invalid
     * 
     * @param apiKey The API key to validate and save
     */
    fun validateAndSaveApiKey(apiKey: String) {
        viewModelScope.launch {
            _apiKeyState.value = ApiKeyState.Validating
            val isValid = chatGptService.validateApiKey(apiKey)
            _apiKeyState.value = if (isValid) {
                ApiKeyState.Configured
            } else {
                ApiKeyState.Invalid
            }
        }
    }

    /**
     * Clears the saved API key and resets the state.
     * This function:
     * 1. Clears the saved key from secure storage
     * 2. Resets the ChatGPT service
     * 3. Updates the state to NotConfigured
     */
    fun clearApiKey() {
        chatGptService.clearCredentials()
        _apiKeyState.value = ApiKeyState.NotConfigured
    }
}

/**
 * Represents the possible states of the API key configuration.
 * These states are used to:
 * - Show appropriate UI feedback
 * - Control button states
 * - Guide the user through the configuration process
 */
sealed class ApiKeyState {
    /** Initial state when the ViewModel is created */
    object Initial : ApiKeyState()
    
    /** No API key is configured */
    object NotConfigured : ApiKeyState()
    
    /** Currently validating a provided API key */
    object Validating : ApiKeyState()
    
    /** A valid API key is configured and saved */
    object Configured : ApiKeyState()
    
    /** The provided API key was invalid */
    object Invalid : ApiKeyState()
} 