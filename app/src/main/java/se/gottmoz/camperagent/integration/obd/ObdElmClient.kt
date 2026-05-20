package se.gottmoz.camperagent.integration.obd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import se.gottmoz.camperagent.integration.usbserial.UsbSerialManager

class ObdElmClient(private val usbSerialManager: UsbSerialManager) {
    private val initSequence = listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH1", "ATSP0")

    suspend fun initialize(): List<String> = withContext(Dispatchers.IO) {
        initSequence.map { sendReadOnlyCommand(it) }
    }

    suspend fun probeAdapter(): Map<String, String> = withContext(Dispatchers.IO) {
        mapOf(
            "ATI" to sendReadOnlyCommand("ATI"),
            "AT@1" to sendReadOnlyCommand("AT@1"),
            "ATDP" to sendReadOnlyCommand("ATDP")
        )
    }

    suspend fun readSupportedPids(): Set<String> {
        val requests = listOf("0100" to 0x00, "0120" to 0x20, "0140" to 0x40, "0160" to 0x60)
        return requests.flatMap { (command, base) ->
            ObdPidDecoder.decodeSupportedPids(sendReadOnlyCommand(command), base)
        }.toSet()
    }

    suspend fun readPid(pid: String): ObdTelemetry = ObdPidDecoder.decodeTelemetry(pid, sendReadOnlyCommand("01$pid"))

    suspend fun readDtcs(): List<String> = parseDtcs(sendReadOnlyCommand("03"))

    suspend fun sendReadOnlyCommand(command: String, timeoutMs: Int = 1_500): String {
        require(isReadOnlyCommand(command)) { "Blocked non read-only OBD command: $command" }
        usbSerialManager.writeBytes((command.trim() + "\r").toByteArray(Charsets.US_ASCII))
        return sanitize(usbSerialManager.readUntilPrompt(timeoutMs))
    }

    private fun isReadOnlyCommand(command: String): Boolean {
        val normalized = command.trim().uppercase()
        return normalized in initSequence ||
            normalized in setOf("ATI", "AT@1", "ATDP", "0100", "0120", "0140", "0160", "03") ||
            normalized.matches(Regex("01(0C|0D|05|0F|10|11|46|61|62|63)"))
    }

    private fun sanitize(raw: String): String = raw.replace(">", "").replace(Regex("[\\r\\n]"), "").trim()

    private fun parseDtcs(response: String): List<String> {
        val clean = response.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
        val payload = clean.substringAfter("43", "")
        return payload.chunked(4).filter { it.length == 4 && it != "0000" }.map { "P$it" }
    }
}
