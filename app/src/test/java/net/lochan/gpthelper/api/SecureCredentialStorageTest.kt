package net.lochan.gpthelper.api

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class SecureCredentialStorageTest {
    private lateinit var context: Context
    private lateinit var masterKey: MasterKey
    private lateinit var encryptedPrefs: EncryptedSharedPreferences
    private lateinit var storage: SecureCredentialStorage

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        masterKey = mockk(relaxed = true)
        encryptedPrefs = mockk(relaxed = true)
        
        // Mock the creation of EncryptedSharedPreferences
        mockkObject(EncryptedSharedPreferences)
        every { 
            EncryptedSharedPreferences.create(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns encryptedPrefs

        storage = SecureCredentialStorage(context)
    }

    @Test
    fun `saveApiKey stores key in encrypted preferences`() {
        // Arrange
        val apiKey = "test-api-key"
        val editor = mockk<EncryptedSharedPreferences.Editor>(relaxed = true)
        every { encryptedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.apply() } just Runs

        // Act
        storage.saveApiKey(apiKey)

        // Assert
        verify { 
            editor.putString(eq("api_key"), eq(apiKey))
            editor.apply()
        }
    }

    @Test
    fun `getApiKey retrieves key from encrypted preferences`() {
        // Arrange
        val expectedKey = "test-api-key"
        every { encryptedPrefs.getString("api_key", null) } returns expectedKey

        // Act
        val result = storage.getApiKey()

        // Assert
        assertEquals(expectedKey, result)
        verify { encryptedPrefs.getString("api_key", null) }
    }

    @Test
    fun `getApiKey returns null when no key exists`() {
        // Arrange
        every { encryptedPrefs.getString("api_key", null) } returns null

        // Act
        val result = storage.getApiKey()

        // Assert
        assertNull(result)
        verify { encryptedPrefs.getString("api_key", null) }
    }

    @Test
    fun `clearApiKey removes key from encrypted preferences`() {
        // Arrange
        val editor = mockk<EncryptedSharedPreferences.Editor>(relaxed = true)
        every { encryptedPrefs.edit() } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } just Runs

        // Act
        storage.clearApiKey()

        // Assert
        verify { 
            editor.remove("api_key")
            editor.apply()
        }
    }

    @Test
    fun `getApiKey handles encryption exceptions`() {
        // Arrange
        every { encryptedPrefs.getString("api_key", null) } throws Exception("Encryption error")

        // Act
        val result = storage.getApiKey()

        // Assert
        assertNull(result)
    }

    @Test
    fun `saveApiKey handles encryption exceptions`() {
        // Arrange
        val apiKey = "test-api-key"
        val editor = mockk<EncryptedSharedPreferences.Editor>(relaxed = true)
        every { encryptedPrefs.edit() } returns editor
        every { editor.putString(any(), any()) } throws Exception("Encryption error")

        // Act & Assert
        try {
            storage.saveApiKey(apiKey)
        } catch (e: Exception) {
            fail("Should handle encryption exceptions gracefully")
        }
    }
} 