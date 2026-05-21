package se.gottmoz.camperagent.integration.obd

object ObdPidDecoder {
    fun rpm(a: Int, b: Int): Int = ((a * 256) + b) / 4
    fun speed(a: Int): Int = a
    fun temperature(a: Int): Int = a - 40
    fun maf(a: Int, b: Int): Double = ((a * 256) + b) / 100.0
    fun throttle(a: Int): Double = a * 100.0 / 255.0
    fun moduleVoltage(a: Int, b: Int): Double = ((a * 256) + b) / 1000.0
    fun torquePercent(a: Int): Int = a - 125
    fun referenceTorqueNm(a: Int, b: Int): Int = a * 256 + b

    fun decodeSupportedPids(response: String, basePid: Int): Set<String> {
        val parsed = ObdResponseParser.parseMode01(response) ?: return emptySet()
        if (parsed.pid != "%02X".format(basePid) || parsed.dataBytes.size < 4) return emptySet()
        val mask = parsed.dataBytes.take(4)
        val supported = mutableSetOf<String>()
        mask.forEachIndexed { byteIndex, value ->
            for (bit in 0..7) {
                if ((value and (0x80 shr bit)) != 0) {
                    supported += "%02X".format(basePid + byteIndex * 8 + bit + 1)
                }
            }
        }
        return supported
    }

    fun decodeTelemetry(pid: String, response: String): ObdTelemetry {
        val parsed = ObdResponseParser.parseMode01(response) ?: return ObdTelemetry()
        val data = parsed.dataBytes
        return when (pid.uppercase()) {
            "0C" -> if (data.size >= 2) ObdTelemetry(rpm = rpm(data[0], data[1])) else ObdTelemetry()
            "0D" -> if (data.isNotEmpty()) ObdTelemetry(speedKph = speed(data[0])) else ObdTelemetry()
            "05" -> if (data.isNotEmpty()) ObdTelemetry(coolantTempC = temperature(data[0])) else ObdTelemetry()
            "0F" -> if (data.isNotEmpty()) ObdTelemetry(intakeTempC = temperature(data[0])) else ObdTelemetry()
            "10" -> if (data.size >= 2) ObdTelemetry(mafGps = maf(data[0], data[1])) else ObdTelemetry()
            "11" -> if (data.isNotEmpty()) ObdTelemetry(throttlePercent = throttle(data[0])) else ObdTelemetry()
            "42" -> if (data.size >= 2) ObdTelemetry(moduleVoltage = moduleVoltage(data[0], data[1])) else ObdTelemetry()
            "46" -> if (data.isNotEmpty()) ObdTelemetry(ambientTempC = temperature(data[0])) else ObdTelemetry()
            "61" -> if (data.isNotEmpty()) ObdTelemetry(driverDemandTorquePercent = torquePercent(data[0])) else ObdTelemetry()
            "62" -> if (data.isNotEmpty()) ObdTelemetry(actualTorquePercent = torquePercent(data[0])) else ObdTelemetry()
            "63" -> if (data.size >= 2) ObdTelemetry(engineReferenceTorqueNm = referenceTorqueNm(data[0], data[1])) else ObdTelemetry()
            else -> ObdTelemetry()
        }
    }

}
