package se.gottmoz.camperagent.integration.canbus

enum class CanIdMode { Standard11Bit, Extended29Bit, Auto }
enum class CanAdapterType { ElmVLinker, UsbCanRecommended, SocketCanBridgeFuture, Unknown }

data class CanBusProfile(
    val id: String,
    val label: String,
    val bitrate: Int?,
    val idMode: CanIdMode,
    val protocol: String,
    val adapterType: CanAdapterType,
    val passiveListenOnly: Boolean = true
) {
    companion object {
        val fordObd = CanBusProfile("ford_obd", "Ford OBD CAN", 500000, CanIdMode.Standard11Bit, "ISO15765-4", CanAdapterType.ElmVLinker)
        val nmea2000 = CanBusProfile("nmea2000", "Garmin/NMEA 2000", 250000, CanIdMode.Extended29Bit, "NMEA2000 / J1939 PGN", CanAdapterType.UsbCanRecommended)
        val batteryBms = CanBusProfile("battery_bms", "Battery BMS CAN", null, CanIdMode.Auto, "Auto detect BMS CAN", CanAdapterType.UsbCanRecommended)
    }
}
