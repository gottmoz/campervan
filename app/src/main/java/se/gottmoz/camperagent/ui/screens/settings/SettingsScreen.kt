package se.gottmoz.camperagent.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.LockedActionTile
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.model.BridgeStatus
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun SettingsScreen(bridge: BridgeStatus) = ScreenScaffold("Settings") {
    Column(Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusCard("Theme", "Dark cockpit", ModuleStatus.Online)
        StatusCard("Bridge URL", bridge.url, bridge.health)
        StatusCard("Logging profile", "Read-only simulator", ModuleStatus.Simulated)
        StatusCard("Safety lock", "Enabled", ModuleStatus.Online)
        LockedActionTile("Developer controls")
    }
}
