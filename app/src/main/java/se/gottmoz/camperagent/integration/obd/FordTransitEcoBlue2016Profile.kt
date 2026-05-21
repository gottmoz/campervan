package se.gottmoz.camperagent.integration.obd

object FordTransitEcoBlue2016Profile {
    const val label = "Ford Transit EcoBlue 2.0 2016"
    const val preferredProtocol = "ISO15765-4 CAN 11/500"
    const val preferredElmProtocol = "6"
    const val fastPollMs = 500L
    const val slowPollMs = 2_000L
    const val diagnosticPoll = "manual DTC request only"
    val standardPids = listOf("0C", "0D", "05", "0F", "10", "11", "42", "46", "61", "62", "63")
    val autoBaudOrder = listOf(115200, 38400, 9600, 57600, 230400, 460800, 921600)
    val initSequence = listOf("ATZ", "ATE0", "ATL0", "ATS0", "ATH1", "ATAL", "ATST96", "ATSP6", "ATDP", "ATDPN")
}
