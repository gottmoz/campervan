package se.gottmoz.camperagent.ui.screens.vehicle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.components.StatusCard
import se.gottmoz.camperagent.ui.model.VehicleTelemetry
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun VehicleScreen(vehicle: VehicleTelemetry) = ScreenScaffold("Vehicle") {
    Row(Modifier.padding(it), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("Speed", "${vehicle.speedKph}", "km/h", Modifier.weight(1f))
        MetricTile("RPM", "${vehicle.rpm}", "", Modifier.weight(1f))
        MetricTile("Battery", "%.1f".format(vehicle.batteryVoltage), "V", Modifier.weight(1f))
        StatusCard("Fault codes", if (vehicle.faultCodes.isEmpty()) "None" else vehicle.faultCodes.joinToString(), vehicle.source, Modifier.weight(1f))
    }
}
