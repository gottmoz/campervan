package se.gottmoz.camperagent.integration.tcan485

import org.json.JSONArray
import org.json.JSONObject

data class TCan485Settings(
    val enabled: Boolean = true,
    val networkMode: String = "sta_android_hotspot",
    val baseUrl: String = "http://192.168.4.1",
    val hostname: String = "camper-tcan485",
    val readOnly: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject()
        .put("enabled", enabled)
        .put("networkMode", networkMode)
        .put("baseUrl", baseUrl)
        .put("hostname", hostname)
        .put("readOnly", true)

    companion object {
        fun fromJson(json: JSONObject): TCan485Settings = TCan485Settings(
            enabled = json.optBoolean("enabled", true),
            networkMode = json.optString("networkMode", "sta_android_hotspot"),
            baseUrl = json.optString("baseUrl", "http://192.168.4.1"),
            hostname = json.optString("hostname", "camper-tcan485"),
            readOnly = true
        )
    }
}

data class TCan485DiscoverySnapshot(
    val running: Boolean,
    val beacon: JSONObject?
) {
    fun toJson(): JSONObject = JSONObject()
        .put("discoveryRunning", running)
        .put("port", TCan485Repository.DISCOVERY_PORT)
        .put("beacon", beacon ?: JSONObject.NULL)
        .put("fallbackUrls", JSONArray(listOf("http://camper-tcan485.local", "http://192.168.4.1")))
        .put("readOnly", true)
}
