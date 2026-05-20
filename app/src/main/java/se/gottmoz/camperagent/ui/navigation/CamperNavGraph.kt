package se.gottmoz.camperagent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.gottmoz.camperagent.ui.CamperViewModel
import se.gottmoz.camperagent.ui.screens.can.CanMonitorScreen
import se.gottmoz.camperagent.ui.screens.comma.CommaNodeScreen
import se.gottmoz.camperagent.ui.screens.dashboard.DashboardScreen
import se.gottmoz.camperagent.ui.screens.electrical.ElectricalScreen
import se.gottmoz.camperagent.ui.screens.lighting.LightingScreen
import se.gottmoz.camperagent.ui.screens.settings.SettingsScreen
import se.gottmoz.camperagent.ui.screens.suspension.SuspensionScreen
import se.gottmoz.camperagent.ui.screens.system.SystemScreen
import se.gottmoz.camperagent.ui.screens.vehicle.VehicleScreen

val NavItems = listOf(
    CamperRoutes.Dashboard to Icons.Default.Dashboard,
    CamperRoutes.Vehicle to Icons.Default.DirectionsCar,
    CamperRoutes.Electrical to Icons.Default.Bolt,
    CamperRoutes.Lighting to Icons.Default.Lightbulb,
    CamperRoutes.Suspension to Icons.Default.VerticalAlignCenter,
    CamperRoutes.Can to Icons.Default.SettingsInputComponent,
    CamperRoutes.Comma to Icons.Default.Memory,
    CamperRoutes.System to Icons.Default.Memory,
    CamperRoutes.Settings to Icons.Default.Settings
)

@Composable
fun CamperNavGraph(viewModel: CamperViewModel) {
    val navController = rememberNavController()
    val state by viewModel.dashboard.collectAsStateWithLifecycle()

    NavHost(navController, startDestination = CamperRoutes.Dashboard) {
        composable(CamperRoutes.Dashboard) {
            DashboardScreen(state, onModuleClick = { route -> navController.navigate(route) })
        }
        composable(CamperRoutes.Vehicle) { VehicleScreen(state.vehicle) }
        composable(CamperRoutes.Electrical) { ElectricalScreen(state.electrical) }
        composable(CamperRoutes.Lighting) { LightingScreen(state.lightingZones) }
        composable(CamperRoutes.Suspension) { SuspensionScreen(state.suspension) }
        composable(CamperRoutes.Can) { CanMonitorScreen(state.canBus) }
        composable(CamperRoutes.Comma) { CommaNodeScreen(state.commaNode) }
        composable(CamperRoutes.System) { SystemScreen(state.system) }
        composable(CamperRoutes.Settings) { SettingsScreen(state.system.bridgeConnection) }
    }
}
