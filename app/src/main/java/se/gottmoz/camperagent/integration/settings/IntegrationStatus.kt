package se.gottmoz.camperagent.integration.settings

enum class IntegrationConnectionState {
    Disabled,
    Disconnected,
    PermissionRequired,
    Connecting,
    Online,
    Stale,
    Error
}

data class IntegrationHealth(
    val id: String,
    val label: String,
    val state: IntegrationConnectionState,
    val readOnly: Boolean = true,
    val lastSeenEpochMs: Long? = null,
    val error: String? = null
)
