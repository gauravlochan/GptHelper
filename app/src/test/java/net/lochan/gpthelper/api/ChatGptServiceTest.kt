package net.lochan.gpthelper.api

import android.content.Context
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import kotlin.time.Duration.Companion.seconds

class ChatGptServiceTest {
    private lateinit var context: Context
    private lateinit var credentialStorage: SecureCredentialStorage
    private lateinit var openAI: OpenAI
    private lateinit var chatGptService: ChatGptService

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        credentialStorage = mockk(relaxed = true)
        openAI = mockk(relaxed = true)
        
        // Mock the creation of ChatGptService to inject our mocks
        mockkConstructor(ChatGptService::class)
        every { 
            anyConstructed<ChatGptService>().apply {
                this::class.memberProperties.find { it.name == "credentialStorage" }?.apply {
                    isAccessible = true
                    set(this@apply, credentialStorage)
                }
                this::class.memberProperties.find { it.name == "openAI" }?.apply {
                    isAccessible = true
                    set(this@apply, openAI)
                }
            }
        } returns Unit

        chatGptService = ChatGptService(context)
    }

    @Test
    fun `initialize sets up OpenAI client and saves API key`() {
        // Arrange
        val apiKey = "test-api-key"
        coEvery { credentialStorage.saveApiKey(apiKey) } just Runs

        // Act
        chatGptService.initialize(apiKey)

        // Assert
        coVerify { credentialStorage.saveApiKey(apiKey) }
    }

    @Test
    fun `initializeWithSavedKey returns true when valid key exists`() {
        // Arrange
        val savedKey = "valid-saved-key"
        coEvery { credentialStorage.getApiKey() } returns savedKey

        // Act
        val result = chatGptService.initializeWithSavedKey()

        // Assert
        assertTrue(result)
        coVerify { credentialStorage.getApiKey() }
    }

    @Test
    fun `initializeWithSavedKey returns false when no key exists`() {
        // Arrange
        coEvery { credentialStorage.getApiKey() } returns null

        // Act
        val result = chatGptService.initializeWithSavedKey()

        // Assert
        assertFalse(result)
        coVerify { credentialStorage.getApiKey() }
    }

    @Test
    fun `validateApiKey returns true for valid key`() = runBlocking {
        // Arrange
        val apiKey = "valid-api-key"
        val models = listOf<Model>()
        coEvery { openAI.models() } returns models

        // Act
        val result = chatGptService.validateApiKey(apiKey)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `validateApiKey returns false for invalid key`() = runBlocking {
        // Arrange
        val apiKey = "invalid-api-key"
        coEvery { openAI.models() } throws Exception("Invalid API key")

        // Act
        val result = chatGptService.validateApiKey(apiKey)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getModels returns empty list when not authenticated`() = runBlocking {
        // Arrange
        coEvery { openAI.models() } throws Exception("Not authenticated")

        // Act
        val result = chatGptService.getModels()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getModels returns list of models when authenticated`() = runBlocking {
        // Arrange
        val expectedModels = listOf<Model>()
        coEvery { openAI.models() } returns expectedModels

        // Act
        val result = chatGptService.getModels()

        // Assert
        assertEquals(expectedModels, result)
    }

    @Test
    fun `clearCredentials clears API key and resets OpenAI client`() {
        // Arrange
        coEvery { credentialStorage.clearApiKey() } just Runs

        // Act
        chatGptService.clearCredentials()

        // Assert
        coVerify { credentialStorage.clearApiKey() }
    }
} 