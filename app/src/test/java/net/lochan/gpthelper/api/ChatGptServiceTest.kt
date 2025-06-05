package net.lochan.gpthelper.api

import android.content.Context
import android.net.ConnectivityManager
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertNull
import java.io.IOException

class ChatGptServiceTest {
    private lateinit var credentialStorage: CredentialStorage
    private lateinit var openAI: OpenAI
    private lateinit var service: ChatGptService
    private val context = mockk<android.content.Context>(relaxed = true)
    private val connectivityManager = mockk<ConnectivityManager>(relaxed = true)

    @Before
    fun setUp() {
        credentialStorage = mockk(relaxed = true)
        openAI = mockk(relaxed = true)
        every { context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns mockk(relaxed = true)
        service = ChatGptService(
            context,
            credentialStorage,
            openAIFactory = { _: OpenAIConfig -> openAI }
        )
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
    fun `initializeWithSavedKey returns true if key exists`() = runBlocking {
        every { credentialStorage.getApiKey() } returns "key"
        every { credentialStorage.saveApiKey("key") } just Runs
        coEvery { openAI.models() } returns listOf(mockk<Model>())
        val result = service.initializeWithSavedKey()
        assertTrue(result)
        verify { credentialStorage.getApiKey() }
    }

    @Test
    fun `initializeWithSavedKey returns false if no key exists`() = runBlocking {
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
        every { credentialStorage.saveApiKey(apiKey) } just Runs
        coEvery { openAI.models() } returns listOf(mockk<Model>())

        // Act
        val result = service.validateApiKey(apiKey)

        // Assert
        assertTrue(result)
        verify { credentialStorage.saveApiKey(apiKey) }
    }

    @Test
    fun `validateApiKey throws IOException for invalid key`() = runBlocking {
        // Arrange
        val apiKey = "invalid-api-key"
        every { credentialStorage.saveApiKey(apiKey) } just Runs
        coEvery { openAI.models() } throws Exception("Invalid API key")

        // Act & Assert
        try {
            service.validateApiKey(apiKey)
            assertFalse(true) // Should not reach here
        } catch (e: IOException) {
            assertTrue(e.message?.contains("Failed to validate API key") == true)
        }
        verify { credentialStorage.saveApiKey(apiKey) }
    }

    @Test
    fun `getModels returns empty list when not authenticated`() = runBlocking {
        // Arrange
        coEvery { openAI.models() } throws Exception("Not authenticated")
        // Simulate not initialized
        service.clearCredentials()

        // Act
        val result = service.getModels()

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getModels returns list of models when authenticated`() = runBlocking {
        // Arrange
        val expectedModels = listOf(mockk<Model>())
        coEvery { openAI.models() } returns expectedModels
        service.initialize("key")
        // Simulate successful validation
        service.javaClass.getDeclaredField("isInitialized").apply { isAccessible = true }.set(service, true)

        // Act
        val result = service.getModels()

        // Assert
        assertEquals(expectedModels, result)
    }
} 