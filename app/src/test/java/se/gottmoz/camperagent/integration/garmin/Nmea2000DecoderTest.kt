package se.gottmoz.camperagent.integration.garmin

import org.junit.Assert.assertEquals
import org.junit.Test

class Nmea2000DecoderTest {
    @Test fun extractsExtendedCanFields() {
        val frame = Nmea2000Decoder.decode(0x09F11223, byteArrayOf(1, 2, 3))
        assertEquals(2, frame.priority)
        assertEquals(127250, frame.pgn)
        assertEquals(0x23, frame.source)
        assertEquals(null, frame.destination)
        assertEquals("Vessel Heading", Nmea2000Decoder.pgnName(frame.pgn))
    }

    @Test fun handlesUnknownPgn() {
        val frame = Nmea2000Decoder.decode(0x0DFF002A, byteArrayOf())
        assertEquals("Unknown / proprietary", Nmea2000Decoder.pgnName(frame.pgn))
    }
}
