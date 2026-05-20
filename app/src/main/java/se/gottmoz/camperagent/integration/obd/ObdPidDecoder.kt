package se.gottmoz.camperagent.integration.obd

object ObdPidDecoder {
    fun rpm(a: Int, b: Int): Int = ((a * 256) + b) / 4
    fun speed(a: Int): Int = a
    fun temperature(a: Int): Int = a - 40
    fun maf(a: Int, b: Int): Double = ((a * 256) + b) / 100.0
    fun throttle(a: Int): Double = a * 100.0 / 255.0
    fun torquePercent(a: Int): Int = a - 125
    fun referenceTorqueNm(a: Int, b: Int): Int = a * 256 + b

    fun decodeSupportedPids(response: String, basePid: Int): Set<String> {
        val bytes = responseBytes(response)
        if (bytes.size < 6 || bytes[0] != 0x41) return emptySet()
        val mask = bytes.drop(2).take(4)
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
        val bytes = responseBytes(response)
        if (bytes.size < 3 || bytes[0] != 0x41) return ObdTelemetry()
        val data = bytes.drop(2)
        return when (pid.uppercase()) {
            "0C" -> if (data.size >= 2) ObdTelemetry(rpm = rpm(data[0], data[1])) else ObdTelemetry()
            "0D" -> ObdTelemetry(speedKph = speed(data[0]))
            "05" -> ObdTelemetry(coolantTempC = temperature(data[0]))
            "0F" -> ObdTelemetry(intakeTempC = temperature(data[0]))
            "10" -> if (data.size >= 2) ObdTelemetry(mafGps = maf(data[0], data[1])) else ObdTelemetry()
            "11" -> ObdTelemetry(throttlePercent = throttle(data[0]))
            "46" -> ObdTelemetry(ambientTempC = temperature(data[0]))
            "61" -> ObdTelemetry(driverDemandTorquePercent = torquePercent(data[0]))
            "62" -> ObdTelemetry(actualTorquePercent = torquePercent(data[0]))
            "63" -> if (data.size >= 2) ObdTelemetry(engineReferenceTorqueNm = referenceTorqueNm(data[0], data[1])) else ObdTelemetry()
            else -> ObdTelemetry()
        }
    }

    private fun responseBytes(response: String): List<Int> {
        val clean = response.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
        return clean.chunked(2).mapNotNull { it.toIntOrNull(16) }
    }
}
