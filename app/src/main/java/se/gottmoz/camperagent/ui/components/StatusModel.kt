package se.gottmoz.camperagent.ui.components

import androidx.compose.runtime.Immutable
import se.gottmoz.camperagent.adapter.AdapterSessionState

enum class CamperStatusTone {
    Neutral,
    Info,
    Success,
    Warning,
    Error
}

@Immutable
data class CamperStatus(
    val label: String,
    val detail: String,
    val tone: CamperStatusTone
)

fun AdapterSessionState.toCamperStatus(): CamperStatus = when (this) {
    AdapterSessionState.Disconnected -> CamperStatus(
        label = "Disconnected",
        detail = "No adapter session is active.",
        tone = CamperStatusTone.Neutral
    )
    AdapterSessionState.Enumerated -> CamperStatus(
        label = "USB found",
        detail = "Adapter is visible; permission is still required.",
        tone = CamperStatusTone.Info
    )
    AdapterSessionState.PermissionGranted -> CamperStatus(
        label = "Permission granted",
        detail = "Adapter can be opened for read-only setup.",
        tone = CamperStatusTone.Success
    )
    AdapterSessionState.PortOpened -> CamperStatus(
        label = "Port open",
        detail = "Waiting for adapter identity.",
        tone = CamperStatusTone.Info
    )
    AdapterSessionState.IdentityKnown -> CamperStatus(
        label = "Identity known",
        detail = "Adapter is ready for read-only capture.",
        tone = CamperStatusTone.Success
    )
    AdapterSessionState.ReadOnlyCapture -> CamperStatus(
        label = "Read-only capture",
        detail = "Telemetry capture is active without write/control commands.",
        tone = CamperStatusTone.Success
    )
    AdapterSessionState.Error -> CamperStatus(
        label = "Error",
        detail = "Adapter session needs attention.",
        tone = CamperStatusTone.Error
    )
}
