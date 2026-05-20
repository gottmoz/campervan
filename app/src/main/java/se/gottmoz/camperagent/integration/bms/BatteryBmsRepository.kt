package se.gottmoz.camperagent.integration.bms

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BatteryBmsRepository {
    private val _telemetry = MutableStateFlow(simulated())
    val telemetry: StateFlow<BatteryBmsTelemetry> = _telemetry

    fun updateFromVictronMapped(telemetry: BatteryBmsTelemetry) {
        _telemetry.value = telemetry
    }

    fun simulated(): BatteryBmsTelemetry = BatteryBmsTelemetry(
        socPercent = 87.0,
        voltage = 13.2,
        current = -18.0,
        powerWatts = -237.6,
        remainingCapacityAh = 278.4,
        fullCapacityAh = 320.0,
        chargeAllowed = true,
        dischargeAllowed = true,
        warnings = emptyList(),
        alarms = emptyList(),
        source = "simulator_fallback",
        protocol = "PUPVWMHB discovery pending"
    )
}
