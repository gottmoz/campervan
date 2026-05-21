package se.gottmoz.camperagent.integration.tcan485

import android.content.Context
import org.json.JSONObject

class TCan485SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("integration_settings", Context.MODE_PRIVATE)

    fun get(): JSONObject = JSONObject(prefs.getString(KEY, null) ?: TCan485Settings().toJson().toString())

    fun save(json: JSONObject): JSONObject {
        val sanitized = JSONObject(json.toString())
            .put("readOnly", true)
        prefs.edit().putString(KEY, sanitized.toString()).apply()
        return sanitized
    }

    companion object {
        private const val KEY = "tcan485"
    }
}
