package net.lochan.gpthelper.api

import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.api.http.Timeout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service class for handling ChatGPT API interactions.
 * This is the first version focusing on authentication.
 */
class ChatGptService {
    private var openAI: OpenAI? = null

    /**
     * Initializes the OpenAI client with the provided API key.
     * @param apiKey The ChatGPT API key
     */
    fun initialize(apiKey: String) {
        val config = OpenAIConfig(
            token = apiKey,
            timeout = Timeout(socket = 30000, connect = 30000) // 30 seconds for both socket and connect timeouts
        )
        openAI = OpenAI(config)
    }

    /**
     * Validates a ChatGPT API key by attempting to list available models.
     * @param apiKey The API key to validate
     * @return true if the API key is valid, false otherwise
     */
    suspend fun validateApiKey(apiKey: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                initialize(apiKey)
                val models = openAI?.models()
                models?.isNotEmpty() == true
            } catch (e: Exception) {
                false
            }
        }
    }

    /**
     * Gets the list of available models.
     * @return List of available models or empty list if not authenticated
     */
    suspend fun getModels(): List<Model> {
        return withContext(Dispatchers.IO) {
            try {
                openAI?.models() ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
} 