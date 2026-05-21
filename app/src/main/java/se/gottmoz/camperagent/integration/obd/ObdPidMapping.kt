package se.gottmoz.camperagent.integration.obd

import org.json.JSONArray
import org.json.JSONObject

data class ObdPidMapping(
    val functionKey: String,
    val label: String,
    val enabled: Boolean,
    val mode: String,
    val service: String,
    val pid: String,
    val formula: String,
    val unit: String,
    val min: Double?,
    val max: Double?,
    val pollIntervalMs: Long,
    val timeoutMs: Int,
    val decimals: Int,
    val note: String = "",
) {
    val command: String get() = service.uppercase() + pid.uppercase()

    fun toJson(): JSONObject = JSONObject()
        .put("functionKey", functionKey)
        .put("label", label)
        .put("enabled", enabled)
        .put("mode", mode)
        .put("service", service)
        .put("pid", pid)
        .put("formula", formula)
        .put("unit", unit)
        .put("min", min ?: JSONObject.NULL)
        .put("max", max ?: JSONObject.NULL)
        .put("pollIntervalMs", pollIntervalMs)
        .put("timeoutMs", timeoutMs)
        .put("decimals", decimals)
        .put("note", note)

    companion object {
        fun fromJson(json: JSONObject): ObdPidMapping = ObdPidMapping(
            functionKey = json.optString("functionKey"),
            label = json.optString("label"),
            enabled = json.optBoolean("enabled", false),
            mode = json.optString("mode", "Standard OBD"),
            service = json.optString("service", "01").uppercase(),
            pid = json.optString("pid").uppercase(),
            formula = json.optString("formula", "A"),
            unit = json.optString("unit"),
            min = if (json.isNull("min")) null else json.optDouble("min"),
            max = if (json.isNull("max")) null else json.optDouble("max"),
            pollIntervalMs = json.optLong("pollIntervalMs", 2_000L).coerceAtLeast(500L),
            timeoutMs = json.optInt("timeoutMs", 2_000).coerceIn(500, 10_000),
            decimals = json.optInt("decimals", 0).coerceIn(0, 3),
            note = json.optString("note"),
        )

        fun defaults(): List<ObdPidMapping> = listOf(
            ObdPidMapping("rpm", "RPM", true, "Standard OBD", "01", "0C", "((A*256)+B)/4", "rpm", 0.0, 5000.0, 700, 2_000, 0),
            ObdPidMapping("speedKph", "Speed", true, "Standard OBD", "01", "0D", "A", "km/h", 0.0, 200.0, 700, 2_000, 0),
            ObdPidMapping("coolantTempC", "Coolant temp", true, "Standard OBD", "01", "05", "A-40", "degC", 40.0, 120.0, 2_000, 2_000, 0),
            ObdPidMapping("intakeTempC", "Intake temp", true, "Standard OBD", "01", "0F", "A-40", "degC", 40.0, 120.0, 2_000, 2_000, 0),
            ObdPidMapping("mafGps", "MAF", true, "Standard OBD", "01", "10", "((A*256)+B)/100", "g/s", 0.0, 100.0, 2_000, 2_000, 1),
            ObdPidMapping("throttlePercent", "Throttle", true, "Standard OBD", "01", "11", "A*100/255", "%", 0.0, 100.0, 1_500, 2_000, 0),
            ObdPidMapping("moduleVoltage", "Module voltage", true, "Standard OBD", "01", "42", "((A*256)+B)/1000", "V", 11.0, 15.5, 3_000, 2_000, 1),
            ObdPidMapping("outsideTempC", "Outside temp", true, "Standard OBD", "01", "46", "A-40", "degC", -30.0, 50.0, 3_000, 2_000, 0),
            ObdPidMapping("engineLoadPercent", "Engine load", true, "Standard OBD", "01", "04", "A*100/255", "%", 0.0, 100.0, 1_500, 2_000, 0),
            ObdPidMapping("oilTempC", "Oil temp", false, "Standard OBD", "01", "5C", "A-40", "degC", 40.0, 130.0, 3_000, 2_000, 0, "May not be supported on this Ford. User can change."),
            ObdPidMapping("boostBar", "Boost / turbo", false, "Ford custom PID", "22", "", "custom future", "bar", 0.0, 2.5, 1_500, 3_000, 2, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("egtTempC", "EGT", false, "Ford custom PID", "22", "", "custom future", "degC", 0.0, 800.0, 3_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("dpfSootPercent", "DPF soot", false, "Ford custom PID", "22", "", "custom future", "%", 0.0, 100.0, 5_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("dpfRegenActive", "DPF regen", false, "Ford custom PID", "22", "", "custom future", "state", null, null, 5_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("fuelLevelPercent", "Fuel level", false, "Standard OBD", "01", "2F", "A*100/255", "%", 0.0, 100.0, 5_000, 2_000, 0),
            ObdPidMapping("fuelRateLh", "Fuel rate", false, "Standard OBD", "01", "5E", "((A*256)+B)*0.05", "L/h", 0.0, 50.0, 3_000, 2_000, 1),
            ObdPidMapping("torque", "Torque", false, "Standard OBD", "01", "62", "A-125", "%", -125.0, 125.0, 2_000, 2_000, 0),
            ObdPidMapping("gear", "Gear source / estimated gear", false, "Calculated", "", "", "custom future", "", null, null, 1_000, 1_000, 0),
        )

        fun toJsonArray(mappings: List<ObdPidMapping>): JSONArray = JSONArray().also { array ->
            mappings.forEach { array.put(it.toJson()) }
        }
    }
}
