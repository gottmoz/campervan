package se.gottmoz.camperagent.integration.tcan485

import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketTimeoutException

class TCan485Repository(
    private val settingsStore: TCan485SettingsStore,
    private val client: TCan485Client = TCan485Client()
) {
    @Volatile private var discoveryRunning = false
    @Volatile private var lastBeacon: JSONObject? = null

    fun getSettings(): JSONObject = settingsStore.get()
    fun saveSettings(json: JSONObject): JSONObject = settingsStore.save(json)

    fun startDiscovery(): JSONObject {
        if (!discoveryRunning) startDiscoveryThread()
        return snapshot().put("state", "Listening")
    }

    fun stopDiscovery(): JSONObject {
        discoveryRunning = false
        return snapshot().put("state", "Stopped")
    }

    fun snapshot(): JSONObject = TCan485DiscoverySnapshot(discoveryRunning, lastBeacon).toJson()

    fun health(baseUrl: String): JSONObject = client.get(baseUrl, "/health")
    fun gatewayStatus(baseUrl: String): JSONObject = client.get(baseUrl, "/api/gateway/status")
    fun rs485Status(baseUrl: String): JSONObject = client.get(baseUrl, "/api/rs485/status")
    fun bmsLatest(baseUrl: String): JSONObject = client.get(baseUrl, "/api/bms/latest")
    fun rs485RawLatest(baseUrl: String): JSONObject = client.get(baseUrl, "/api/rs485/raw/latest?limit=50")
    fun canStatus(baseUrl: String): JSONObject = client.get(baseUrl, "/api/can/status")
    fun canFramesLatest(baseUrl: String): JSONObject = client.get(baseUrl, "/api/can/frames/latest?limit=100")
    fun saveWifiSettings(baseUrl: String, json: JSONObject): JSONObject = client.post(baseUrl, "/api/settings/wifi", redactedWifiPayload(json))
    fun saveCanSettings(baseUrl: String, json: JSONObject): JSONObject = client.post(baseUrl, "/api/settings/can", json)
    fun saveRs485Settings(baseUrl: String, json: JSONObject): JSONObject = client.post(baseUrl, "/api/settings/rs485", json)
    fun reboot(baseUrl: String): JSONObject = client.post(baseUrl, "/api/reboot")

    private fun startDiscoveryThread() {
        discoveryRunning = true
        Thread {
            DatagramSocket(DISCOVERY_PORT).use { socket ->
                socket.broadcast = true
                socket.soTimeout = 2_000
                val buffer = ByteArray(4096)
                while (discoveryRunning) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket.receive(packet)
                        val text = String(packet.data, packet.offset, packet.length, Charsets.UTF_8)
                        val json = runCatching { JSONObject(text) }.getOrNull() ?: continue
                        if (json.optString("type") == "camper_tcan485_hello") {
                            val remoteAddress = packet.address.hostAddress ?: ""
                            lastBeacon = json
                                .put("remoteAddress", remoteAddress)
                                .put("baseUrl", "http://${json.optString("ip", remoteAddress)}")
                                .put("password", "[redacted]")
                        }
                    } catch (_: SocketTimeoutException) {
                        // Keep listening until the user stops discovery.
                    }
                }
            }
        }.apply {
            name = "CamperTcan485Discovery"
            isDaemon = true
            start()
        }
    }

    private fun redactedWifiPayload(json: JSONObject): JSONObject = JSONObject(json.toString()).apply {
        put("readOnly", true)
    }

    companion object {
        const val DISCOVERY_PORT = 47887
    }
}
