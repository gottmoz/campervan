package se.gottmoz.camperagent.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.components.WarningBanner
import se.gottmoz.camperagent.ui.theme.CamperColors

@Composable
fun ScreenScaffold(title: String, content: @Composable (PaddingValues) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(CamperColors.Background)
            .padding(18.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        WarningBanner("Read-only phase: controls are locked and no vehicle commands are available.", Modifier.padding(vertical = 10.dp))
        content(PaddingValues(top = 8.dp))
    }
}
