package se.gottmoz.camperagent.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CamperScheme = darkColorScheme(
    primary = CamperColors.Accent,
    secondary = CamperColors.Marine,
    background = CamperColors.Background,
    surface = CamperColors.Surface,
    surfaceVariant = CamperColors.SurfaceHigh,
    onPrimary = CamperColors.Background,
    onSecondary = CamperColors.Background,
    onBackground = CamperColors.TextPrimary,
    onSurface = CamperColors.TextPrimary,
    onSurfaceVariant = CamperColors.TextMuted,
    error = CamperColors.Critical
)

@Composable
fun CamperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CamperScheme,
        typography = CamperTypography,
        content = content
    )
}
