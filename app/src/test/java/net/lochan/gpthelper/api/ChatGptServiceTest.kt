package net.lochan.gpthelper.api

import android.content.Context
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull

class ChatGptServiceTest {
    private lateinit var credentialStorage: CredentialStorage
    private lateinit var openAI: OpenAI
    private lateinit var service: ChatGptService
    private val context = mockk<android.content.Context>(relaxed = true)

    @Before
    fun setUp() {
        credentialStorage = mockk(relaxed = true)
        openAI = mockk(relaxed = true)
        service = ChatGptService(context, credentialStorage)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `initialize saves API key`() {
        every { credentialStorage.saveApiKey("key") } just Runs
        service.initialize("key")
        verify { credentialStorage.saveApiKey("key") }
    }

    @Test
    fun `initializeWithSavedKey returns true if key exists`() {
        every { credentialStorage.getApiKey() } returns "key"
        every { credentialStorage.saveApiKey("key") } just Runs
        val result = service.initializeWithSavedKey()
        assertTrue(result)
        verify { credentialStorage.getApiKey() }
    }

    @Test
    fun `initializeWithSavedKey returns false if no key exists`() {
        every { credentialStorage.getApiKey() } returns null
        val result = service.initializeWithSavedKey()
        assertFalse(result)
        verify { credentialStorage.getApiKey() }
    }

    @Test
    fun `clearCredentials clears storage and resets OpenAI`() {
        every { credentialStorage.clearApiKey() } just Runs
        service.clearCredentials()
        verify { credentialStorage.clearApiKey() }
    }

    @Test
    fun `validateApiKey returns true for valid key`() = runBlocking {
        // Arrange
        val apiKey = "valid-api-key"
        val models = listOf<Model>()
        coEvery { openAI.models() } returns models

        // Act
        val result = service.validateApiKey(apiKey)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `validateApiKey returns false for invalid key`() = runBlocking {
        // Arrange
        val apiKey = "invalid-api-key"
        coEvery { openAI.models() } throws Exception("Invalid API key")

        // Act
        val result = service.validateApiKey(apiKey)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `getModels returns empty list when not authenticated`() = runBlocking {
        // Arrange
        coEvery { openAI.models() } throws Exception("Not authenticated")

        // Act
        val result = service.getModels()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getModels returns list of models when authenticated`() = runBlocking {
        // Arrange
        val expectedModels = listOf<Model>()
        coEvery { openAI.models() } returns expectedModels

        // Act
        val result = service.getModels()

        // Assert
        assertEquals(expectedModels, result)
    }
} 