package se.gottmoz.camperagent.integration.obd

import org.json.JSONArray
import org.json.JSONObject

data class VehicleCommandDefinition(
    val id: String,
    val enabled: Boolean = false,
    val displayName: String,
    val description: String,
    val category: String,
    val module: String,
    val setupCommands: List<String> = emptyList(),
    val command: String,
    val expectedPositiveResponse: String? = null,
    val verifiedByUser: Boolean = false,
    val verifiedSource: String = "FORScan",
    val requiresIgnitionOn: Boolean = true,
    val requiresEngineRunning: Boolean? = null,
    val requiresVehicleStopped: Boolean = false,
    val cooldownMs: Long = 1500,
    val confirmBeforeSend: Boolean = true,
    val expectedStatusFunctionKey: String? = null,
    val expectedStatusValue: String? = null,
    val lastTx: String? = null,
    val lastRx: String? = null,
    val lastError: String? = null,
    val lastSentEpochMs: Long? = null,
) {
    fun canExecute(): Boolean = enabled && command.isNotBlank()

    fun blockedReason(): String? = when {
        !enabled -> "$displayName command is disabled"
        command.isBlank() -> "$displayName command is empty"
        else -> null
    }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("enabled", enabled)
        .put("displayName", displayName)
        .put("description", description)
        .put("category", category)
        .put("module", module)
        .put("setupCommands", JSONArray(setupCommands))
        .put("command", command)
        .put("expectedPositiveResponse", expectedPositiveResponse ?: JSONObject.NULL)
        .put("verifiedByUser", verifiedByUser)
        .put("verifiedSource", verifiedSource)
        .put("requiresIgnitionOn", requiresIgnitionOn)
        .put("requiresEngineRunning", requiresEngineRunning ?: JSONObject.NULL)
        .put("requiresVehicleStopped", requiresVehicleStopped)
        .put("cooldownMs", cooldownMs)
        .put("confirmBeforeSend", confirmBeforeSend)
        .put("expectedStatusFunctionKey", expectedStatusFunctionKey ?: JSONObject.NULL)
        .put("expectedStatusValue", expectedStatusValue ?: JSONObject.NULL)
        .put("lastTx", lastTx ?: JSONObject.NULL)
        .put("lastRx", lastRx ?: JSONObject.NULL)
        .put("lastError", lastError ?: JSONObject.NULL)
        .put("lastSentEpochMs", lastSentEpochMs ?: JSONObject.NULL)

    companion object {
        fun fromJson(json: JSONObject): VehicleCommandDefinition = VehicleCommandDefinition(
            id = json.optString("id"),
            enabled = json.optBoolean("enabled", false),
            displayName = json.optString("displayName"),
            description = json.optString("description"),
            category = json.optString("category"),
            module = json.optString("module"),
            setupCommands = json.optJSONArray("setupCommands")?.let { array ->
                (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList(),
            command = json.optString("command"),
            expectedPositiveResponse = json.optString("expectedPositiveResponse").ifBlank { null },
            verifiedByUser = json.optBoolean("verifiedByUser", false),
            verifiedSource = json.optString("verifiedSource", "FORScan"),
            requiresIgnitionOn = json.optBoolean("requiresIgnitionOn", true),
            requiresEngineRunning = if (json.isNull("requiresEngineRunning")) null else json.optBoolean("requiresEngineRunning"),
            requiresVehicleStopped = json.optBoolean("requiresVehicleStopped", false),
            cooldownMs = json.optLong("cooldownMs", 1500).coerceAtLeast(0),
            confirmBeforeSend = json.optBoolean("confirmBeforeSend", true),
            expectedStatusFunctionKey = json.optString("expectedStatusFunctionKey").ifBlank { null },
            expectedStatusValue = json.optString("expectedStatusValue").ifBlank { null },
            lastTx = json.optString("lastTx").ifBlank { null },
            lastRx = json.optString("lastRx").ifBlank { null },
            lastError = json.optString("lastError").ifBlank { null },
            lastSentEpochMs = if (json.isNull("lastSentEpochMs")) null else json.optLong("lastSentEpochMs"),
        )

        fun defaults(): List<VehicleCommandDefinition> = listOf(
            VehicleCommandDefinition("ac_on", displayName = "AC Compressor ON", description = "User can paste the tested command to request AC/compressor ON.", category = "HVAC / AC", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "acCompressorStatus", expectedStatusValue = "01"),
            VehicleCommandDefinition("ac_off", displayName = "AC Compressor OFF", description = "User can paste the tested command to request AC/compressor OFF.", category = "HVAC / AC", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "acCompressorStatus", expectedStatusValue = "00"),
            VehicleCommandDefinition("drive_mode_normal", displayName = "Normal", description = "User-enabled Normal drive mode command.", category = "Driving Modes", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "driveMode", expectedStatusValue = "00"),
            VehicleCommandDefinition("drive_mode_eco", displayName = "Eco", description = "User-enabled Eco drive mode command.", category = "Driving Modes", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "driveMode", expectedStatusValue = "06"),
            VehicleCommandDefinition("drive_mode_slippery", displayName = "Slippery", description = "User-enabled Slippery drive mode command.", category = "Driving Modes", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "driveMode", expectedStatusValue = "05"),
            VehicleCommandDefinition("drive_mode_mud_ruts", displayName = "Mud & Ruts", description = "User-enabled Mud & Ruts drive mode command.", category = "Driving Modes", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "driveMode", expectedStatusValue = "08"),
            VehicleCommandDefinition("drive_mode_tow_haul", displayName = "Tow / Haul", description = "User-enabled Tow / Haul drive mode command.", category = "Driving Modes", module = "PCM", setupCommands = listOf("ATSH7E0"), command = "", expectedStatusFunctionKey = "driveMode", expectedStatusValue = "03"),
        )

        fun toJsonArray(commands: List<VehicleCommandDefinition>): JSONArray = JSONArray().also { array ->
            commands.forEach { array.put(it.toJson()) }
        }
    }
}
