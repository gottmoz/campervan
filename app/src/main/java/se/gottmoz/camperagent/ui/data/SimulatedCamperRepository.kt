package se.gottmoz.camperagent.ui.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import se.gottmoz.camperagent.ui.model.BridgeStatus
import se.gottmoz.camperagent.ui.model.CamperModule
import se.gottmoz.camperagent.ui.model.CanBusStatus
import se.gottmoz.camperagent.ui.model.CommaNodeStatus
import se.gottmoz.camperagent.ui.model.DashboardState
import se.gottmoz.camperagent.ui.model.ElectricalTelemetry
import se.gottmoz.camperagent.ui.model.LightingZone
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.model.SuspensionCorner
import se.gottmoz.camperagent.ui.model.SuspensionTelemetry
import se.gottmoz.camperagent.ui.model.SystemHealth
import se.gottmoz.camperagent.ui.model.VehicleTelemetry

class SimulatedCamperRepository : CamperRepository, VehicleRepository, ElectricalRepository, CanRepository, SystemRepository {
    private val ticks = flow {
        var tick = 0
        while (true) {
            emit(tick++)
            delay(1_000)
        }
    }

    override val dashboard: Flow<DashboardState> = ticks.map { buildDashboard(it) }
    override val vehicle: Flow<VehicleTelemetry> = dashboard.map { it.vehicle }
    override val electrical: Flow<ElectricalTelemetry> = dashboard.map { it.electrical }
    override val canBus: Flow<CanBusStatus> = dashboard.map { it.canBus }
    override val system: Flow<SystemHealth> = dashboard.map { it.system }

    fun initialState(): DashboardState = buildDashboard(0)

    private fun buildDashboard(tick: Int): DashboardState {
        val electrical = ElectricalTelemetry(
            chassisVoltage = 12.5 + wave(tick, 0.2),
            houseVoltage = 13.1 + wave(tick + 3, 0.35),
            houseBatteryPercent = 82 + tick % 5,
            solarWatts = 120 + tick % 70,
            inverterWatts = 0,
            source = ModuleStatus.Simulated
        )
        val can = CanBusStatus(
            usbDeviceCount = 1,
            vLinkerFound = true,
            usbPermissionGranted = false,
            adapterState = "Enumerated",
            hsCanOnline = false,
            msCanOnline = false,
            frameRate = 40 + tick % 12,
            latestFrames = listOf("18DAF110 03 41 0C 1A F8", "7E8 04 41 0D 00 00", "18FEF100 8F 01 22 00"),
            captureProfile = "Read-only simulator",
            logSegments = 3 + tick % 4,
            lastSync = "simulated ${tick}s",
            source = ModuleStatus.Simulated
        )
        val vehicle = VehicleTelemetry(
            speedKph = 0,
            rpm = 0,
            batteryVoltage = electrical.chassisVoltage,
            coolantTempC = 71 + tick % 3,
            intakeTempC = 23 + tick % 2,
            faultCodes = emptyList(),
            source = ModuleStatus.Simulated
        )
        val lighting = listOf("Cabin", "Kitchen", "Bed", "Garage", "Exterior").mapIndexed { index, title ->
            LightingZone(title.lowercase(), title, 20 + index * 12, ModuleStatus.Simulated)
        }
        val suspension = SuspensionTelemetry(
            mode = "Park level",
            corners = listOf("FL", "FR", "RL", "RR").mapIndexed { index, id ->
                SuspensionCorner(id, 66.0 + index + wave(tick + index, 1.2), 402 + index * 3)
            },
            source = ModuleStatus.Simulated
        )
        val bridge = BridgeStatus(false, "http://10.0.0.2:8787", "never", ModuleStatus.Offline)
        val commaNode = CommaNodeStatus(
            deviceOnline = false,
            openpilotRunning = null,
            agnosVersion = "unknown",
            openpilotVersion = "unknown",
            gitCommit = "unknown",
            lastRoute = "none",
            gpsLock = false,
            cameraStatus = "simulated",
            canSeen = false,
            canFrames = 0,
            canBuses = "none",
            uniqueCanAddresses = 0,
            lastSync = "never",
            storageFreeMb = null,
            safetyMode = "READ ONLY",
            transitUpstreamSupport = "Not found",
            transitResearchMode = "Research / Read-only",
            transitFingerprintStatus = "missing",
            transitFirmwareStatus = "missing",
            transitCanSummaryStatus = "missing",
            transitPortReadiness = "needs analysis",
            source = ModuleStatus.Simulated
        )
        val system = SystemHealth(
            androidVersion = "Android head unit",
            device = "Camper cockpit",
            appVersion = "0.1.0",
            bridgeConnection = bridge,
            agentHealth = ModuleStatus.Simulated,
            storageStatus = "Logs available",
            diagnostics = listOf("Read-only safety lock active", "Simulator fallback active"),
            source = ModuleStatus.Simulated
        )
        return DashboardState(
            modules = listOf(
                CamperModule("vehicle", "Vehicle", "directions_car", "vehicle", ModuleStatus.Simulated),
                CamperModule("electrical", "Electrical", "bolt", "electrical", ModuleStatus.Simulated),
                CamperModule("water", "Water", "water_drop", "dashboard", ModuleStatus.Unknown),
                CamperModule("climate", "Climate", "device_thermostat", "dashboard", ModuleStatus.Unknown),
                CamperModule("lighting", "Lighting", "lightbulb", "lighting", ModuleStatus.Simulated),
                CamperModule("suspension", "Suspension", "air", "suspension", ModuleStatus.Simulated),
                CamperModule("comma", "Comma Node", "sensors", "comma", ModuleStatus.Simulated),
                CamperModule("can", "CAN Monitor", "settings_input_component", "can", ModuleStatus.Simulated),
                CamperModule("system", "System", "memory", "system", ModuleStatus.Simulated),
                CamperModule("diagnostics", "Diagnostics", "health_and_safety", "system", ModuleStatus.Simulated),
                CamperModule("automation", "Automation", "auto_mode", "settings", ModuleStatus.Offline)
            )
                ,
            vehicle = vehicle,
            electrical = electrical,
            lightingZones = lighting,
            suspension = suspension,
            canBus = can,
            commaNode = commaNode,
            system = system
        )
    }

    private fun wave(tick: Int, scale: Double): Double = ((tick % 8) - 4) * scale / 4.0
}
