package se.gottmoz.camperagent.integration.obd

import org.json.JSONObject
import kotlin.math.round

object ObdFormulaEvaluator {
    fun decodeValueForTest(formula: String, bytes: List<Int>, decimals: Int = 3): Double {
        return roundTo(applyPreset(formula, bytes), decimals)
    }

    fun decode(mapping: ObdPidMapping, raw: String): JSONObject {
        val parsed = ObdResponseParser.parse(raw, mapping.service) ?: error("No positive OBD response")
        val expectedPid = mapping.pid.uppercase()
        if (!parsed.pid.endsWith(expectedPid, ignoreCase = true) && parsed.pid != expectedPid) {
            error("Response PID ${parsed.pid} did not match $expectedPid")
        }
        val bytes = parsed.dataBytes
        val value = applyPreset(mapping.formula, bytes)
        val rawU16 = ((bytes.getOrNull(0) ?: 0) * 256) + (bytes.getOrNull(1) ?: 0)
        return JSONObject()
            .put("functionKey", mapping.functionKey)
            .put("label", mapping.label)
            .put("command", mapping.command)
            .put("raw", raw)
            .put("service", parsed.service)
            .put("pidEcho", parsed.pid)
            .put("payloadHex", bytes.joinToString("") { "%02X".format(it) })
            .put("B4", bytes.getOrNull(0) ?: JSONObject.NULL)
            .put("B5", bytes.getOrNull(1) ?: JSONObject.NULL)
            .put("formulaPreset", mapping.formula)
            .put("rawU16", rawU16)
            .put("bytes", bytesToJson(bytes))
            .put("value", roundTo(value, mapping.decimals))
            .put("unit", mapping.unit)
    }

    private fun applyPreset(formula: String, bytes: List<Int>): Double {
        val a = bytes.getOrNull(0) ?: 0
        val b = bytes.getOrNull(1) ?: 0
        val u16 = (a * 256) + b
        return when (formula.replace(" ", "")) {
            "A", "B4" -> a.toDouble()
            "A&1" -> (a and 1).toDouble()
            "(A>>1)&1" -> ((a shr 1) and 1).toDouble()
            "(A>>2)&1" -> ((a shr 2) and 1).toDouble()
            "(A>>3)&1" -> ((a shr 3) and 1).toDouble()
            "(A>>4)&1" -> ((a shr 4) and 1).toDouble()
            "(A>>5)&1" -> ((a shr 5) and 1).toDouble()
            "A-40", "B4_MINUS_40" -> (a - 40).toDouble()
            "((A*256)+B)/4" -> u16 / 4.0
            "((A*256)+B)/100", "U16_DIV_100" -> u16 / 100.0
            "((A*256)+B)/1000", "U16_DIV_1000" -> u16 / 1000.0
            "((A*256)+B)/10", "U16_DIV_10" -> u16 / 10.0
            "((A*256)+B)/16", "U16_DIV_16" -> u16 / 16.0
            "U16_DIV_256" -> u16 / 256.0
            "((A*256)+B)/32768" -> u16 / 32768.0
            "((A*256)+B)/16384" -> u16 / 16384.0
            "((A*256)+B)", "U16" -> u16.toDouble()
            "U16_OFFSET_32768" -> (u16 - 32768).toDouble()
            "U16_OFFSET_32768_DIV_10" -> (u16 - 32768) / 10.0
            "A*100/255", "PERCENT_255" -> a * 100.0 / 255.0
            "A*100/128", "PERCENT_128" -> a * 100.0 / 128.0
            "A-125" -> (a - 125).toDouble()
            "((A*256)+B)*0.05" -> u16 * 0.05
            "((A*256)+B)*2/100" -> u16 * 2.0 / 100.0
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
