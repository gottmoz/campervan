package se.gottmoz.camperagent.integration.logging

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.net.HttpURLConnection
import java.net.URL

class RemoteLogUploader(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("remote_logging", Context.MODE_PRIVATE)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("enabled", enabled).apply()
    }

    fun setServerUrl(url: String) {
        prefs.edit().putString("serverUrl", sanitizeUrl(url)).apply()
    }

    fun settings(): JSONObject = JSONObject()
        .put("enabled", enabled())
        .put("serverUrl", serverUrl())

    suspend fun testConnection(): JSONObject = withContext(Dispatchers.IO) {
        request("GET", "${serverUrl()}/health", null)
    }

    suspend fun uploadLog(level: String, tag: String, message: String, data: JSONObject = JSONObject()): JSONObject = withContext(Dispatchers.IO) {
        if (!enabled()) return@withContext JSONObject().put("skipped", "disabled")
        val payload = JSONObject()
            .put("timestamp", nowIso())
            .put("source", "android")
            .put("level", level)
            .put("tag", tag)
            .put("message", message)
            .put("data", redact(data))
        request("POST", "${serverUrl()}/api/logs/ingest", payload)
    }

    suspend fun uploadDiagnostics(json: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        request("POST", "${serverUrl()}/api/diagnostics/upload", redact(json))
    }

    suspend fun runtimeStatus(): JSONObject = withContext(Dispatchers.IO) {
        request("GET", "${serverUrl()}/api/runtime/status", null)
    }

    suspend fun latestLogs(): JSONObject = withContext(Dispatchers.IO) {
        request("GET", "${serverUrl()}/api/logs/latest", null)
    }

    fun enabled(): Boolean = prefs.getBoolean("enabled", true)
    fun serverUrl(): String = prefs.getString("serverUrl", DEFAULT_URL) ?: DEFAULT_URL

    private fun request(method: String, url: String, payload: JSONObject?): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 3_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/json")
            if (payload != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        payload?.let {
            connection.outputStream.use { stream -> stream.write(it.toString().toByteArray(Charsets.UTF_8)) }
        }
        val body = runCatching {
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        }.getOrDefault("")
        return runCatching { JSONObject(body) }.getOrElse {
            JSONObject().put("ok", connection.responseCode in 200..299).put("data", JSONObject().put("raw", body)).put("error", null)
        }
    }

    private fun redact(value: JSONObject): JSONObject {
        val out = JSONObject()
        value.keys().forEach { key ->
            val lowered = key.lowercase()
            out.put(key, if (listOf("token", "secret", "password", "credential", "wifi").any { lowered.contains(it) }) "[redacted]" else value.get(key))
        }
        return out
    }

    private fun sanitizeUrl(url: String): String = url.trim().trimEnd('/')

    private fun nowIso(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    companion object {
        const val DEFAULT_URL = "https://sometimes-women-supported-writings.trycloudflare.com"
    }
}
