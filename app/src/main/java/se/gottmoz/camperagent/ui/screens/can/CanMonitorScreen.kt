package se.gottmoz.camperagent.ui.screens.can

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.LockedActionTile
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.model.CanBusStatus
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun CanMonitorScreen(can: CanBusStatus) = ScreenScaffold("CAN Monitor") {
    Column(Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile("USB devices", "${can.usbDeviceCount}", "", Modifier.weight(1f))
            StatusCard("vLinker", if (can.vLinkerFound) "Found" else "Not found", if (can.vLinkerFound) ModuleStatus.Online else ModuleStatus.Offline, Modifier.weight(1f))
            MetricTile("Frame rate", "${can.frameRate}", "fps", Modifier.weight(1f))
            StatusCard("Session", can.adapterState, can.source, Modifier.weight(1f))
        }
        can.latestFrames.forEach { Text(it) }
        LockedActionTile("Export log")
    }
}
