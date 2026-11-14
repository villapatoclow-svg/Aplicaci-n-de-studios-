package com.example.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import com.example.data.model.AiConnectorConfig
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class ApiKeyVaultManager(private val context: Context) {

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun loadConfig(): AiConnectorConfig {
        return try {
            val raw = prefs.getString(KEY_CIPHER_CONFIG, null)
            val jsonString = if (raw != null) {
                decrypt(raw) ?: prefs.getString(KEY_FALLBACK_CONFIG, null)
            } else {
                prefs.getString(KEY_FALLBACK_CONFIG, null)
            }
            if (jsonString.isNullOrBlank()) {
                AiConnectorConfig()
            } else {
                parseJson(jsonString)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error loading AI connector config", e)
            AiConnectorConfig()
        }
    }

    fun saveConfig(config: AiConnectorConfig) {
        val jsonString = toJson(config).toString()
        val encrypted = encrypt(jsonString)
        prefs.edit().apply {
            if (encrypted != null) {
                putString(KEY_CIPHER_CONFIG, encrypted)
                remove(KEY_FALLBACK_CONFIG)
            } else {
                putString(KEY_FALLBACK_CONFIG, jsonString)
            }
            apply()
        }
    }

    fun clearKeys() {
        val current = loadConfig()
        saveConfig(
            current.copy(
                activeProvider = "firebase_ai",
                geminiApiKey = "",
                cloudCodeApiKey = "",
                customApiKey = ""
            )
        )
    }

    private fun getOrCreateKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } catch (e: Exception) {
            Log.w(TAG, "Keystore unavailable in this runtime, using local app prefs fallback: ${e.message}")
            null
        }
    }

    private fun encrypt(plainText: String): String? {
        val key = getOrCreateKey() ?: return null
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
            val dataB64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            "$ivB64:$dataB64"
        } catch (e: Exception) {
            Log.w(TAG, "Encryption failed: ${e.message}")
            null
        }
    }

    private fun decrypt(payload: String): String? {
        val parts = payload.split(":")
        if (parts.size != 2) return null
        val key = getOrCreateKey() ?: return null
        return try {
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val data = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(cipher.doFinal(data), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            Log.w(TAG, "Decryption failed: ${e.message}")
            null
        }
    }

    private fun toJson(c: AiConnectorConfig): JSONObject = JSONObject().apply {
        put("activeProvider", c.activeProvider)
        put("geminiApiKey", c.geminiApiKey)
        put("geminiModel", c.geminiModel)
        put("cloudCodeApiKey", c.cloudCodeApiKey)
        put("cloudCodeEndpoint", c.cloudCodeEndpoint)
        put("cloudCodeModel", c.cloudCodeModel)
        put("customApiKey", c.customApiKey)
        put("customBaseUrl", c.customBaseUrl)
        put("customModel", c.customModel)
    }

    private fun parseJson(raw: String): AiConnectorConfig {
        val o = JSONObject(raw)
        return AiConnectorConfig(
            activeProvider = o.optString("activeProvider", "firebase_ai"),
            geminiApiKey = o.optString("geminiApiKey", ""),
            geminiModel = o.optString("geminiModel", "gemini-2.5-flash"),
            cloudCodeApiKey = o.optString("cloudCodeApiKey", ""),
            cloudCodeEndpoint = o.optString("cloudCodeEndpoint", "https://api.anthropic.com/v1/messages"),
            cloudCodeModel = o.optString("cloudCodeModel", "claude-sonnet-4-20250514"),
            customApiKey = o.optString("customApiKey", ""),
            customBaseUrl = o.optString("customBaseUrl", "https://api.openai.com/v1/chat/completions"),
            customModel = o.optString("customModel", "gpt-4o-mini")
        )
    }

    private companion object {
        const val TAG = "ApiKeyVault"
        const val PREFS_NAME = "mentepro_proton2_vault"
        const val KEY_CIPHER_CONFIG = "cipher_ai_config"
        const val KEY_FALLBACK_CONFIG = "local_ai_config"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "mentepro_api_vault_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
