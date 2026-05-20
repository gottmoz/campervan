package se.gottmoz.camperagent.integration.bms

data class BatteryBmsTelemetry(
    val socPercent: Double? = null,
    val voltage: Double? = null,
    val current: Double? = null,
    val powerWatts: Double? = null,
    val remainingCapacityAh: Double? = null,
    val fullCapacityAh: Double? = 320.0,
    val cellVoltages: List<Double> = emptyList(),
    val minCellVoltage: Double? = null,
    val maxCellVoltage: Double? = null,
    val cellDeltaMv: Double? = null,
    val temperaturesC: List<Double> = emptyList(),
    val chargeAllowed: Boolean? = null,
    val dischargeAllowed: Boolean? = null,
    val balancingActive: Boolean? = null,
    val chargeMosfetOn: Boolean? = null,
    val dischargeMosfetOn: Boolean? = null,
    val warnings: List<String> = emptyList(),
    val alarms: List<String> = emptyList(),
    val cycleCount: Int? = null,
    val source: String = "none",
    val protocol: String = "unknown",
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

data class SmartSolarMpptTelemetry(val powerWatts: Double? = null, val batteryVoltage: Double? = null)
data class RenogyDcDcTelemetry(val powerWatts: Double? = null, val chargeCurrentA: Double? = null)
data class OrionDcDcTelemetry(val powerWatts: Double? = null, val chargeCurrentA: Double? = null)
data class ShoreTelemetry(val connected: Boolean? = null, val powerWatts: Double? = null)

data class CamperPowerTelemetry(
    val batteryBms: BatteryBmsTelemetry = BatteryBmsTelemetry(),
    val smartSolar: SmartSolarMpptTelemetry = SmartSolarMpptTelemetry(),
    val renogyDcDc: RenogyDcDcTelemetry = RenogyDcDcTelemetry(),
    val orionDcDc: OrionDcDcTelemetry = OrionDcDcTelemetry(),
    val shore: ShoreTelemetry = ShoreTelemetry(),
    val readOnly: Boolean = true,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
