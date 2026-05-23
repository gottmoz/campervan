package se.gottmoz.camperagent.integration.obd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class ObdPidMappingStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("obd_pid_library_v1", Context.MODE_PRIVATE)

    fun getMappings(): List<ObdPidMapping> {
        val raw = prefs.getString(KEY, null) ?: appContext.getSharedPreferences("obd_pid_mappings", Context.MODE_PRIVATE).getString(KEY, null)
        val saved = raw?.let {
            val array = JSONArray(it)
            (0 until array.length()).mapNotNull { index ->
                runCatching { ObdPidMapping.fromJson(array.getJSONObject(index)) }.getOrNull()
            }
        } ?: emptyList()
        return mergeDefaults(saved)
    }

    private fun mergeDefaults(saved: List<ObdPidMapping>): List<ObdPidMapping> {
        val byKey = saved.associateBy { it.functionKey }
        return ObdPidMapping.defaults().map { default ->
            val savedMapping = byKey[default.functionKey] ?: return@map default
            when (default.functionKey) {
                "generatorCurrentA", "vehicleBatteryVoltage", "alternatorDutyPercent" -> default.copy(enabled = savedMapping.enabled)
                else -> savedMapping
            }
        } +
            saved.filter { savedMapping -> ObdPidMapping.defaults().none { it.functionKey == savedMapping.functionKey } }
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
        persist(mergeDefaults(sanitized))
        return getJson()
    }

    fun setEnabled(functionKey: String, enabled: Boolean): JSONObject {
        persist(getMappings().map { if (it.functionKey == functionKey) it.copy(enabled = enabled) else it })
        return getJson()
    }

    fun reset(): JSONObject {
        prefs.edit().remove(KEY).apply()
        return getJson()
    }

    fun enabledMappings(): List<ObdPidMapping> = getMappings()
        .filter { it.enabled && it.service.isNotBlank() && it.pid.isNotBlank() }

    private fun persist(mappings: List<ObdPidMapping>) {
        prefs.edit().putString(KEY, ObdPidMapping.toJsonArray(mappings).toString()).apply()
    }

    companion object {
        private const val KEY = "mappings"
    }
}
