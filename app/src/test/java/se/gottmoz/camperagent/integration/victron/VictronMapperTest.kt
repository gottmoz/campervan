package se.gottmoz.camperagent.integration.victron

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VictronMapperTest {
    @Test fun mapsCommonTelemetry() {
        val telemetry = VictronMapper.fromValues(
            mapOf(
                "batterySocPercent" to 87.0,
                "pvPowerWatts" to 420.0,
                "shoreConnected" to true,
                "acInputSource" to "shore"
            )
        )
        assertEquals(87.0, telemetry.batterySocPercent!!, 0.001)
        assertEquals(420.0, telemetry.pvPowerWatts!!, 0.001)
        assertTrue(telemetry.shoreConnected == true)
        assertEquals("shore", telemetry.acInputSource)
    }
}
