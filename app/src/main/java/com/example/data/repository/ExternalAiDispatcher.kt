package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.AiConnectorConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class ExternalAiDispatcher {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun isExternalTextActive(config: AiConnectorConfig): Boolean {
        return when (config.activeProvider) {
            "gemini_byok" -> config.geminiApiKey.isNotBlank()
            "cloud_code" -> config.cloudCodeApiKey.isNotBlank()
            "openai_compatible" -> config.customApiKey.isNotBlank()
            else -> false
        }
    }

    suspend fun generateText(
        config: AiConnectorConfig,
        prompt: String,
        expectJson: Boolean = false,
        bitmap: Bitmap? = null
    ): String = withContext(Dispatchers.IO) {
        val finalPrompt = if (expectJson) {
            "$prompt\n\nRESPONDE ÚNICAMENTE con un objeto JSON válido (sin texto adicional ni bloques markdown)."
        } else {
            prompt
        }

        val raw = when (config.activeProvider) {
            "gemini_byok" -> callGeminiRest(
                apiKey = config.geminiApiKey.trim(),
                model = config.geminiModel.trim().ifBlank { "gemini-2.5-flash" },
                prompt = finalPrompt,
                expectJson = expectJson,
                bitmap = bitmap
            )
            "cloud_code" -> callCloudCodeRest(
                apiKey = config.cloudCodeApiKey.trim(),
                endpoint = config.cloudCodeEndpoint.trim().ifBlank { "https://api.anthropic.com/v1/messages" },
                model = config.cloudCodeModel.trim().ifBlank { "claude-sonnet-4-20250514" },
                prompt = finalPrompt,
                bitmap = bitmap
            )
            "openai_compatible" -> callOpenAiCompatibleRest(
                apiKey = config.customApiKey.trim(),
                endpoint = config.customBaseUrl.trim().ifBlank { "https://api.openai.com/v1/chat/completions" },
                model = config.customModel.trim().ifBlank { "gpt-4o-mini" },
                prompt = finalPrompt
            )
            else -> throw IllegalStateException("Proveedor externo no configurado.")
        }

        if (expectJson) extractJsonObject(raw) else raw.trim()
    }

    suspend fun testConnection(config: AiConnectorConfig): Result<String> {
        return try {
            if (config.activeProvider == "firebase_ai") {
                return Result.success("Firebase AI Logic (Protón 2) activo sin clave API requerida.")
            }
            val reply = generateText(
                config = config,
                prompt = "Responde exactamente: Conexión Protón 2 verificada.",
                expectJson = false
            )
            Result.success("${config.activeLabel()}: ${reply.take(80)}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun callGeminiRest(
        apiKey: String,
        model: String,
        prompt: String,
        expectJson: Boolean,
        bitmap: Bitmap?
    ): String {
        if (apiKey.isBlank()) throw IllegalStateException("Falta tu clave API de Gemini.")
        val parts = JSONArray()
        if (bitmap != null) {
            val b64 = bitmapToBase64Jpeg(bitmap)
            parts.put(
                JSONObject().put(
                    "inlineData",
                    JSONObject()
                        .put("mimeType", "image/jpeg")
                        .put("data", b64)
                )
            )
        }
        parts.put(JSONObject().put("text", prompt))

        val bodyJson = JSONObject().apply {
            put("contents", JSONArray().put(JSONObject().put("parts", parts)))
            if (expectJson) {
                put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
            }
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { resp ->
            val respText = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("Gemini API (${resp.code}): ${extractErrorMessage(respText)}")
            }
            val json = JSONObject(respText)
            val candidates = json.optJSONArray("candidates") ?: throw IllegalStateException("Sin respuesta de Gemini API.")
            val first = candidates.getJSONObject(0)
            val content = first.getJSONObject("content")
            val respParts = content.getJSONArray("parts")
            return respParts.getJSONObject(0).getString("text")
        }
    }

    private fun callCloudCodeRest(
        apiKey: String,
        endpoint: String,
        model: String,
        prompt: String,
        bitmap: Bitmap?
    ): String {
        if (apiKey.isBlank()) throw IllegalStateException("Falta tu clave API de Cloud Code / Anthropic.")
        val contentArr = JSONArray()
        if (bitmap != null) {
            val b64 = bitmapToBase64Jpeg(bitmap)
            contentArr.put(
                JSONObject()
                    .put("type", "image")
                    .put(
                        "source",
                        JSONObject()
                            .put("type", "base64")
                            .put("media_type", "image/jpeg")
                            .put("data", b64)
                    )
            )
        }
        contentArr.put(JSONObject().put("type", "text").put("text", prompt))

        val bodyJson = JSONObject().apply {
            put("model", model)
            put("max_tokens", 2048)
            put("messages", JSONArray().put(JSONObject().put("role", "user").put("content", contentArr)))
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { resp ->
            val respText = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("Cloud Code API (${resp.code}): ${extractErrorMessage(respText)}")
            }
            val json = JSONObject(respText)
            val arr = json.optJSONArray("content") ?: throw IllegalStateException("Sin contenido de Cloud Code.")
            return arr.getJSONObject(0).getString("text")
        }
    }

    private fun callOpenAiCompatibleRest(
        apiKey: String,
        endpoint: String,
        model: String,
        prompt: String
    ): String {
        if (apiKey.isBlank()) throw IllegalStateException("Falta tu clave API del servicio externo.")
        val bodyJson = JSONObject().apply {
            put("model", model)
            put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", prompt)
                )
            )
            put("temperature", 0.5)
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
            .build()

        client.newCall(request).execute().use { resp ->
            val respText = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) {
                throw IllegalStateException("API Externa (${resp.code}): ${extractErrorMessage(respText)}")
            }
            val json = JSONObject(respText)
            val choices = json.optJSONArray("choices") ?: throw IllegalStateException("Sin respuestas del proveedor externo.")
            return choices.getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
    }

    private fun extractErrorMessage(raw: String): String {
        return try {
            val o = JSONObject(raw)
            o.optJSONObject("error")?.optString("message")
                ?: o.optString("message")
                ?: raw.take(140)
        } catch (_: Exception) {
            raw.take(140)
        }
    }

    private fun extractJsonObject(raw: String): String {
        val trimmed = raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = trimmed.indexOf('{')
        val end = trimmed.lastIndexOf('}')
        return if (start in 0 until end) {
            trimmed.substring(start, end + 1)
        } else {
            trimmed
        }
    }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
