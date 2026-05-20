package se.gottmoz.camperagent.integration.obd

object FordTransitEcoBlue2016Profile {
    const val label = "Ford Transit EcoBlue 2.0 2016"
    const val preferredProtocol = "AUTO / ISO15765"
    const val fastPollMs = 500L
    const val slowPollMs = 2_000L
    const val diagnosticPoll = "manual DTC request only"
    val standardPids = listOf("0C", "0D", "05", "0F", "10", "11", "46", "61", "62", "63")
}
