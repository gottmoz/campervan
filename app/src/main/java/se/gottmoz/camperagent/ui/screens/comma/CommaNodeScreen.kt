package se.gottmoz.camperagent.ui.screens.comma

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.model.CommaNodeUiState
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommaNodeScreen(node: CommaNodeUiState) = ScreenScaffold("Comma Node") {
    Column(Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusCard("Device", if (node.deviceOnline) "online" else "offline", node.source, Modifier.weight(1f))
            StatusCard("openpilot", node.openpilotRunning?.let { running -> if (running) "running" else "stopped" } ?: "unknown", node.source, Modifier.weight(1f))
            StatusCard("GPS", if (node.gpsLock) "lock" else "unknown", node.source, Modifier.weight(1f))
            StatusCard("Camera", node.cameraStatus, node.source, Modifier.weight(1f))
            StatusCard("CAN", if (node.canSeen) "seen" else "not seen", if (node.canSeen) ModuleStatus.Online else node.source, Modifier.weight(1f))
            MetricTile("Frames", "${node.canFrames}", "", Modifier.weight(1f))
            MetricTile("Unique IDs", "${node.uniqueCanAddresses}", "", Modifier.weight(1f))
            StatusCard("Last route", node.lastRoute, node.source, Modifier.weight(1f))
            StatusCard("AGNOS", node.agnosVersion, node.source, Modifier.weight(1f))
            StatusCard("openpilot version", node.openpilotVersion, node.source, Modifier.weight(1f))
            StatusCard("Git commit", node.gitCommit.take(12), node.source, Modifier.weight(1f))
            StatusCard("Last sync", node.lastSync, node.source, Modifier.weight(1f))
            MetricTile("Storage", node.storageFreeMb?.toString() ?: "unknown", "MB", Modifier.weight(1f))
            StatusCard("Safety mode", node.safetyMode, ModuleStatus.Online, Modifier.weight(1f))
            StatusCard("Transit support", node.transitUpstreamSupport, ModuleStatus.Warning, Modifier.weight(1f))
            StatusCard("Mode", node.transitResearchMode, ModuleStatus.Online, Modifier.weight(1f))
            StatusCard("Fingerprint", node.transitFingerprintStatus, node.source, Modifier.weight(1f))
            StatusCard("Firmware", node.transitFirmwareStatus, node.source, Modifier.weight(1f))
            StatusCard("CAN summary", node.transitCanSummaryStatus, node.source, Modifier.weight(1f))
            StatusCard("Port readiness", node.transitPortReadiness, ModuleStatus.Unknown, Modifier.weight(1f))
        }
    }
}
