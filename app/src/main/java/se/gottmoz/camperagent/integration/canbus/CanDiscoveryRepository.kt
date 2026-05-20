package se.gottmoz.camperagent.integration.canbus

import org.json.JSONArray
import org.json.JSONObject

class CanDiscoveryRepository {
    private val scanner = CanScanner()

    fun listAdapters(): JSONObject = JSONObject()
        .put("profiles", JSONArray(CanAdapterManager().profiles().map { profile ->
            JSONObject()
                .put("id", profile.id)
                .put("label", profile.label)
                .put("bitrate", profile.bitrate ?: JSONObject.NULL)
                .put("idMode", profile.idMode.name)
                .put("protocol", profile.protocol)
                .put("adapterType", profile.adapterType.name)
                .put("passiveListenOnly", profile.passiveListenOnly)
        }))
        .put("readOnly", true)

    fun start(profileId: String): JSONObject {
        val profile = CanAdapterManager().profiles().firstOrNull { it.id == profileId } ?: CanBusProfile.batteryBms
        scanner.start(profile)
        return JSONObject().put("state", "PassiveScanStarted").put("profile", profile.id).put("readOnly", true)
    }

    fun stop(): JSONObject {
        scanner.stop()
        return JSONObject().put("state", "Stopped").put("readOnly", true)
    }

    fun snapshot(): JSONObject = JSONObject()
        .put("frames", JSONArray())
        .put("readOnly", true)
}
