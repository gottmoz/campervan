package se.gottmoz.camperagent.ui.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import android.util.Log
import org.json.JSONObject
import se.gottmoz.camperagent.ui.model.CanBusStatus
import se.gottmoz.camperagent.ui.model.CommaNodeStatus
import se.gottmoz.camperagent.ui.model.DashboardState
import se.gottmoz.camperagent.ui.model.ElectricalTelemetry
import se.gottmoz.camperagent.ui.model.ModuleStatus
import se.gottmoz.camperagent.ui.model.SystemHealth
import se.gottmoz.camperagent.ui.model.VehicleTelemetry
import se.gottmoz.camperagent.usb.UsbInventory
import java.net.HttpURLConnection
import java.net.URL

class RealCamperRepository(
    private val context: Context,
    private val fallback: SimulatedCamperRepository = SimulatedCamperRepository()
) : CamperRepository, VehicleRepository, ElectricalRepository, CanRepository, SystemRepository {
    override val dashboard: Flow<DashboardState> = fallback.dashboard.map { state ->
        val usb = UsbInventory(context).collect()
        val commaNode = fetchCommaNodeStatus() ?: state.commaNode
        state.copy(
            canBus = state.canBus.copy(
                usbDeviceCount = usb.devices.size,
                vLinkerFound = usb.devices.any { it.productName?.contains("vLinker", ignoreCase = true) == true },
                usbPermissionGranted = usb.devices.any { it.hasPermission },
                source = if (usb.devices.isEmpty()) ModuleStatus.Simulated else ModuleStatus.Online
            ),
            commaNode = commaNode,
            modules = state.modules.map { module ->
                if (module.id == "comma") module.copy(status = commaNode.source) else module
            },
            system = state.system.copy(
                bridgeConnection = state.system.bridgeConnection.copy(
                    connected = commaNode.source == ModuleStatus.Online,
                    url = BRIDGE_URLS.first(),
                    lastSync = commaNode.lastSync,
                    health = if (commaNode.source == ModuleStatus.Online) ModuleStatus.Online else ModuleStatus.Offline
                )
            )
        )
    }.flowOn(Dispatchers.IO)

    override val vehicle: Flow<VehicleTelemetry> = dashboard.map { it.vehicle }
    override val electrical: Flow<ElectricalTelemetry> = dashboard.map { it.electrical }
    override val canBus: Flow<CanBusStatus> = dashboard.map { it.canBus }
    override val system: Flow<SystemHealth> = dashboard.map { it.system }

    // TODO: connect read-only TelemetryService snapshots and log database when those APIs exist.
    // TODO: keep all future commands behind explicit policy and permission gates.

    private fun fetchCommaNodeStatus(): CommaNodeStatus? {
        for (baseUrl in BRIDGE_URLS) {
            val result = runCatching {
                val status = readBridgeJson(baseUrl, "/api/comma/latest-status")
                    ?.getJSONObject("status")
                    ?: return@runCatching null
                val canSummary = readBridgeJson(baseUrl, "/api/comma/latest-can-summary")
                    ?.getJSONObject("status")
                parseCommaStatus(status, canSummary)
            }.onFailure { error ->
                Log.w(TAG, "Bridge fetch failed for $baseUrl", error)
            }.getOrNull()
            if (result != null) return result
        }
        return null
    }

    private fun readBridgeJson(baseUrl: String, path: String): JSONObject? {
        val connection = URL("$baseUrl$path").openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 800
        connection.readTimeout = 800
        connection.useCaches = false
        return try {
            if (connection.responseCode != 200) return null
            JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCommaStatus(status: JSONObject, canSummary: JSONObject?): CommaNodeStatus {
        val sensors = status.optJSONObject("sensors")
        val online = status.optBoolean("online", true)
        val frameCount = canSummary?.optInt("frame_count", status.optJSONObject("can_summary")?.optInt("frames", 0) ?: 0) ?: 0
        val busCount = canSummary?.optInt("bus_count", 0) ?: 0
        return CommaNodeStatus(
            deviceOnline = online,
            openpilotRunning = if (status.isNull("openpilot_running")) null else status.optBoolean("openpilot_running"),
            agnosVersion = status.optString("agnos_version", "unknown"),
            openpilotVersion = status.optString("openpilot_version", "unknown"),
            gitCommit = status.optString("git_commit", "unknown"),
            lastRoute = status.optString("last_route", "unknown"),
            gpsLock = status.optString("gps_status", "").equals("lock", ignoreCase = true) || (sensors?.optBoolean("gps_lock", false) ?: false),
            cameraStatus = status.optString("camera_status", sensors?.optString("camera_status", "unknown") ?: "unknown"),
            canSeen = status.optBoolean("can_seen", false) || frameCount > 0,
            canFrames = frameCount,
            canBuses = if (busCount == 0) "none" else busCount.toString(),
            uniqueCanAddresses = canSummary?.let { summary -> summary.optInt("unique_arbitration_ids", summary.optInt("unique_addresses", 0)) }
                ?: status.optJSONObject("can_summary")?.optInt("unique_addresses", 0)
                ?: 0,
            lastSync = status.optString("reported_at", status.optString("last_sync", "unknown")),
            storageFreeMb = if (status.isNull("storage_free_mb")) null else status.optInt("storage_free_mb"),
            safetyMode = status.optString("safety_mode", "READ ONLY").uppercase(),
            transitUpstreamSupport = "Not found",
            transitResearchMode = "Research / Read-only",
            transitFingerprintStatus = "missing",
            transitFirmwareStatus = "missing",
            transitCanSummaryStatus = if (canSummary != null) "collected" else "missing",
            transitPortReadiness = "needs analysis",
            source = if (online) ModuleStatus.Online else ModuleStatus.Offline
        )
    }

    companion object {
        private val BRIDGE_URLS = listOf(
            "http://192.168.50.237:8765",
            "http://10.0.2.2:8765"
        )
        private const val TAG = "RealCamperRepository"
    }
}
