package se.gottmoz.camperagent.integration

import android.content.Context
import org.json.JSONObject

class IntegrationSettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("integration_settings", Context.MODE_PRIVATE)

    fun getVictronSettings(): JSONObject = JSONObject(prefs.getString("victron", null) ?: defaultVictron().toString())
    fun getBatteryBmsSettings(): JSONObject = JSONObject(prefs.getString("battery_bms", null) ?: defaultBatteryBms().toString())
    fun getGarminSettings(): JSONObject = JSONObject(prefs.getString("garmin", null) ?: defaultGarmin().toString())
    fun getObdSettings(): JSONObject = JSONObject(prefs.getString("obd", null) ?: defaultObd().toString())

    fun saveVictronSettings(json: JSONObject) = save("victron", enforceReadOnly(json))
    fun saveBatteryBmsSettings(json: JSONObject) = save("battery_bms", enforceReadOnly(json))
    fun saveGarminSettings(json: JSONObject) = save("garmin", enforceReadOnly(json))
    fun saveObdSettings(json: JSONObject) = save("obd", enforceReadOnly(json))

    private fun save(key: String, json: JSONObject) {
        prefs.edit().putString(key, json.toString()).apply()
    }

    private fun enforceReadOnly(json: JSONObject): JSONObject {
        json.put("readOnly", true)
        return json
    }

    private fun defaultVictron() = JSONObject()
        .put("enabled", true)
        .put("mode", "GxLan")
        .put("host", "cerbo-gx.local")
        .put("modbusPort", 502)
        .put("mqttPort", 1883)
        .put("gxDevice", "Cerbo GX")
        .put("preferredSource", "Cerbo GX / Venus OS")
        .put("devices", org.json.JSONArray()
            .put(JSONObject()
                .put("id", "victron_smartsolar_mppt")
                .put("displayName", "Victron SmartSolar MPPT")
                .put("type", "solar_charger")
                .put("expectedService", "com.victronenergy.solarcharger.*")
                .put("enabled", true))
            .put(JSONObject()
                .put("id", "victron_ip22_30a")
                .put("displayName", "Victron Blue Smart IP22 30A")
                .put("type", "ac_charger")
                .put("expectedService", "com.victronenergy.charger.*")
                .put("enabled", true))
            .put(JSONObject()
                .put("id", "victron_cerbo_gx")
                .put("displayName", "Victron Cerbo GX")
                .put("type", "gx")
                .put("expectedService", "com.victronenergy.system")
                .put("enabled", true)))
        .put("readOnly", true)

    private fun defaultBatteryBms() = JSONObject()
        .put("enabled", true)
        .put("profileId", "pupvwmhb_lifepo4_12v_320ah_250a")
        .put("connectionPath", "VictronCanViaGx")
        .put("canBitrate", "Auto")
        .put("protocol", "Auto detect")
        .put("readOnly", true)

    private fun defaultGarmin() = JSONObject()
        .put("enabled", false)
        .put("mode", "Nmea2000Can")
        .put("canBitrate", 250000)
        .put("readOnly", true)

    private fun defaultObd() = JSONObject()
        .put("enabled", false)
        .put("adapterType", "Auto")
        .put("baudRate", JSONObject.NULL)
        .put("protocol", "AUTO")
        .put("readOnly", true)
}
