package se.gottmoz.camperagent.integration.victron

class VictronModbusClient {
    fun testConnection(settings: VictronSettings): Boolean {
        return settings.readOnly && settings.host.isNotBlank() && settings.modbusPort in 1..65535
    }
}
