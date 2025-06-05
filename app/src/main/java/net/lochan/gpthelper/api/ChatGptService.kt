package net.lochan.gpthelper.api

import android.content.Context
import android.net.ConnectivityManager
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.api.http.Timeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds
import java.io.IOException

/**
 * Service class for handling ChatGPT API interactions.
 * This is the first version focusing on authentication.
 */
class ChatGptService(
    private val context: Context,
    private val credentialStorage: CredentialStorage = SecureCredentialStorage(context),
    private val openAIFactory: (OpenAIConfig) -> OpenAI = { config -> OpenAI(config) }
) {
    private var openAI: OpenAI? = null
    private var isInitialized = false

    /**
     * Initializes the OpenAI client with the provided API key.
     * @param apiKey The ChatGPT API key
     */
    fun initialize(apiKey: String) {
        val config = OpenAIConfig(
            token = apiKey,
            timeout = Timeout(socket = 30.seconds, connect = 30.seconds)
        )
        openAI = openAIFactory(config)
        credentialStorage.saveApiKey(apiKey)
        isInitialized = false // Reset initialization state until we validate
    }

    /**
     * Initializes the OpenAI client with the saved API key if available.
     * @return true if initialization was successful, false otherwise
     */
    suspend fun initializeWithSavedKey(): Boolean {
        val savedKey = credentialStorage.getApiKey()
        return if (savedKey != null) {
            try {
                initialize(savedKey)
                validateApiKey(savedKey)
            } catch (e: Exception) {
                isInitialized = false
                false
            }
        } else {
            false
        }
    }

    /**
     * Validates a ChatGPT API key by making a test API call.
     * @param apiKey The API key to validate
     * @return true if the API key is valid, false otherwise
     * @throws Exception if there's an error during validation
     */
    suspend fun validateApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Check for internet connectivity first
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                if (network == null) {
                    throw IOException("No internet connection available")
                }

                initialize(apiKey)
                // Make a simple API call to verify the key
                val models = openAI?.models()
                isInitialized = models?.isNotEmpty() == true
                isInitialized
            } catch (e: IOException) {
                isInitialized = false
                throw e
            } catch (e: Exception) {
                isInitialized = false
                throw IOException("Failed to validate API key: ${e.message}", e)
            }
        }
    }

    /**
     * Gets the list of available models.
     * @return List of available models or empty list if not authenticated
     */
    suspend fun getModels(): List<Model> {
        if (!isInitialized) {
            return emptyList()
        }
        return withContext(Dispatchers.IO) {
            try {
                openAI?.models() ?: emptyList()
            } catch (e: Exception) {
                isInitialized = false
                emptyList()
            }
        }
    }

    /**
     * Clears the saved API key and resets the OpenAI client.
     */
    fun clearCredentials() {
        credentialStorage.clearApiKey()
        openAI = null
        isInitialized = false
    }

    /**
     * Checks if the service is properly initialized with a valid API key.
     * @return true if the service is initialized and ready to use
     */
    fun isReady(): Boolean = isInitialized
} 