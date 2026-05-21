package se.gottmoz.camperagent.integration.obd

data class ObdSettings(
    val enabled: Boolean = false,
    val adapterType: ObdAdapterType = ObdAdapterType.Auto,
    val baudRate: Int? = null,
    val protocol: String = ObdProtocol.Iso15765Can11_500.label,
    val readOnly: Boolean = true
)

enum class ObdProtocol(val label: String, val elmProtocol: String) {
    Auto("Auto", "0"),
    Iso15765Can11_500("ISO15765-4 CAN 11/500", "6"),
    Iso15765Can29_500("ISO15765-4 CAN 29/500", "7"),
    Iso15765Can11_250("ISO15765-4 CAN 11/250", "8"),
    Iso15765Can29_250("ISO15765-4 CAN 29/250", "9"),
    SaeJ1939Can29_250("SAE J1939 CAN 29/250", "A"),
    SaeJ1939Can29_500("SAE J1939 CAN 29/500", "A"),
    Iso9141_2("ISO9141-2", "3"),
    Iso14230_4Kwp("ISO14230-4 KWP", "4"),
    Custom("Custom", "0");

    companion object {
        fun fromLabel(label: String?): ObdProtocol = entries.firstOrNull { it.label == label } ?: Iso15765Can11_500
    }
}

enum class ObdAdapterType {
    Auto,
    Elm327Usb,
    StnUsb,
    VLinkerUsb
}

data class ObdTelemetry(
    val speedKph: Int? = null,
    val rpm: Int? = null,
    val coolantTempC: Int? = null,
    val intakeTempC: Int? = null,
    val moduleVoltage: Double? = null,
    val mafGps: Double? = null,
    val throttlePercent: Double? = null,
    val ambientTempC: Int? = null,
    val driverDemandTorquePercent: Int? = null,
    val actualTorquePercent: Int? = null,
    val engineReferenceTorqueNm: Int? = null,
    val dtcCodes: List<String> = emptyList(),
    val supportedPids: Set<String> = emptySet(),
    val source: String = "Simulated",
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
