package se.gottmoz.camperagent.integration.victron

object VictronMapper {
    fun fromValues(values: Map<String, Any?>): VictronTelemetry = VictronTelemetry(
        batterySocPercent = values["batterySocPercent"] as? Double,
        batteryVoltage = values["batteryVoltage"] as? Double,
        batteryCurrent = values["batteryCurrent"] as? Double,
        batteryPowerWatts = values["batteryPowerWatts"] as? Double,
        pvPowerWatts = values["pvPowerWatts"] as? Double,
        dcChargerPowerWatts = values["dcChargerPowerWatts"] as? Double,
        inverterChargerPowerWatts = values["inverterChargerPowerWatts"] as? Double,
        shoreConnected = values["shoreConnected"] as? Boolean,
        acInputSource = values["acInputSource"] as? String,
        solarYieldTodayKwh = values["solarYieldTodayKwh"] as? Double,
        rawSource = values["rawSource"] as? String ?: "mapped"
    )
}
