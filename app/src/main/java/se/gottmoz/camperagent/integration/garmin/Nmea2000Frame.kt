package se.gottmoz.camperagent.integration.garmin

data class Nmea2000Frame(
    val canId: Int,
    val priority: Int,
    val pgn: Int,
    val source: Int,
    val destination: Int?,
    val data: ByteArray
)
