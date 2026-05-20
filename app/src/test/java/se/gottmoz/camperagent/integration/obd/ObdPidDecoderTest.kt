package se.gottmoz.camperagent.integration.obd

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObdPidDecoderTest {
    @Test fun decodesStandardPids() {
        assertEquals(1726, ObdPidDecoder.rpm(0x1A, 0xF8))
        assertEquals(88, ObdPidDecoder.speed(88))
        assertEquals(90, ObdPidDecoder.temperature(130))
        assertEquals(10.0, ObdPidDecoder.maf(0x03, 0xE8), 0.001)
        assertEquals(50.196, ObdPidDecoder.throttle(128), 0.001)
        assertEquals(5, ObdPidDecoder.torquePercent(130))
        assertEquals(400, ObdPidDecoder.referenceTorqueNm(0x01, 0x90))
    }

    @Test fun decodesPidSupportMask() {
        val supported = ObdPidDecoder.decodeSupportedPids("41 00 BE 1F A8 13", 0x00)
        assertTrue(supported.contains("01"))
        assertTrue(supported.contains("0C"))
        assertTrue(supported.contains("20"))
    }
}
