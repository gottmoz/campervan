package se.gottmoz.camperagent.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.CockpitBackground
import se.gottmoz.camperagent.ui.components.ConnectionPill
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.components.ModuleCard
import se.gottmoz.camperagent.ui.components.SectionHeader
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.components.WarningBanner
import se.gottmoz.camperagent.ui.model.DashboardState
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.theme.CamperColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(state: DashboardState, onModuleClick: (String) -> Unit) {
    val tabs = listOf("Overview", "Tanks", "Power", "Solar", "Climate", "Suspension", "CAN", "System")
    var selectedTab by remember { mutableIntStateOf(0) }

    CockpitBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Camper Agent", style = MaterialTheme.typography.headlineMedium)
                    Text("Premium cockpit dashboard", color = CamperColors.TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConnectionPill("SIMULATOR", ModuleStatus.Simulated)
                    ConnectionPill("READ-ONLY", ModuleStatus.Online)
                }
            }
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            WarningBanner("Safety lock active. GUI is status-only; all control features are disabled.")
            when (tabs[selectedTab]) {
                "Tanks" -> TanksTab()
                "Power" -> PowerTab(state)
                "Solar" -> SolarTab(state)
                "Climate" -> ClimateTab()
                "Suspension" -> SuspensionTab(state)
                "CAN" -> CanTab(state)
                "System" -> SystemTab(state)
                else -> OverviewTab(state, onModuleClick)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewTab(state: DashboardState, onModuleClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatusCard("Vehicle", state.vehicle.source.name, state.vehicle.source, Modifier.weight(1f))
            MetricTile("House battery", "${state.electrical.houseBatteryPercent}", "%", Modifier.weight(1f))
            MetricTile("Cabin temp", "21", "C", Modifier.weight(1f))
            StatusCard("CAN", state.canBus.source.name, state.canBus.source, Modifier.weight(1f))
        }
        SectionHeader("Modules")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.modules.take(10).forEach { module ->
                ModuleCard(module.title, module.status, iconFor(module.id), Modifier.width(154.dp)) {
                    onModuleClick(module.route)
                }
            }
        }
    }
}

@Composable
private fun TanksTab() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("Fresh water", "68", "%", Modifier.weight(1f))
        MetricTile("Grey water", "22", "%", Modifier.weight(1f))
        MetricTile("LPG", "50", "%", Modifier.weight(1f))
        MetricTile("Waste", "12", "%", Modifier.weight(1f))
    }
}

@Composable
private fun PowerTab(state: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("Starter battery", "%.1f".format(state.electrical.chassisVoltage), "V", Modifier.weight(1f))
        MetricTile("House battery", "%.1f".format(state.electrical.houseVoltage), "V", Modifier.weight(1f))
        MetricTile("Charge", "${state.electrical.houseBatteryPercent}", "%", Modifier.weight(1f))
        MetricTile("Inverter", "${state.electrical.inverterWatts}", "W", Modifier.weight(1f))
    }
}

@Composable
private fun SolarTab(state: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("Solar input", "${state.electrical.solarWatts}", "W", Modifier.weight(1f))
        MetricTile("MPPT", "Sim", "", Modifier.weight(1f))
        MetricTile("Forecast", "Good", "", Modifier.weight(1f))
        StatusCard("Charging", "Read-only", ModuleStatus.Simulated, Modifier.weight(1f))
    }
}

@Composable
private fun ClimateTab() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("Cabin", "21.4", "C", Modifier.weight(1f))
        MetricTile("Outside", "13.2", "C", Modifier.weight(1f))
        MetricTile("Humidity", "44", "%", Modifier.weight(1f))
        StatusCard("Ventilation", "Locked", ModuleStatus.Offline, Modifier.weight(1f))
    }
}

@Composable
private fun SuspensionTab(state: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        state.suspension.corners.forEach { corner ->
            MetricTile(corner.id, "%.0f".format(corner.pressurePsi), "psi", Modifier.weight(1f))
        }
    }
}

@Composable
private fun CanTab(state: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusCard("vLinker", if (state.canBus.vLinkerFound) "Found" else "Missing", state.canBus.source, Modifier.weight(1f))
        MetricTile("Frame rate", "${state.canBus.frameRate}", "fps", Modifier.weight(1f))
        StatusCard("Capture", state.canBus.captureProfile, ModuleStatus.Simulated, Modifier.weight(1f))
        StatusCard("USB permission", if (state.canBus.usbPermissionGranted) "Granted" else "Needed", ModuleStatus.Warning, Modifier.weight(1f))
    }
}

@Composable
private fun SystemTab(state: DashboardState) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatusCard("Bridge", if (state.system.bridgeConnection.connected) "Online" else "Offline", state.system.bridgeConnection.health, Modifier.weight(1f))
        StatusCard("USB", "${state.canBus.usbDeviceCount} devices", state.canBus.source, Modifier.weight(1f))
        StatusCard("Logs", "${state.canBus.logSegments} segments", ModuleStatus.Simulated, Modifier.weight(1f))
        StatusCard("Last sync", state.canBus.lastSync, ModuleStatus.Unknown, Modifier.weight(1f))
    }
}

private fun iconFor(id: String): ImageVector = when (id) {
    "vehicle" -> Icons.Default.DirectionsCar
    "electrical" -> Icons.Default.Bolt
    "water" -> Icons.Default.InvertColors
    "climate" -> Icons.Default.Thermostat
    "lighting" -> Icons.Default.Lightbulb
    "suspension" -> Icons.Default.VerticalAlignCenter
    "can" -> Icons.Default.SettingsInputComponent
    "comma" -> Icons.Default.Memory
    "system", "diagnostics" -> Icons.Default.Memory
    else -> Icons.Default.Settings
}
