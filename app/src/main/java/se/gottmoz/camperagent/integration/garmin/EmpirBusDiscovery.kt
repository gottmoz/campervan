package se.gottmoz.camperagent.integration.garmin

class EmpirBusDiscovery {
    fun classify(sample: NmeaPgnSample): String {
        return if (sample.name == "Unknown / proprietary") "unverified proprietary circuit" else "standard NMEA data"
    }
}
