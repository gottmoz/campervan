package se.gottmoz.camperagent.ui.data

import kotlinx.coroutines.flow.Flow
import se.gottmoz.camperagent.ui.model.CanBusStatus
import se.gottmoz.camperagent.ui.model.DashboardState
import se.gottmoz.camperagent.ui.model.ElectricalTelemetry
import se.gottmoz.camperagent.ui.model.SystemHealth
import se.gottmoz.camperagent.ui.model.VehicleTelemetry

interface CamperRepository {
    val dashboard: Flow<DashboardState>
}

interface VehicleRepository {
    val vehicle: Flow<VehicleTelemetry>
}

interface ElectricalRepository {
    val electrical: Flow<ElectricalTelemetry>
}

interface CanRepository {
    val canBus: Flow<CanBusStatus>
}

interface SystemRepository {
    val system: Flow<SystemHealth>
}

interface AgentStatusProvider {
    fun agentHealth(): String
}

interface UsbStatusProvider {
    fun usbDeviceCount(): Int
    fun vLinkerFound(): Boolean
    fun usbPermissionGranted(): Boolean
}

interface CanSessionStatusProvider {
    fun adapterState(): String
    fun captureProfile(): String
}

interface LogStatusProvider {
    fun logSegments(): Int
    fun lastSyncTimestamp(): String
}
