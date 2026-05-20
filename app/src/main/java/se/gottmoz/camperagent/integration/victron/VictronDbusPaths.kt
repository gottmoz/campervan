package se.gottmoz.camperagent.integration.victron

object VictronDbusPaths {
    const val systemService = "com.victronenergy.system"
    val battery = listOf(
        "/Dc/Battery/Soc",
        "/Dc/Battery/Voltage",
        "/Dc/Battery/Current",
        "/Dc/Battery/Power",
        "/Dc/Battery/Temperature",
        "/Dc/Battery/Capacity",
        "/Dc/Battery/State",
        "/SystemState/ChargeDisabled",
        "/SystemState/DischargeDisabled"
    )
    val solar = listOf(
        "/Dc/Pv/Power",
        "/Dc/Pv/Current",
        "/Pv/V",
        "/Pv/I",
        "/Yield/Power",
        "/History/Daily/0/Yield",
        "/History/Daily/0/MaxPower",
        "/State",
        "/ErrorCode"
    )
    val shore = listOf("/Ac/ActiveIn/Source", "/Ac/In/0/Connected", "/Ac/In/0/Source", "/Ac/In/0/ServiceName")
    val dcChargers = listOf("/Dc/Charger/Power", "com.victronenergy.dcdc.*", "com.victronenergy.alternator.*", "com.victronenergy.dcsource.*")
}
