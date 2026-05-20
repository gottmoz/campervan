package se.gottmoz.camperagent.integration.garmin

object Nmea2000Decoder {
    private val knownPgns = mapOf(
        126996 to "Product Information",
        126464 to "PGN List",
        127250 to "Vessel Heading",
        127257 to "Attitude",
        127488 to "Engine Parameters Rapid Update",
        127489 to "Engine Parameters Dynamic",
        127505 to "Fluid Level",
        129025 to "Position Rapid Update",
        129026 to "COG/SOG Rapid Update",
        130312 to "Temperature",
        130316 to "Temperature Extended Range"
    )

    fun decode(canId: Int, data: ByteArray): Nmea2000Frame {
        val priority = (canId ushr 26) and 0x7
        val dataPage = (canId ushr 24) and 0x1
        val pduFormat = (canId ushr 16) and 0xff
        val pduSpecific = (canId ushr 8) and 0xff
        val source = canId and 0xff
        val destination = if (pduFormat < 240) pduSpecific else null
        val pgn = if (pduFormat < 240) {
            (dataPage shl 16) or (pduFormat shl 8)
        } else {
            (dataPage shl 16) or (pduFormat shl 8) or pduSpecific
        }
        return Nmea2000Frame(canId, priority, pgn, source, destination, data)
    }

    fun pgnName(pgn: Int): String = knownPgns[pgn] ?: "Unknown / proprietary"
}
