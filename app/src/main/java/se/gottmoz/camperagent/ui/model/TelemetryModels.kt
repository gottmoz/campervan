package se.gottmoz.camperagent.ui.model

enum class ModuleStatus { Online, Offline, Simulated, Unknown, Warning, Critical }
enum class SafetyLevel { ReadOnly, LockedControl, FutureControl }

data class Capability(
    val id: String,
    val title: String,
    val enabled: Boolean = false,
    val readOnly: Boolean = true
)

data class CamperModule(
    val id: String,
    val title: String,
    val icon: String,
    val route: String,
    val status: ModuleStatus,
    val capabilities: List<Capability> = emptyList(),
    val safetyLevel: SafetyLevel = SafetyLevel.ReadOnly,
    val enabled: Boolean = true,
    val readOnly: Boolean = true
)

data class VehicleTelemetry(
    val speedKph: Int,
    val rpm: Int,
    val batteryVoltage: Double,
    val coolantTempC: Int,
    val intakeTempC: Int,
    val faultCodes: List<String>,
    val source: ModuleStatus
)

data class ElectricalTelemetry(
    val chassisVoltage: Double,
    val houseVoltage: Double,
    val houseBatteryPercent: Int,
    val solarWatts: Int,
    val inverterWatts: Int,
    val source: ModuleStatus
)

data class LightingZone(
    val id: String,
    val title: String,
    val levelPercent: Int,
    val status: ModuleStatus,
    val locked: Boolean = true
)

data class SuspensionCorner(
    val id: String,
    val pressurePsi: Double,
    val heightMm: Int
)

data class SuspensionTelemetry(
    val mode: String,
    val corners: List<SuspensionCorner>,
    val source: ModuleStatus
)

data class CanBusStatus(
    val usbDeviceCount: Int,
    val vLinkerFound: Boolean,
    val usbPermissionGranted: Boolean,
    val adapterState: String,
    val hsCanOnline: Boolean,
    val msCanOnline: Boolean,
    val frameRate: Int,
    val latestFrames: List<String>,
    val captureProfile: String,
    val logSegments: Int,
    val lastSync: String,
    val source: ModuleStatus
)

data class CommaNodeStatus(
    val deviceOnline: Boolean,
    val openpilotRunning: Boolean?,
    val agnosVersion: String,
    val openpilotVersion: String,
    val gitCommit: String,
    val lastRoute: String,
    val gpsLock: Boolean,
    val cameraStatus: String,
    val canSeen: Boolean,
    val canFrames: Int,
    val canBuses: String,
    val uniqueCanAddresses: Int,
    val lastSync: String,
    val storageFreeMb: Int?,
    val safetyMode: String,
    val transitUpstreamSupport: String,
    val transitResearchMode: String,
    val transitFingerprintStatus: String,
    val transitFirmwareStatus: String,
    val transitCanSummaryStatus: String,
    val transitPortReadiness: String,
    val source: ModuleStatus
)

typealias CommaNodeUiState = CommaNodeStatus

data class SystemHealth(
    val androidVersion: String,
    val device: String,
    val appVersion: String,
    val bridgeConnection: BridgeStatus,
    val agentHealth: ModuleStatus,
    val storageStatus: String,
    val diagnostics: List<String>,
    val source: ModuleStatus
)

data class BridgeStatus(
    val connected: Boolean,
    val url: String,
    val lastSync: String,
    val health: ModuleStatus
)

data class DashboardState(
    val modules: List<CamperModule>,
    val vehicle: VehicleTelemetry,
    val electrical: ElectricalTelemetry,
    val lightingZones: List<LightingZone>,
    val suspension: SuspensionTelemetry,
    val canBus: CanBusStatus,
    val commaNode: CommaNodeStatus,
    val system: SystemHealth
)
