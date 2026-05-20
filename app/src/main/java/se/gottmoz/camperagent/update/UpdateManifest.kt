package se.gottmoz.camperagent.update

data class UpdateManifest(
    val jobId: String,
    val target: String,
    val version: String,
    val artifactUrl: String,
    val artifactSha256: String,
    val verb: String,
    val expiresAt: Long,
    val nonce: String,
    val sequence: Long,
    val signature: String
) {
    fun isAllowedFor(targetNode: String, nowEpochSeconds: Long): Boolean {
        return target == targetNode &&
            verb in ALLOWED_VERBS &&
            expiresAt > nowEpochSeconds &&
            artifactSha256.length == 64
    }

    companion object {
        val ALLOWED_VERBS = setOf("update.agent", "restart.service")
    }
}
