package se.gottmoz.camperagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import se.gottmoz.camperagent.ui.theme.CamperColors
import se.gottmoz.camperagent.ui.theme.CamperShapes
import se.gottmoz.camperagent.ui.theme.CamperSpacing
import se.gottmoz.camperagent.ui.model.ModuleStatus

@Composable
fun StatusCard(title: String, value: String, status: ModuleStatus, modifier: Modifier = Modifier) {
    CamperStatusCard(
        status = CamperStatus(title, value, status.toTone()),
        modifier = modifier
    )
}

@Composable
fun MetricTile(label: String, value: String, unit: String = "", modifier: Modifier = Modifier) {
    CamperPanel(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (unit.isBlank()) value else "$value $unit",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ModuleCard(
    title: String,
    status: ModuleStatus,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val sweep by rememberInfiniteTransition(label = "moduleSweep").animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(2_600, easing = LinearEasing), RepeatMode.Reverse),
        label = "moduleSweep"
    )
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = CamperShapes.panel,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            CamperColors.SurfaceHigh.copy(alpha = 0.96f),
                            CamperColors.Surface.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(1.dp, status.toTone().contentColor().copy(alpha = 0.28f), CamperShapes.panel)
                .padding(CamperSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CamperSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedModuleIcon(icon = icon, status = status, wobble = sweep)
            Text(title, style = MaterialTheme.typography.titleMedium)
            ConnectionPill(status.name, status)
        }
    }
}

@Composable
fun AnimatedModuleIcon(icon: ImageVector, status: ModuleStatus, wobble: Float = 0f, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(74.dp)
            .graphicsLayer {
                rotationY = wobble
                rotationX = -wobble / 2f
                cameraDistance = 14f * density
            }
            .shadow(16.dp, CircleShape, ambientColor = status.toTone().contentColor(), spotColor = status.toTone().contentColor())
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        status.toTone().contentColor().copy(alpha = 0.45f),
                        CamperColors.SurfaceHigh,
                        CamperColors.Background
                    )
                )
            )
            .border(2.dp, Color.White.copy(alpha = 0.18f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
    }
}

@Composable
fun CockpitBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(CamperColors.SurfaceHigh, CamperColors.Background, Color.Black),
                    center = Offset(360f, 120f),
                    radius = 900f
                )
            )
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 42f
            var y = 0f
            while (y < size.height) {
                var x = if (((y / step).toInt() % 2) == 0) 0f else step / 2f
                while (x < size.width) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.025f),
                        radius = 14f,
                        center = Offset(x, y)
                    )
                    x += step
                }
                y += step * 0.82f
            }
        }
        content()
    }
}

@Composable
fun LockedActionTile(title: String, modifier: Modifier = Modifier) {
    CamperSecondaryButton(
        text = "$title - Locked / read-only phase",
        onClick = {},
        modifier = modifier.fillMaxWidth(),
        enabled = false
    )
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun WarningBanner(text: String, modifier: Modifier = Modifier) {
    CamperStatusPill(
        status = CamperStatus("Warning", text, CamperStatusTone.Warning),
        modifier = modifier
    )
}

@Composable
fun ConnectionPill(text: String, status: ModuleStatus, modifier: Modifier = Modifier) {
    CamperStatusPill(
        status = CamperStatus(text, status.name, status.toTone()),
        modifier = modifier
    )
}

private fun ModuleStatus.toTone(): CamperStatusTone = when (this) {
    ModuleStatus.Online -> CamperStatusTone.Success
    ModuleStatus.Warning -> CamperStatusTone.Warning
    ModuleStatus.Critical -> CamperStatusTone.Error
    ModuleStatus.Offline -> CamperStatusTone.Neutral
    ModuleStatus.Simulated -> CamperStatusTone.Info
    ModuleStatus.Unknown -> CamperStatusTone.Neutral
}

@Composable
private fun CamperStatusTone.contentColor(): Color = when (this) {
    CamperStatusTone.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
    CamperStatusTone.Info -> MaterialTheme.colorScheme.primary
    CamperStatusTone.Success -> CamperColors.Ok
    CamperStatusTone.Warning -> CamperColors.Warning
    CamperStatusTone.Error -> CamperColors.Critical
}

@Composable
fun CamperSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CamperSpacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        content()
    }
}

@Composable
fun CamperPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = CamperShapes.panel,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(CamperSpacing.md),
            verticalArrangement = Arrangement.spacedBy(CamperSpacing.sm)
        ) {
            content()
        }
    }
}

@Composable
fun CamperInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
fun CamperStatusCard(
    status: CamperStatus,
    modifier: Modifier = Modifier
) {
    CamperPanel(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(CamperSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val colors = status.tone.colors()
            Icon(
                imageVector = status.tone.icon(),
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = status.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = status.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CamperStatusPill(
    status: CamperStatus,
    modifier: Modifier = Modifier
) {
    val colors = status.tone.colors()
    Row(
        modifier = modifier
            .background(colors.container, CamperShapes.pill)
            .padding(horizontal = CamperSpacing.sm, vertical = CamperSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(CamperSpacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = status.tone.icon(),
            contentDescription = null,
            tint = colors.content,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.content
        )
    }
}

@Composable
fun CamperPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = CamperShapes.control
    ) {
        Text(text = text)
    }
}

@Composable
fun CamperSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = CamperShapes.control,
        colors = ButtonDefaults.outlinedButtonColors()
    ) {
        Text(text = text)
    }
}

@Composable
fun CamperVerticalGap() {
    Spacer(modifier = Modifier.height(CamperSpacing.md))
}

@Stable
private data class StatusColors(
    val container: Color,
    val content: Color
)

@Composable
private fun CamperStatusTone.colors(): StatusColors = when (this) {
    CamperStatusTone.Neutral -> StatusColors(
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant
    )
    CamperStatusTone.Info -> StatusColors(
        container = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
        content = MaterialTheme.colorScheme.primary
    )
    CamperStatusTone.Success -> StatusColors(
        container = CamperColors.Ok.copy(alpha = 0.14f),
        content = CamperColors.Ok
    )
    CamperStatusTone.Warning -> StatusColors(
        container = CamperColors.Warning.copy(alpha = 0.16f),
        content = CamperColors.Warning
    )
    CamperStatusTone.Error -> StatusColors(
        container = CamperColors.Critical.copy(alpha = 0.14f),
        content = CamperColors.Critical
    )
}

private fun CamperStatusTone.icon(): ImageVector = when (this) {
    CamperStatusTone.Neutral -> Icons.Filled.RadioButtonUnchecked
    CamperStatusTone.Info -> Icons.Filled.Info
    CamperStatusTone.Success -> Icons.Filled.CheckCircle
    CamperStatusTone.Warning -> Icons.Filled.Warning
    CamperStatusTone.Error -> Icons.Filled.Error
}
