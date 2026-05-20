package se.gottmoz.camperagent.integration.garmin

class Nmea2000Discovery {
    private val samples = linkedMapOf<String, NmeaPgnSample>()

    fun observe(canId: Int, data: ByteArray, nowMs: Long = System.currentTimeMillis()): NmeaPgnSample {
        val frame = Nmea2000Decoder.decode(canId, data)
        val key = "${frame.pgn}:${frame.source}"
        val sample = NmeaPgnSample(
            pgn = frame.pgn,
            source = frame.source,
            name = Nmea2000Decoder.pgnName(frame.pgn),
            lastSeenEpochMs = nowMs,
            rawHex = data.joinToString("") { "%02X".format(it) }
        )
        samples[key] = sample
        return sample
    }

    fun snapshot(): List<NmeaPgnSample> = samples.values.toList()
}
