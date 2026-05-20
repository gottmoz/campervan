package se.gottmoz.camperagent.integration.bms

data class BleBmsCandidate(
    val name: String,
    val address: String,
    val services: List<String> = emptyList(),
    val characteristics: List<String> = emptyList()
)

class BatteryBmsBluetoothDiscovery {
    fun candidates(): List<BleBmsCandidate> = emptyList()
}
