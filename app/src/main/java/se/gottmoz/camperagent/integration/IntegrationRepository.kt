package se.gottmoz.camperagent.integration

import org.json.JSONArray
import org.json.JSONObject
import se.gottmoz.camperagent.integration.bms.BatteryBmsRepository
import se.gottmoz.camperagent.integration.bms.BuiltInBatteryProfiles
import se.gottmoz.camperagent.integration.settings.IntegrationConnectionState
import se.gottmoz.camperagent.integration.settings.IntegrationHealth

class IntegrationRepository(private val settingsStore: IntegrationSettingsStore) {
    private val batteryBmsRepository = BatteryBmsRepository()

    fun snapshot(): JSONObject {
        return JSONObject()
            .put("mode", "simulated")
            .put("readOnly", true)
            .put("batteryBms", batteryBmsSnapshot())
            .put("batteryBmsSettings", settingsStore.getBatteryBmsSettings())
            .put("victronSettings", settingsStore.getVictronSettings())
            .put("garminSettings", settingsStore.getGarminSettings())
            .put("obdSettings", settingsStore.getObdSettings())
            .put("health", JSONArray(health().map { it.toJson() }))
    }

    fun health(): List<IntegrationHealth> = listOf(
        IntegrationHealth("battery_bms", "PUPVWMHB LiFePO4 BMS", IntegrationConnectionState.Stale),
        IntegrationHealth("victron", "Victron", IntegrationConnectionState.Disconnected),
        IntegrationHealth("garmin", "Garmin/NMEA", IntegrationConnectionState.Disconnected),
        IntegrationHealth("obd", "Ford OBD", IntegrationConnectionState.PermissionRequired),
        IntegrationHealth("usb", "USB subsystem", IntegrationConnectionState.Disconnected)
    )

    private fun IntegrationHealth.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("label", label)
        .put("state", state.name)
        .put("readOnly", readOnly)
        .put("lastSeenEpochMs", lastSeenEpochMs ?: JSONObject.NULL)
        .put("error", error ?: JSONObject.NULL)

    fun batteryBmsSnapshot(): JSONObject {
        val profile = BuiltInBatteryProfiles.pupvwmhbLiFePo4
        val bms = batteryBmsRepository.simulated()
        return JSONObject()
            .put("profile", JSONObject()
                .put("id", profile.id)
                .put("displayName", profile.displayName)
                .put("brand", profile.brand)
                .put("chemistry", profile.chemistry.name)
                .put("nominalVoltage", profile.nominalVoltage)
                .put("capacityAh", profile.capacityAh)
                .put("bmsContinuousCurrentAmp", profile.bmsContinuousCurrentAmp)
                .put("notes", profile.notes))
            .put("telemetry", JSONObject()
                .put("socPercent", bms.socPercent)
                .put("voltage", bms.voltage)
                .put("current", bms.current)
                .put("powerWatts", bms.powerWatts)
                .put("remainingCapacityAh", bms.remainingCapacityAh)
                .put("chargeAllowed", bms.chargeAllowed)
                .put("dischargeAllowed", bms.dischargeAllowed)
                .put("warnings", JSONArray(bms.warnings))
                .put("alarms", JSONArray(bms.alarms))
                .put("source", bms.source)
                .put("protocol", bms.protocol)
                .put("updatedAtEpochMs", bms.updatedAtEpochMs))
            .put("readOnly", true)
    }
}
