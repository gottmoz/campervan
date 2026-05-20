package se.gottmoz.camperagent.integration.obd

data class ObdSettings(
    val enabled: Boolean = false,
    val adapterType: ObdAdapterType = ObdAdapterType.Auto,
    val baudRate: Int? = null,
    val protocol: String = "AUTO",
    val readOnly: Boolean = true
)

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
    val mafGps: Double? = null,
    val throttlePercent: Double? = null,
    val ambientTempC: Int? = null,
    val driverDemandTorquePercent: Int? = null,
    val actualTorquePercent: Int? = null,
    val engineReferenceTorqueNm: Int? = null,
    val dtcCodes: List<String> = emptyList(),
    val supportedPids: Set<String> = emptySet(),
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)
