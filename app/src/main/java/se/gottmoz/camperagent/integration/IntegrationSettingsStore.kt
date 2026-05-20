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
        .put("enabled", false)
        .put("mode", "GxLan")
        .put("host", "")
        .put("modbusPort", 502)
        .put("mqttPort", 1883)
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
