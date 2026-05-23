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
    val category: String = "General",
    val module: String = "PCM",
    val setupCommands: List<String> = emptyList(),
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
        .put("category", category)
        .put("module", module)
        .put("setupCommands", JSONArray(setupCommands))
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
            category = json.optString("category", "General"),
            module = json.optString("module", "PCM"),
            setupCommands = json.optJSONArray("setupCommands")?.let { array ->
                (0 until array.length()).map { array.optString(it) }.filter { it.isNotBlank() }
            } ?: emptyList(),
            min = if (json.isNull("min")) null else json.optDouble("min"),
            max = if (json.isNull("max")) null else json.optDouble("max"),
            pollIntervalMs = json.optLong("pollIntervalMs", 2_000L).coerceAtLeast(500L),
            timeoutMs = json.optInt("timeoutMs", 2_000).coerceIn(500, 10_000),
            decimals = json.optInt("decimals", 0).coerceIn(0, 3),
            note = json.optString("note"),
        )

        fun defaults(): List<ObdPidMapping> = listOf(
            ObdPidMapping("rpm", "RPM", true, "Standard OBD", "01", "0C", "((A*256)+B)/4", "rpm", "Main Dashboard", "PCM", emptyList(), 0.0, 5000.0, 700, 2_000, 0),
            ObdPidMapping("speedKph", "Speed", true, "Standard OBD", "01", "0D", "A", "km/h", "Main Dashboard", "PCM", emptyList(), 0.0, 200.0, 700, 2_000, 0),
            ObdPidMapping("coolantTempC", "Coolant temp", true, "Ford PID", "22", "F405", "A-40", "degC", "Main Dashboard", "PCM", listOf("ATSH7E0"), 40.0, 120.0, 2_000, 2_000, 0),
            ObdPidMapping("intakeTempC", "Intake temp", true, "Ford PID", "22", "F40F", "A-40", "degC", "Engine Details", "PCM", listOf("ATSH7E0"), 40.0, 120.0, 2_000, 2_000, 0),
            ObdPidMapping("outsideTempC", "Outside temp", true, "Ford PID", "22", "057D", "A-40", "degC", "Main Dashboard", "PCM", listOf("ATSH7E0"), -30.0, 50.0, 3_000, 2_000, 0),
            ObdPidMapping("acCompressorStatus", "AC compressor status", true, "Ford PID", "22", "099B", "A", "enum", "HVAC / AC", "PCM", listOf("ATSH7E0"), 0.0, 1.0, 2_000, 2_000, 0),
            ObdPidMapping("driveMode", "Drive mode", true, "Ford PID", "22", "0651", "A", "enum", "Driving Modes", "PCM", listOf("ATSH7E0"), null, null, 2_000, 2_000, 0),
            ObdPidMapping("alternatorDutyPercent", "Alternator duty cycle", true, "Ford PID", "22", "0598", "B4", "%", "Charging / Electrical", "PCM", listOf("ATSH7E0"), 0.0, 100.0, 2_000, 2_000, 0, "This is alternator regulator command/duty, not direct current."),
            ObdPidMapping("generatorCurrentA", "Generator / vehicle battery current", true, "Ford PID", "22", "402B", "U16_OFFSET_32768", "A", "Charging / Electrical", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), -250.0, 250.0, 2_000, 2_000, 0, "Ford BCM battery current uses 32768 zero offset. Positive/negative sign may need verification."),
            ObdPidMapping("vehicleBatteryVoltage", "Vehicle battery voltage", true, "Ford PID", "22", "402A", "U16_DIV_256", "V", "Charging / Electrical", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 10.0, 16.0, 2_000, 2_000, 2, "Ford BCM voltage appears to be raw/256. Do not display raw or raw/100."),
            ObdPidMapping("mafGps", "MAF", true, "Standard OBD", "01", "10", "((A*256)+B)/100", "g/s", "Engine Details", "PCM", emptyList(), 0.0, 100.0, 2_000, 2_000, 1),
            ObdPidMapping("throttlePercent", "Throttle", true, "Standard OBD", "01", "11", "A*100/255", "%", "Engine Details", "PCM", emptyList(), 0.0, 100.0, 1_500, 2_000, 0),
            ObdPidMapping("engineLoadPercent", "Engine load", true, "Standard OBD", "01", "04", "A*100/255", "%", "Engine Details", "PCM", emptyList(), 0.0, 100.0, 1_500, 2_000, 0),
            ObdPidMapping("mapKpa", "MAP", false, "Standard OBD", "01", "0B", "A", "kPa", "SAE Standard", "PCM", emptyList(), 0.0, 255.0, 2_000, 2_000, 0),
            ObdPidMapping("fuelLevelPercent", "Fuel level", false, "Standard OBD", "01", "2F", "A*100/255", "%", "Engine Details", "PCM", emptyList(), 0.0, 100.0, 5_000, 2_000, 0),
            ObdPidMapping("oxygenSensor1Faer", "Oxygen Sensor B1 FAER", false, "Standard OBD", "01", "34", "((A*256)+B)/32768", "ratio", "SAE Standard", "PCM", emptyList(), 0.0, 2.0, 5_000, 2_000, 3),
            ObdPidMapping("oxygenSensor5Faer", "Oxygen Sensor B2 FAER", false, "Standard OBD", "01", "38", "((A*256)+B)/32768", "ratio", "SAE Standard", "PCM", emptyList(), 0.0, 2.0, 5_000, 2_000, 3),
            ObdPidMapping("commandedEquivalenceRatio", "Commanded equivalence ratio", false, "Standard OBD", "01", "44", "((A*256)+B)/32768", "ratio", "SAE Standard", "PCM", emptyList(), 0.0, 2.0, 5_000, 2_000, 3),
            ObdPidMapping("ambientTempC", "Ambient air temp", false, "Standard OBD", "01", "46", "A-40", "degC", "SAE Standard", "PCM", emptyList(), -40.0, 80.0, 3_000, 2_000, 0),
            ObdPidMapping("actualEngineTorquePercent", "Actual engine torque percent", false, "Standard OBD", "01", "62", "A-125", "%", "SAE Standard", "PCM", emptyList(), -125.0, 125.0, 2_000, 2_000, 0),
            ObdPidMapping("engineReferenceTorqueNm", "Engine reference torque", false, "Standard OBD", "01", "63", "((A*256)+B)", "Nm", "SAE Standard", "PCM", emptyList(), 0.0, null, 3_000, 2_000, 0),
            ObdPidMapping("odometerKm", "Odometer", false, "Standard OBD", "01", "A6", "((A*256)+B)", "km", "SAE Standard", "PCM", emptyList(), 0.0, null, 10_000, 3_000, 0),
            ObdPidMapping("fuelRateLh", "Fuel rate", false, "Ford PID", "22", "F49D", "((A*256)+B)*2/100", "g/s", "Engine Details", "PCM", listOf("ATSH7E0"), 0.0, 50.0, 3_000, 2_000, 1),
            ObdPidMapping("iat2TempC", "Intake air temp 2", false, "Ford PID", "22", "03CA", "A-40", "degC", "PCM", "PCM", listOf("ATSH7E0"), -40.0, 120.0, 3_000, 2_000, 0),
            ObdPidMapping("transFluidTempC", "Transmission fluid temp", false, "Ford PID", "22", "1E1C", "((A*256)+B)/16", "degC", "PCM", "PCM", listOf("ATSH7E0"), -40.0, 160.0, 5_000, 2_000, 1),
            ObdPidMapping("gearCommanded", "Gear commanded", false, "Ford PID", "22", "1E12", "A", "enum", "PCM", "PCM", listOf("ATSH7E0"), null, null, 2_000, 2_000, 0),
            ObdPidMapping("brakePedalStatus", "Brake pedal status", false, "Ford PID", "22", "2B00", "A", "binary", "PCM", "PCM", listOf("ATSH7E0"), 0.0, 6.0, 1_000, 2_000, 0),
            ObdPidMapping("wastegateDutyPercent", "Wastegate duty", false, "Ford PID", "22", "0462", "A*100/128", "%", "PCM", "PCM", listOf("ATSH7E0"), 0.0, 100.0, 2_000, 2_000, 0),
            ObdPidMapping("oilLifePercent", "Oil life", false, "Ford PID", "22", "054B", "A", "%", "PCM", "PCM", listOf("ATSH7E0"), 0.0, 100.0, 10_000, 2_000, 0),
            ObdPidMapping("learnedOctaneRatio", "Learned octane ratio", false, "Ford PID", "22", "03E8", "((A*256)+B)/16384", "%", "PCM", "PCM", listOf("ATSH7E0"), null, null, 10_000, 2_000, 3),
            ObdPidMapping("fuelPumpDutyPercent", "Fuel pump duty", false, "Ford PID", "22", "0307", "A*100/255", "%", "PCM", "PCM", listOf("ATSH7E0"), 0.0, 100.0, 3_000, 2_000, 0),
            ObdPidMapping("fuelSystemStatus", "Fuel system status", false, "Ford PID", "22", "F403", "A", "binary", "PCM", "PCM", listOf("ATSH7E0"), null, null, 5_000, 2_000, 0),
            ObdPidMapping("desiredFuelPressureKpa", "Desired fuel pressure", false, "Ford PID", "22", "03DC", "((A*256)+B)", "kPa", "PCM", "PCM", listOf("ATSH7E0"), 0.0, null, 3_000, 2_000, 0),
            ObdPidMapping("fuelPressureKpa", "Fuel pressure sensor", false, "Ford PID", "22", "F423", "((A*256)+B)", "kPa", "PCM", "PCM", listOf("ATSH7E0"), 0.0, null, 3_000, 2_000, 0),
            ObdPidMapping("frontTirePlacardPressure", "Front tire placard pressure", false, "Ford PID", "22", "2827", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("leftFrontTirePressure", "Left front tire pressure", false, "Ford PID", "22", "2813", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("rightFrontTirePressure", "Right front tire pressure", false, "Ford PID", "22", "2814", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("rearTirePlacardPressure", "Rear tire placard pressure", false, "Ford PID", "22", "2828", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("leftRearOuterTirePressure", "Left rear outer tire pressure", false, "Ford PID", "22", "2816", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("rightRearOuterTirePressure", "Right rear outer tire pressure", false, "Ford PID", "22", "2815", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("leftRearInnerTirePressure", "Left rear inner tire pressure", false, "Ford PID", "22", "2818", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("rightRearInnerTirePressure", "Right rear inner tire pressure", false, "Ford PID", "22", "2817", "((A*256)+B)/10", "InHg", "BCM / TPMS", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), null, null, 10_000, 2_000, 1),
            ObdPidMapping("vehicleBatterySocPercent", "Vehicle battery SOC", false, "Ford PID", "22", "4028", "A", "%", "Charging / Electrical", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 100.0, 5_000, 2_000, 0),
            ObdPidMapping("driverDoorAjar", "Driver door ajar", false, "Ford PID", "22", "5B1D", "A&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("passengerDoorAjar", "Passenger door ajar", false, "Ford PID", "22", "5B1D", "(A>>1)&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("hoodAjar", "Hood ajar", false, "Ford PID", "22", "5B1D", "(A>>2)&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("leftRearDoorAjar", "Left rear door ajar", false, "Ford PID", "22", "5B1D", "(A>>3)&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("rightRearDoorAjar", "Right rear door ajar", false, "Ford PID", "22", "5B1D", "(A>>4)&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("luggageDoorAjar", "Luggage compartment ajar", false, "Ford PID", "22", "5B1D", "(A>>5)&1", "state", "BCM / Doors", "BCM", listOf("ATSH000726", "STCAFCP726,72E"), 0.0, 1.0, 5_000, 2_000, 0),
            ObdPidMapping("oilTempC", "Oil temp", false, "Standard OBD", "01", "5C", "A-40", "degC", "Main Dashboard", "PCM", emptyList(), 40.0, 130.0, 3_000, 2_000, 0, "May not be supported on this Ford. User can change."),
            ObdPidMapping("boostBar", "Boost / turbo", false, "Ford custom PID", "22", "", "custom future", "bar", "Engine Details", "PCM", listOf("ATSH7E0"), 0.0, 2.5, 1_500, 3_000, 2, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("egtTempC", "EGT", false, "Ford custom PID", "22", "", "custom future", "degC", "Engine Details", "PCM", listOf("ATSH7E0"), 0.0, 800.0, 3_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("dpfSootPercent", "DPF soot", false, "Ford custom PID", "22", "", "custom future", "%", "Engine Details", "PCM", listOf("ATSH7E0"), 0.0, 100.0, 5_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("dpfRegenActive", "DPF regen", false, "Ford custom PID", "22", "", "custom future", "state", "Engine Details", "PCM", listOf("ATSH7E0"), null, null, 5_000, 3_000, 0, "Needs confirmed Ford-specific PID."),
            ObdPidMapping("torque", "Torque", false, "Standard OBD", "01", "62", "A-125", "%", "Engine Details", "PCM", emptyList(), -125.0, 125.0, 2_000, 2_000, 0),
            ObdPidMapping("gear", "Gear source / estimated gear", false, "Calculated", "", "", "custom future", "", "Engine Details", "PCM", emptyList(), null, null, 1_000, 1_000, 0),
        )

        fun toJsonArray(mappings: List<ObdPidMapping>): JSONArray = JSONArray().also { array ->
            mappings.forEach { array.put(it.toJson()) }
        }
    }
}
