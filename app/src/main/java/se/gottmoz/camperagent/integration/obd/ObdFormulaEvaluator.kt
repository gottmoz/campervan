package se.gottmoz.camperagent.integration.obd

import org.json.JSONObject
import kotlin.math.round

object ObdFormulaEvaluator {
    fun decode(mapping: ObdPidMapping, raw: String): JSONObject {
        val parsed = ObdResponseParser.parse(raw, mapping.service) ?: error("No positive OBD response")
        val expectedPid = mapping.pid.uppercase()
        if (!parsed.pid.endsWith(expectedPid, ignoreCase = true) && parsed.pid != expectedPid) {
            error("Response PID ${parsed.pid} did not match $expectedPid")
        }
        val bytes = parsed.dataBytes
        val value = applyPreset(mapping.formula, bytes)
        return JSONObject()
            .put("functionKey", mapping.functionKey)
            .put("label", mapping.label)
            .put("command", mapping.command)
            .put("raw", raw)
            .put("bytes", bytesToJson(bytes))
            .put("value", roundTo(value, mapping.decimals))
            .put("unit", mapping.unit)
    }

    private fun applyPreset(formula: String, bytes: List<Int>): Double {
        val a = bytes.getOrNull(0) ?: 0
        val b = bytes.getOrNull(1) ?: 0
        return when (formula.replace(" ", "")) {
            "A" -> a.toDouble()
            "A-40" -> (a - 40).toDouble()
            "((A*256)+B)/4" -> ((a * 256) + b) / 4.0
            "((A*256)+B)/100" -> ((a * 256) + b) / 100.0
            "((A*256)+B)/1000" -> ((a * 256) + b) / 1000.0
            "A*100/255" -> a * 100.0 / 255.0
            "A-125" -> (a - 125).toDouble()
            "((A*256)+B)*0.05" -> ((a * 256) + b) * 0.05
            else -> error("Unsupported formula preset: $formula")
        }
    }

    private fun roundTo(value: Double, decimals: Int): Double {
        val factor = Math.pow(10.0, decimals.toDouble())
        return round(value * factor) / factor
    }

    private fun bytesToJson(bytes: List<Int>) = org.json.JSONArray().also { array ->
        bytes.forEach { array.put(it) }
    }
}
