package se.gottmoz.camperagent.integration.bms

data class BatteryCanFrameSummary(
    val canId: String,
    val dlc: Int,
    val dataHex: String,
    val rateHz: Double? = null,
    val lastSeenEpochMs: Long,
    val decodedAs: String = "unknown raw CAN"
)

class BatteryBmsCanDiscovery {
    private val frames = linkedMapOf<String, BatteryCanFrameSummary>()

    fun observePassive(canId: Int, data: ByteArray, nowMs: Long = System.currentTimeMillis()) {
        val id = "0x%X".format(canId)
        frames[id] = BatteryCanFrameSummary(
            canId = id,
            dlc = data.size,
            dataHex = data.joinToString(" ") { "%02X".format(it) },
            lastSeenEpochMs = nowMs
        )
    }

    fun snapshot(): List<BatteryCanFrameSummary> = frames.values.toList()
}
