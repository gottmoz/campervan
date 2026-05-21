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
        assertEquals(13.8, ObdPidDecoder.moduleVoltage(0x35, 0xE8), 0.001)
        assertEquals(5, ObdPidDecoder.torquePercent(130))
        assertEquals(400, ObdPidDecoder.referenceTorqueNm(0x01, 0x90))
    }

    @Test fun decodesPidSupportMask() {
        val supported = ObdPidDecoder.decodeSupportedPids("41 00 BE 1F A8 13", 0x00)
        assertTrue(supported.contains("01"))
        assertTrue(supported.contains("0C"))
        assertTrue(supported.contains("20"))
    }

    @Test fun parsesHeaderedSupportedPidResponse() {
        val parsed = ObdResponseParser.parseMode01("7E8064100983B8017\r\r>")
        assertEquals("7E8", parsed?.ecuId)
        assertEquals(0x06, parsed?.length)
        assertEquals("41", parsed?.service)
        assertEquals("00", parsed?.pid)
        assertEquals(listOf(0x98, 0x3B, 0x80, 0x17), parsed?.dataBytes)

        val supported = ObdPidDecoder.decodeSupportedPids("7E8064100983B8017", 0x00)
        assertEquals(setOf("01", "04", "05", "0B", "0C", "0D", "0F", "10", "11", "1C", "1E", "1F", "20"), supported)
    }

    @Test fun parsesCommonMode01ResponseForms() {
        val rpmWithHeader = ObdResponseParser.parseMode01("7E804410C1AF8")
        assertEquals("41", rpmWithHeader?.service)
        assertEquals("0C", rpmWithHeader?.pid)
        assertEquals(listOf(0x1A, 0xF8), rpmWithHeader?.dataBytes)

        val rpmNoHeader = ObdResponseParser.parseMode01("410C1AF8")
        assertEquals("41", rpmNoHeader?.service)
        assertEquals("0C", rpmNoHeader?.pid)
        assertEquals(listOf(0x1A, 0xF8), rpmNoHeader?.dataBytes)

        assertEquals(listOf(0x00), ObdResponseParser.parseMode01("7E8 03 41 0D 00")?.dataBytes)
    }
}
