package se.gottmoz.camperagent.update

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class UpdatePoller(
    private val queueUrl: String,
    private val targetNode: String = "hikity-android"
) {
    fun poll(): List<UpdateManifest> {
        val connection = URL("${queueUrl.trimEnd('/')}/nodes/$targetNode/jobs").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 2_000
        connection.readTimeout = 2_000
        connection.useCaches = false
        return try {
            if (connection.responseCode != 200) return emptyList()
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            val jobs = JSONObject(raw).optJSONArray("jobs") ?: return emptyList()
            buildList {
                for (index in 0 until jobs.length()) {
                    jobs.optJSONObject(index)?.toManifest()?.let { manifest ->
                        if (manifest.isAllowedFor(targetNode, System.currentTimeMillis() / 1000L)) {
                            add(manifest)
                        }
                    }
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toManifest(): UpdateManifest? {
        val required = listOf(
            "job_id",
            "target",
            "version",
            "artifact_url",
            "artifact_sha256",
            "verb",
            "expires_at",
            "nonce",
            "sequence",
            "signature"
        )
        if (required.any { !has(it) }) return null
        return UpdateManifest(
            jobId = getString("job_id"),
            target = getString("target"),
            version = getString("version"),
            artifactUrl = getString("artifact_url"),
            artifactSha256 = getString("artifact_sha256"),
            verb = getString("verb"),
            expiresAt = getLong("expires_at"),
            nonce = getString("nonce"),
            sequence = getLong("sequence"),
            signature = getString("signature")
        )
    }
}
