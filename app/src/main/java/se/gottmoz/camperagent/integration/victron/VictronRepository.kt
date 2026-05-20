package se.gottmoz.camperagent.integration.victron

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VictronRepository {
    private val _telemetry = MutableStateFlow(simulated())
    val telemetry: StateFlow<VictronTelemetry> = _telemetry

    fun simulated(): VictronTelemetry = VictronTelemetry(
        batterySocPercent = 87.0,
        batteryVoltage = 13.4,
        batteryCurrent = -2.6,
        batteryPowerWatts = -35.0,
        pvPowerWatts = 420.0,
        dcChargerPowerWatts = 0.0,
        inverterChargerPowerWatts = 600.0,
        shoreConnected = true,
        acInputSource = "shore",
        solarYieldTodayKwh = 3.7,
        rawSource = "simulated"
    )

    fun testConnection(settings: VictronSettings): Boolean {
        return when (settings.mode) {
            VictronMode.Mqtt -> VictronMqttClient().testConnection(settings)
            else -> VictronModbusClient().testConnection(settings)
        }
    }
}
