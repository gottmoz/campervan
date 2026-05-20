package se.gottmoz.camperagent.ui.screens.system

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.model.SystemHealth
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun SystemScreen(system: SystemHealth) = ScreenScaffold("System") {
    Row(Modifier.padding(it), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("App", system.appVersion, "", Modifier.weight(1f))
        StatusCard("Bridge", system.bridgeConnection.url, system.bridgeConnection.health, Modifier.weight(1f))
        StatusCard("Agent", system.agentHealth.name, system.agentHealth, Modifier.weight(1f))
        MetricTile("Storage", system.storageStatus, "", Modifier.weight(1f))
    }
}
