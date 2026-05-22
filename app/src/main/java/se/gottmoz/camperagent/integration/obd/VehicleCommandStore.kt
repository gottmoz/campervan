package se.gottmoz.camperagent.integration.obd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class VehicleCommandStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("vehicle_commands", Context.MODE_PRIVATE)

    fun getCommands(): List<VehicleCommandDefinition> {
        val raw = prefs.getString(KEY, null) ?: return VehicleCommandDefinition.defaults()
        val array = JSONArray(raw)
        return (0 until array.length()).mapNotNull { index ->
            runCatching { VehicleCommandDefinition.fromJson(array.getJSONObject(index)) }.getOrNull()
        }.ifEmpty { VehicleCommandDefinition.defaults() }
    }

    fun getJson(): JSONObject = JSONObject()
        .put("profileName", "Ford Transit FORScan verified commands")
        .put("commands", VehicleCommandDefinition.toJsonArray(getCommands()))
        .put("readOnly", false)

    fun save(json: JSONObject): JSONObject {
        val array = json.optJSONArray("commands") ?: JSONArray()
        val commands = (0 until array.length()).mapNotNull { index ->
            runCatching { VehicleCommandDefinition.fromJson(array.getJSONObject(index)) }.getOrNull()
        }.filter { it.id.isNotBlank() }
        prefs.edit().putString(KEY, VehicleCommandDefinition.toJsonArray(commands).toString()).apply()
        return getJson()
    }

    fun find(id: String): VehicleCommandDefinition? = getCommands().firstOrNull { it.id == id }

    fun updateResult(command: VehicleCommandDefinition, tx: String?, rx: String?, error: String?): VehicleCommandDefinition {
        val updated = command.copy(lastTx = tx, lastRx = rx, lastError = error, lastSentEpochMs = System.currentTimeMillis())
        val next = getCommands().map { if (it.id == command.id) updated else it }
        prefs.edit().putString(KEY, VehicleCommandDefinition.toJsonArray(next).toString()).apply()
        return updated
    }

    fun exportJson(): JSONObject = JSONObject()
        .put("profileName", "Ford Transit FORScan verified commands")
        .put("createdAt", nowIso())
        .put("commands", VehicleCommandDefinition.toJsonArray(getCommands()))

    private fun nowIso(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    companion object {
        private const val KEY = "commands"
    }
}
