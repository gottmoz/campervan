package se.gottmoz.camperagent.integration

import android.content.Context
import android.webkit.JavascriptInterface
import org.json.JSONObject
import se.gottmoz.camperagent.integration.canbus.CanDiscoveryRepository
import se.gottmoz.camperagent.integration.obd.FordTransitEcoBlue2016Profile
import se.gottmoz.camperagent.integration.usbserial.UsbSerialManager

class CamperAgentBridge(context: Context) {
    private val settingsStore = IntegrationSettingsStore(context.applicationContext)
    private val repository = IntegrationRepository(settingsStore)
    private val usbSerialManager = UsbSerialManager(context.applicationContext)
    private val canDiscoveryRepository = CanDiscoveryRepository()

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

    @JavascriptInterface fun scanUsbSerialDevices(): String = ok(JSONObject().put("devices", usbSerialManager.enumerateJson()).put("status", usbSerialManager.statusJson()))
    @JavascriptInterface fun getUsbPermissionStatus(): String = ok(usbSerialManager.statusJson())
    @JavascriptInterface fun requestUsbPermission(kind: String): String = handle {
        usbSerialManager.requestPermissionFirst(kind)
        JSONObject().put("kind", kind).put("status", usbSerialManager.statusJson()).put("readOnly", true)
    }
    @JavascriptInterface fun openUsbSerial(json: String): String = handle {
        val baud = validatedJson(json).optInt("baudRate", 115200)
        usbSerialManager.openFirst(baud)
        usbSerialManager.statusJson()
    }
    @JavascriptInterface fun closeUsbSerial(): String = handle {
        usbSerialManager.close()
        usbSerialManager.statusJson()
    }
    @JavascriptInterface fun connectObd(json: String): String = handle {
        val settings = validatedJson(json)
        val baud = if (settings.optString("baudRate", "Auto") == "Auto") FordTransitEcoBlue2016Profile.autoBaudOrder.first() else settings.optInt("baudRate", 115200)
        usbSerialManager.openFirst(baud)
        JSONObject()
            .put("state", if (usbSerialManager.status.value.open) "Serial open" else usbSerialManager.status.value.state.name)
            .put("baudRate", baud)
            .put("protocol", "ISO15765-4 CAN 11/500")
            .put("elmProtocol", FordTransitEcoBlue2016Profile.preferredElmProtocol)
            .put("initSequence", org.json.JSONArray(FordTransitEcoBlue2016Profile.initSequence))
            .put("readOnly", true)
    }
    @JavascriptInterface fun disconnectObd(): String = handle {
        usbSerialManager.close()
        JSONObject().put("state", "Disconnected").put("readOnly", true)
    }
    @JavascriptInterface fun getObdConnectionStatus(): String = ok(usbSerialManager.statusJson().put("protocol", "ISO15765-4 CAN 11/500").put("elmProtocol", "6"))
    @JavascriptInterface fun sendReadOnlyObdCommand(command: String): String = handle {
        val normalized = command.trim().uppercase()
        require(!isBlockedObdCommand(normalized)) { "Blocked unsafe OBD command" }
        JSONObject().put("command", normalized).put("queued", false).put("readOnly", true)
    }
    @JavascriptInterface fun scanSupportedPids(): String = ok(JSONObject().put("commands", org.json.JSONArray(listOf("0100", "0120", "0140", "0160"))).put("readOnly", true))
    @JavascriptInterface fun readDtcReadOnly(): String = ok(JSONObject().put("command", "03").put("dtcs", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun listCanAdapters(): String = ok(canDiscoveryRepository.listAdapters())
    @JavascriptInterface fun startCanScan(profileJson: String): String = handle {
        val profileId = validatedJson(profileJson).optString("profileId", "battery_bms")
        canDiscoveryRepository.start(profileId)
    }
    @JavascriptInterface fun stopCanScan(): String = ok(canDiscoveryRepository.stop())
    @JavascriptInterface fun getCanScanSnapshot(): String = ok(canDiscoveryRepository.snapshot())
    @JavascriptInterface fun scanBatteryCan(): String = ok(JSONObject().put("state", "PassiveListenReady").put("frames", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun startBatteryCanScan(json: String): String = startCanScan(json)
    @JavascriptInterface fun stopBatteryCanScan(): String = stopCanScan()
    @JavascriptInterface fun scanBatteryBluetooth(): String = ok(JSONObject().put("state", "DiscoveryOnly").put("devices", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun startBatteryBluetoothScan(): String = scanBatteryBluetooth()
    @JavascriptInterface fun stopBatteryBluetoothScan(): String = ok(JSONObject().put("state", "Stopped").put("readOnly", true))
    @JavascriptInterface fun exportBatteryBmsDiagnostics(): String = ok(repository.batteryBmsSnapshot())
    @JavascriptInterface fun testVictronConnection(): String = ok(JSONObject().put("state", "Offline").put("readOnly", true))
    @JavascriptInterface fun getVictronSnapshot(): String = ok(JSONObject().put("dbusPaths", org.json.JSONArray(se.gottmoz.camperagent.integration.victron.VictronDbusPaths.battery + se.gottmoz.camperagent.integration.victron.VictronDbusPaths.solar)).put("readOnly", true))
    @JavascriptInterface fun testObdConnection(): String = ok(JSONObject().put("state", "PermissionRequired").put("readOnly", true))
    @JavascriptInterface fun scanNmeaBus(): String = ok(JSONObject().put("state", "Simulated").put("pgnCount", 0).put("readOnly", true))
    @JavascriptInterface fun startNmea2000Scan(json: String): String = startCanScan(json)
    @JavascriptInterface fun stopNmea2000Scan(): String = stopCanScan()
    @JavascriptInterface fun getNmea2000Snapshot(): String = ok(canDiscoveryRepository.snapshot())
    @JavascriptInterface fun exportIntegrationDiagnostics(): String = ok(repository.snapshot())

    fun close() {
        usbSerialManager.dispose()
    }

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

    private fun isBlockedObdCommand(command: String): Boolean {
        val service = command.take(2)
        return service in setOf("04", "14", "2E", "10", "27", "28", "2F", "31", "3D", "85")
    }
}
