package se.gottmoz.camperagent.ui.screens.suspension

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.LockedActionTile
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.model.SuspensionTelemetry
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun SuspensionScreen(suspension: SuspensionTelemetry) = ScreenScaffold("Suspension") {
    Column(Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            suspension.corners.forEach { corner ->
                MetricTile(corner.id, "%.0f".format(corner.pressurePsi), "psi", Modifier.weight(1f))
            }
        }
        LockedActionTile("Ride height controls")
    }
}
