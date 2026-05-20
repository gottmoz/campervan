package se.gottmoz.camperagent.ui.screens.electrical

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.model.ElectricalTelemetry
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun ElectricalScreen(electrical: ElectricalTelemetry) = ScreenScaffold("Electrical") {
    Row(Modifier.padding(it), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile("12V chassis", "%.1f".format(electrical.chassisVoltage), "V", Modifier.weight(1f))
        MetricTile("House", "${electrical.houseBatteryPercent}", "%", Modifier.weight(1f))
        MetricTile("Solar", "${electrical.solarWatts}", "W", Modifier.weight(1f))
        MetricTile("Inverter", "${electrical.inverterWatts}", "W", Modifier.weight(1f))
    }
}
