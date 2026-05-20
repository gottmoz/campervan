package se.gottmoz.camperagent.integration.garmin

object Nmea2000Decoder {
    private val knownPgns = mapOf(
        59904 to "Request",
        60928 to "ISO Address Claim",
        126208 to "NMEA Request/Command/Acknowledge Group Function",
        126993 to "Heartbeat",
        126996 to "Product Information",
        126464 to "PGN List",
        126998 to "Configuration Information",
        127250 to "Vessel Heading",
        127257 to "Attitude",
        127258 to "Magnetic Variation",
        127488 to "Engine Parameters Rapid Update",
        127489 to "Engine Parameters Dynamic",
        127493 to "Transmission Parameters",
        127497 to "Trip Parameters Engine",
        127501 to "Binary Switch Bank Status",
        127502 to "Switch Bank Control",
        127503 to "AC Input Status",
        127504 to "AC Output Status",
        127505 to "Fluid Level",
        127506 to "DC Detailed Status",
        127507 to "Charger Status",
        127508 to "Battery Status",
        127509 to "Inverter Status",
        127510 to "Charger Configuration Status",
        127513 to "Battery Configuration Status",
        129025 to "Position Rapid Update",
        129026 to "COG/SOG Rapid Update",
        129029 to "GNSS Position Data",
        129539 to "GNSS DOPs",
        130310 to "Environmental Parameters",
        130311 to "Environmental Parameters",
        130312 to "Temperature",
        130313 to "Humidity",
        130314 to "Actual Pressure",
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
