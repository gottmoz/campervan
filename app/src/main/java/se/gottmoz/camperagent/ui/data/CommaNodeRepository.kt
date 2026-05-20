package se.gottmoz.camperagent.ui.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import se.gottmoz.camperagent.ui.model.CommaNodeUiState
import se.gottmoz.camperagent.ui.model.ModuleStatus

interface CommaNodeRepository {
    val commaNode: Flow<CommaNodeUiState>
}

class SimulatedCommaNodeRepository : CommaNodeRepository {
    override val commaNode: Flow<CommaNodeUiState> = flowOf(
        CommaNodeUiState(
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
    )
}
