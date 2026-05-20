package se.gottmoz.camperagent.integration.bms

import se.gottmoz.camperagent.integration.victron.VictronTelemetry

object BatteryBmsMapper {
    fun fromVictronGx(telemetry: VictronTelemetry): BatteryBmsTelemetry = BatteryBmsTelemetry(
        socPercent = telemetry.batterySocPercent,
        voltage = telemetry.batteryVoltage,
        current = telemetry.batteryCurrent,
        powerWatts = telemetry.batteryPowerWatts,
        chargeAllowed = null,
        dischargeAllowed = null,
        source = "victron_gx_can_bms",
        protocol = "Victron/GX mapped telemetry"
    )

    fun unknownRaw(source: String): BatteryBmsTelemetry = BatteryBmsTelemetry(source = source, protocol = "unknown")
}
