package se.gottmoz.camperagent.integration.victron

class VictronMqttClient {
    fun testConnection(settings: VictronSettings): Boolean {
        return settings.readOnly && settings.host.isNotBlank() && settings.mqttPort in 1..65535
    }
}
