package se.gottmoz.camperagent.ui.screens.lighting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.LockedActionTile
import se.gottmoz.camperagent.ui.components.MetricTile
import se.gottmoz.camperagent.ui.model.LightingZone
import se.gottmoz.camperagent.ui.screens.ScreenScaffold

@Composable
fun LightingScreen(zones: List<LightingZone>) = ScreenScaffold("Lighting") {
    Column(Modifier.padding(it), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            zones.forEach { zone -> MetricTile(zone.title, "${zone.levelPercent}", "%", Modifier.weight(1f)) }
        }
        LockedActionTile("Lighting controls")
    }
}
