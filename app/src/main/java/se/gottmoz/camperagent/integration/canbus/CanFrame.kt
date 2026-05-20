package se.gottmoz.camperagent.integration.canbus

data class CanFrame(
    val timestampEpochMs: Long,
    val canId: Int,
    val extended: Boolean,
    val data: ByteArray
)
