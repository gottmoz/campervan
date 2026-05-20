package se.gottmoz.camperagent.integration.bms

import org.junit.Assert.assertEquals
import org.junit.Test
import se.gottmoz.camperagent.integration.victron.VictronTelemetry

class BatteryBmsMapperTest {
    @Test fun mapsVictronGxBatteryTelemetryAsPreferredBmsSource() {
        val bms = BatteryBmsMapper.fromVictronGx(
            VictronTelemetry(
                batterySocPercent = 91.0,
                batteryVoltage = 13.4,
                batteryCurrent = -12.0,
                batteryPowerWatts = -160.8
            )
        )
        assertEquals(91.0, bms.socPercent!!, 0.001)
        assertEquals(13.4, bms.voltage!!, 0.001)
        assertEquals(-12.0, bms.current!!, 0.001)
        assertEquals("victron_gx_can_bms", bms.source)
    }
}
