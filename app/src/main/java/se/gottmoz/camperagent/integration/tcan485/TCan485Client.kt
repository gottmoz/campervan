package se.gottmoz.camperagent.integration.tcan485

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class TCan485Client {
    fun get(baseUrl: String, path: String): JSONObject = request("GET", baseUrl, path)

    fun post(baseUrl: String, path: String, body: JSONObject = JSONObject()): JSONObject =
        request("POST", baseUrl, path, body)

    private fun request(method: String, baseUrl: String, path: String, body: JSONObject? = null): JSONObject {
        val url = "${normalizeBaseUrl(baseUrl)}$path"
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 3_000
            readTimeout = 5_000
            setRequestProperty("Accept", "application/json")
            if (body != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
        }
        if (body != null) {
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(redactSensitive(body).toString())
            }
        }
        val responseCode = connection.responseCode
        val text = runCatching {
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
        }.getOrDefault("")
        val parsed = runCatching { JSONObject(text) }.getOrElse {
            JSONObject().put("raw", text)
        }
        if (!parsed.has("ok")) parsed.put("ok", responseCode in 200..299)
        return parsed
    }

    private fun normalizeBaseUrl(baseUrl: String): String {
        val trimmed = baseUrl.trim().trimEnd('/')
        require(trimmed.length <= 256) { "T-CAN485 URL too long" }
        require(trimmed.startsWith("http://") || trimmed.startsWith("https://")) { "URL must start with http:// or https://" }
        return trimmed
    }

    private fun redactSensitive(json: JSONObject): JSONObject {
        val copy = JSONObject(json.toString())
        if (copy.optString("password").isBlank()) copy.remove("password")
        return copy
    }
}
