package se.gottmoz.camperagent.integration.canbus

class CanAdapterManager {
    fun profiles(): List<CanBusProfile> = listOf(CanBusProfile.fordObd, CanBusProfile.nmea2000, CanBusProfile.batteryBms)
}
