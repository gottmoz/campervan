package se.gottmoz.camperagent.integration

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject

class CamperAgentBridge(context: Context) {
    private val settingsStore = IntegrationSettingsStore(context.applicationContext)
    private val repository = IntegrationRepository(settingsStore)

    @JavascriptInterface fun getIntegrationSnapshot(): String = ok(repository.snapshot())
    @JavascriptInterface fun getBatteryBmsSnapshot(): String = ok(repository.batteryBmsSnapshot())
    @JavascriptInterface fun getBatteryBmsSettings(): String = ok(settingsStore.getBatteryBmsSettings())
    @JavascriptInterface fun getVictronSettings(): String = ok(settingsStore.getVictronSettings())
    @JavascriptInterface fun getGarminSettings(): String = ok(settingsStore.getGarminSettings())
    @JavascriptInterface fun getObdSettings(): String = ok(settingsStore.getObdSettings())

    @JavascriptInterface
    fun saveVictronSettings(json: String): String = handle {
        val parsed = validatedJson(json)
        settingsStore.saveVictronSettings(parsed)
        settingsStore.getVictronSettings()
    }

    @JavascriptInterface
    fun saveBatteryBmsSettings(json: String): String = handle {
        val parsed = validatedJson(json)
        settingsStore.saveBatteryBmsSettings(parsed)
        settingsStore.getBatteryBmsSettings()
    }

    @JavascriptInterface
    fun saveGarminSettings(json: String): String = handle {
        val parsed = validatedJson(json)
        settingsStore.saveGarminSettings(parsed)
        settingsStore.getGarminSettings()
    }

    @JavascriptInterface
    fun saveObdSettings(json: String): String = handle {
        val parsed = validatedJson(json)
        settingsStore.saveObdSettings(parsed)
        settingsStore.getObdSettings()
    }

    @JavascriptInterface fun requestUsbPermission(kind: String): String = ok(JSONObject().put("kind", kind).put("status", "permission_request_registered"))
    @JavascriptInterface fun scanBatteryCan(): String = ok(JSONObject().put("state", "PassiveListenReady").put("frames", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun scanBatteryBluetooth(): String = ok(JSONObject().put("state", "DiscoveryOnly").put("devices", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun exportBatteryBmsDiagnostics(): String = ok(repository.batteryBmsSnapshot())
    @JavascriptInterface fun testVictronConnection(): String = ok(JSONObject().put("state", "Offline").put("readOnly", true))
    @JavascriptInterface fun testObdConnection(): String = ok(JSONObject().put("state", "PermissionRequired").put("readOnly", true))
    @JavascriptInterface fun scanNmeaBus(): String = ok(JSONObject().put("state", "Simulated").put("pgnCount", 0).put("readOnly", true))
    @JavascriptInterface fun exportIntegrationDiagnostics(): String = ok(repository.snapshot())

    private fun validatedJson(json: String): JSONObject {
        require(json.length <= 16_384) { "JSON payload too large" }
        val parsed = JSONObject(json)
        parsed.put("readOnly", true)
        return parsed
    }

    private fun handle(block: () -> JSONObject): String {
        return try {
            ok(block())
        } catch (error: Throwable) {
            fail(error.message ?: "Unknown error")
        }
    }

    private fun ok(data: JSONObject): String = JSONObject().put("ok", true).put("data", data).toString()
    private fun fail(error: String): String = JSONObject().put("ok", false).put("error", error).toString()
}
