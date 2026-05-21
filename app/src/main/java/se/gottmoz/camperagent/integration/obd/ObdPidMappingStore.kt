package se.gottmoz.camperagent.integration.obd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ObdPidMappingStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("obd_pid_mappings", Context.MODE_PRIVATE)

    fun getMappings(): List<ObdPidMapping> {
        val raw = prefs.getString(KEY, null) ?: return ObdPidMapping.defaults()
        val array = JSONArray(raw)
        return (0 until array.length()).mapNotNull { index ->
            runCatching { ObdPidMapping.fromJson(array.getJSONObject(index)) }.getOrNull()
        }.ifEmpty { ObdPidMapping.defaults() }
    }

    fun getJson(): JSONObject = JSONObject()
        .put("profile", "Ford Transit EcoBlue 2.0 2016 - Default")
        .put("mappings", ObdPidMapping.toJsonArray(getMappings()))
        .put("readOnly", true)

    fun save(json: JSONObject): JSONObject {
        val array = json.optJSONArray("mappings") ?: JSONArray()
        val sanitized = mutableListOf<ObdPidMapping>()
        for (index in 0 until array.length()) {
            val mapping = ObdPidMapping.fromJson(array.getJSONObject(index))
            if (mapping.functionKey.isNotBlank()) sanitized += mapping
        }
        prefs.edit().putString(KEY, ObdPidMapping.toJsonArray(sanitized).toString()).apply()
        return getJson()
    }

    fun reset(): JSONObject {
        prefs.edit().remove(KEY).apply()
        return getJson()
    }

    fun enabledMappings(): List<ObdPidMapping> = getMappings()
        .filter { it.enabled && it.service.isNotBlank() && it.pid.isNotBlank() }

    companion object {
        private const val KEY = "mappings"
    }
}
