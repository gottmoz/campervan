package se.gottmoz.camperagent.integration.obd

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ObdRepository {
    private val _telemetry = MutableStateFlow(ObdTelemetry(supportedPids = rawSupportedPids()))
    val telemetry: StateFlow<ObdTelemetry> = _telemetry

    fun simulated(): ObdTelemetry = ObdTelemetry(
        speedKph = 0,
        rpm = 780,
        coolantTempC = 86,
        intakeTempC = 24,
        mafGps = 4.2,
        throttlePercent = 12.0,
        ambientTempC = 18,
        dtcCodes = emptyList(),
        supportedPids = rawSupportedPids()
    )

    private fun rawSupportedPids() = setOf("0C", "0D", "05", "0F", "10", "11", "46")
}
