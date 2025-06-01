package net.lochan.gpthelper.api

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for sensitive credentials like API keys.
 * Uses EncryptedSharedPreferences to ensure credentials are stored securely.
 */
class SecureCredentialStorage(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_API_KEY = "chatgpt_api_key"
    }

    /**
     * Saves the ChatGPT API key securely.
     * @param apiKey The API key to save
     */
    fun saveApiKey(apiKey: String) {
        sharedPreferences.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    /**
     * Retrieves the saved ChatGPT API key.
     * @return The saved API key, or null if not found
     */
    fun getApiKey(): String? {
        return sharedPreferences.getString(KEY_API_KEY, null)
    }

    /**
     * Clears the saved API key.
     */
    fun clearApiKey() {
        sharedPreferences.edit().remove(KEY_API_KEY).apply()
    }
} 