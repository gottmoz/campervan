package se.gottmoz.camperagent.integration.obd

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class VehicleCommandStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("vehicle_command_library_v1", Context.MODE_PRIVATE)

    fun getCommands(): List<VehicleCommandDefinition> {
        val raw = prefs.getString(KEY, null) ?: appContext.getSharedPreferences("vehicle_commands", Context.MODE_PRIVATE).getString(KEY, null)
        val saved = raw?.let {
            val array = JSONArray(it)
            (0 until array.length()).mapNotNull { index ->
                runCatching { VehicleCommandDefinition.fromJson(array.getJSONObject(index)) }.getOrNull()
            }
        } ?: emptyList()
        return mergeDefaults(saved)
    }

    private fun mergeDefaults(saved: List<VehicleCommandDefinition>): List<VehicleCommandDefinition> {
        val byId = saved.associateBy { it.id }
        return VehicleCommandDefinition.defaults().map { byId[it.id] ?: it } +
            saved.filter { savedCommand -> VehicleCommandDefinition.defaults().none { it.id == savedCommand.id } }
    }

    fun saveCommand(command: VehicleCommandDefinition): JSONObject {
        val current = getCommands()
        val replaced = current.any { it.id == command.id }
        val next = mergeDefaults(if (replaced) current.map { if (it.id == command.id) command else it } else current + command)
        persist(next)
        return getJson()
    }

    fun save(json: JSONObject): JSONObject {
        val array = json.optJSONArray("commands") ?: JSONArray()
        val commands = (0 until array.length()).mapNotNull { index ->
            runCatching { VehicleCommandDefinition.fromJson(array.getJSONObject(index)) }.getOrNull()
        }.filter { it.id.isNotBlank() }
        persist(mergeDefaults(commands))
        return getJson()
    }

    fun getJson(): JSONObject = JSONObject()
        .put("profileName", "Ford Transit FORScan verified commands")
        .put("commands", VehicleCommandDefinition.toJsonArray(getCommands()))
        .put("readOnly", false)

    fun find(id: String): VehicleCommandDefinition? = getCommands().firstOrNull { it.id == id }

    fun updateResult(command: VehicleCommandDefinition, tx: String?, rx: String?, error: String?): VehicleCommandDefinition {
        val updated = command.copy(lastTx = tx, lastRx = rx, lastError = error, lastSentEpochMs = System.currentTimeMillis())
        val next = getCommands().map { if (it.id == command.id) updated else it }
        persist(next)
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

    private fun persist(commands: List<VehicleCommandDefinition>) {
        prefs.edit().putString(KEY, VehicleCommandDefinition.toJsonArray(commands).toString()).apply()
    }

    companion object {
        private const val KEY = "commands"
    }
}
