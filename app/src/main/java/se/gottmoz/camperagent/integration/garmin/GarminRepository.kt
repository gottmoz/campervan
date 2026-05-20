package se.gottmoz.camperagent.integration.garmin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GarminRepository {
    private val discovery = Nmea2000Discovery()
    private val _telemetry = MutableStateFlow(GarminTelemetry())
    val telemetry: StateFlow<GarminTelemetry> = _telemetry

    fun recordFrame(canId: Int, data: ByteArray) {
        discovery.observe(canId, data)
        _telemetry.value = _telemetry.value.copy(discoveredPgns = discovery.snapshot())
    }

    fun simulated(): GarminTelemetry = GarminTelemetry(
        tankLevels = mapOf("Fresh" to 68.0, "Grey" to 38.0),
        switchCircuits = listOf("Cabin lights", "Water pump", "Awning"),
        alarms = emptyList(),
        discoveredPgns = listOf(
            NmeaPgnSample(127505, 12, "Fluid Level", System.currentTimeMillis(), 0.5),
            NmeaPgnSample(129025, 8, "Position Rapid Update", System.currentTimeMillis(), 1.0)
        )
    )
}
