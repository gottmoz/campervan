package se.gottmoz.camperagent.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.model.ModuleStatus

object CamperColors {
    val Background = Color(0xFF071016)
    val Surface = Color(0xFF101A22)
    val SurfaceHigh = Color(0xFF172632)
    val Border = Color(0xFF2C4150)
    val TextPrimary = Color(0xFFEAF2F6)
    val TextMuted = Color(0xFF90A4AE)
    val Accent = Color(0xFF37D3C4)
    val Marine = Color(0xFF4DA3FF)
    val Ok = Color(0xFF39D98A)
    val Warning = Color(0xFFFFC857)
    val Critical = Color(0xFFFF5C7A)
    val Offline = Color(0xFF65737E)
    val Simulated = Color(0xFF8A7CFF)
}

fun statusColor(status: ModuleStatus): Color = when (status) {
    ModuleStatus.Online -> CamperColors.Ok
    ModuleStatus.Warning -> CamperColors.Warning
    ModuleStatus.Critical -> CamperColors.Critical
    ModuleStatus.Offline -> CamperColors.Offline
    ModuleStatus.Simulated -> CamperColors.Simulated
    ModuleStatus.Unknown -> CamperColors.TextMuted
}

object CamperSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

object CamperShapes {
    val panel = RoundedCornerShape(8.dp)
    val control = RoundedCornerShape(6.dp)
    val pill = RoundedCornerShape(50)
}
