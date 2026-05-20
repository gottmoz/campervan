package se.gottmoz.camperagent.integration.bms

enum class BatteryChemistry {
    LiFePO4,
    AGM,
    LeadAcid,
    Unknown
}

enum class BmsConnectionOption {
    VictronCanViaGx,
    DirectCan,
    Bluetooth,
    ShuntOnly,
    Manual,
    Unknown
}

enum class BmsCapability {
    StateOfCharge,
    Voltage,
    Current,
    Power,
    RemainingCapacityAh,
    FullCapacityAh,
    CellVoltages,
    CellDelta,
    Temperatures,
    ChargeAllowed,
    DischargeAllowed,
    AlarmState,
    WarningState,
    CycleCount,
    MosfetState,
    BalancingState,
    ProtocolDiscovery
}

enum class BmsProtocol {
    AutoDetect,
    VictronGxDbusBattery,
    VictronCanBusBms,
    JkBms,
    JkInverterBms,
    JbdXiaoxiang,
    Daly,
    Seplos,
    SeplosV3,
    Pace,
    RenogyBms,
    RvcHouseBattery,
    PylontechLike,
    Eg4,
    Felicity,
    LitimePowerQueenRedodo,
    HeltecYanyangModbus,
    Valence,
    Ant,
    Sinowealth,
    UnknownRawCan,
    UnknownBluetooth
}

data class BatteryProfile(
    val id: String,
    val displayName: String,
    val brand: String,
    val chemistry: BatteryChemistry,
    val nominalVoltage: Double,
    val capacityAh: Double,
    val bmsContinuousCurrentAmp: Double,
    val connectionOptions: Set<BmsConnectionOption>,
    val expectedCapabilities: Set<BmsCapability>,
    val notes: String
)

object BuiltInBatteryProfiles {
    val pupvwmhbLiFePo4 = BatteryProfile(
        id = "pupvwmhb_lifepo4_12v_320ah_250a",
        displayName = "PUPVWMHB 12V 320Ah LiFePO4 250A BMS",
        brand = "PUPVWMHB",
        chemistry = BatteryChemistry.LiFePO4,
        nominalVoltage = 12.8,
        capacityAh = 320.0,
        bmsContinuousCurrentAmp = 250.0,
        connectionOptions = setOf(
            BmsConnectionOption.VictronCanViaGx,
            BmsConnectionOption.DirectCan,
            BmsConnectionOption.Bluetooth,
            BmsConnectionOption.ShuntOnly,
            BmsConnectionOption.Manual
        ),
        expectedCapabilities = setOf(
            BmsCapability.StateOfCharge,
            BmsCapability.Voltage,
            BmsCapability.Current,
            BmsCapability.Power,
            BmsCapability.RemainingCapacityAh,
            BmsCapability.FullCapacityAh,
            BmsCapability.CellVoltages,
            BmsCapability.CellDelta,
            BmsCapability.Temperatures,
            BmsCapability.ChargeAllowed,
            BmsCapability.DischargeAllowed,
            BmsCapability.AlarmState,
            BmsCapability.WarningState,
            BmsCapability.CycleCount,
            BmsCapability.MosfetState,
            BmsCapability.BalancingState,
            BmsCapability.ProtocolDiscovery
        ),
        notes = "User states BMS supports Bluetooth and CAN and can be read by Victron. Exact CAN/Bluetooth protocol is unverified, so phase 1 must discover and map read-only data."
    )
}
