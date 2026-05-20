package se.gottmoz.camperagent.integration.garmin

data class GarminSettings(
    val enabled: Boolean = false,
    val mode: GarminMode = GarminMode.Nmea2000Can,
    val canBitrate: Int = 250000,
    val readOnly: Boolean = true
)

enum class GarminMode {
    Nmea2000Can,
    SignalK,
    Nmea0183Serial,
    EmpirBusDiscovery
}

data class NmeaPgnSample(
    val pgn: Int,
    val source: Int,
    val name: String,
    val lastSeenEpochMs: Long,
    val rateHz: Double? = null,
    val rawHex: String? = null
)

data class GarminTelemetry(
    val gpsSpeedKph: Double? = null,
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val headingDeg: Double? = null,
    val tankLevels: Map<String, Double> = emptyMap(),
    val switchCircuits: List<String> = emptyList(),
    val alarms: List<String> = emptyList(),
    val discoveredPgns: List<NmeaPgnSample> = emptyList(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
