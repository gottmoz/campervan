package se.gottmoz.camperagent.integration.victron

data class VictronSettings(
    val enabled: Boolean = false,
    val mode: VictronMode = VictronMode.GxLan,
    val host: String = "",
    val modbusPort: Int = 502,
    val mqttPort: Int = 1883,
    val readOnly: Boolean = true
)

enum class VictronMode {
    GxLan,
    ModbusTcp,
    Mqtt,
    VeDirectUsb
}

data class VictronTelemetry(
    val batterySocPercent: Double? = null,
    val batteryVoltage: Double? = null,
    val batteryCurrent: Double? = null,
    val batteryPowerWatts: Double? = null,
    val pvPowerWatts: Double? = null,
    val dcChargerPowerWatts: Double? = null,
    val inverterChargerPowerWatts: Double? = null,
    val shoreConnected: Boolean? = null,
    val acInputSource: String? = null,
    val solarYieldTodayKwh: Double? = null,
    val rawSource: String = "none",
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
