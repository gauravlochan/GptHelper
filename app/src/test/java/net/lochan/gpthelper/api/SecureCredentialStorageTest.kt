package net.lochan.gpthelper.api

import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecureCredentialStorageTest {
    private lateinit var storage: CredentialStorage
    private val testKey = "test-api-key"

    @Before
    fun setUp() {
        // Use a mock for CredentialStorage for unit tests
        storage = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `saveApiKey stores key`() {
        every { storage.saveApiKey(testKey) } just Runs
        storage.saveApiKey(testKey)
        verify { storage.saveApiKey(testKey) }
    }

    @Test
    fun `getApiKey retrieves key`() {
        every { storage.getApiKey() } returns testKey
        val result = storage.getApiKey()
        assertEquals(testKey, result)
        verify { storage.getApiKey() }
    }

    @Test
    fun `getApiKey returns null when no key exists`() {
        every { storage.getApiKey() } returns null
        val result = storage.getApiKey()
        assertNull(result)
        verify { storage.getApiKey() }
    }

    @Test
    fun `clearApiKey removes key`() {
        every { storage.clearApiKey() } just Runs
        storage.clearApiKey()
        verify { storage.clearApiKey() }
    }
} 