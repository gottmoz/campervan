package se.gottmoz.camperagent.integration

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.webkit.JavascriptInterface
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import se.gottmoz.camperagent.integration.canbus.CanDiscoveryRepository
import se.gottmoz.camperagent.integration.logging.RemoteLogUploader
import se.gottmoz.camperagent.integration.obd.FordTransitEcoBlue2016Profile
import se.gottmoz.camperagent.integration.obd.ObdFormulaEvaluator
import se.gottmoz.camperagent.integration.obd.ObdPidMapping
import se.gottmoz.camperagent.integration.obd.ObdPidMappingStore
import se.gottmoz.camperagent.integration.obd.ObdPidDecoder
import se.gottmoz.camperagent.integration.obd.ObdRepository
import se.gottmoz.camperagent.integration.obd.ObdResponseParser
import se.gottmoz.camperagent.integration.obd.VehicleCommandDefinition
import se.gottmoz.camperagent.integration.obd.VehicleCommandStore
import se.gottmoz.camperagent.integration.tcan485.TCan485Repository
import se.gottmoz.camperagent.integration.tcan485.TCan485SettingsStore
import se.gottmoz.camperagent.integration.usbserial.UsbSerialManager
import java.io.File
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.ConcurrentHashMap

class CamperAgentBridge(context: Context) {
    private val appContext = context.applicationContext
    private val settingsStore = IntegrationSettingsStore(context.applicationContext)
    private val repository = IntegrationRepository(settingsStore)
    private val usbSerialManager = UsbSerialManager(context.applicationContext)
    private val canDiscoveryRepository = CanDiscoveryRepository()
    private val remoteLogUploader = RemoteLogUploader(context.applicationContext)
    private val obdRepository = ObdRepository()
    private val obdPidMappingStore = ObdPidMappingStore(context.applicationContext)
    private val vehicleCommandStore = VehicleCommandStore(context.applicationContext)
    private val tcan485Repository = TCan485Repository(TCan485SettingsStore(context.applicationContext))
    private val obdCommandLock = Any()
    private val pidErrorCounts = ConcurrentHashMap<String, Int>()
    private val pidPausedUntil = ConcurrentHashMap<String, Long>()
    private val mappedValues = ConcurrentHashMap<String, Any>()
    private val commandLog = ArrayDeque<JSONObject>()
    @Volatile private var obdPolling = false
    @Volatile private var obdConnected = false
    @Volatile private var obdConnecting = false
    @Volatile private var obdState = "Disconnected"
    @Volatile private var lastObdSuccessEpochMs = 0L
    @Volatile private var lastObdError: String? = null
    @Volatile private var adapterName = "ELM327 v2.3"
    @Volatile private var adapterDescription = "OBDII to RS232 Interpreter"

    @JavascriptInterface fun getIntegrationSnapshot(): String = ok(repository.snapshot())
    @JavascriptInterface fun getBatteryBmsSnapshot(): String = ok(repository.batteryBmsSnapshot())
    @JavascriptInterface fun getBatteryBmsSettings(): String = ok(settingsStore.getBatteryBmsSettings())
    @JavascriptInterface fun getVictronSettings(): String = ok(settingsStore.getVictronSettings())
    @JavascriptInterface fun getGarminSettings(): String = ok(settingsStore.getGarminSettings())
    @JavascriptInterface fun getObdSettings(): String = ok(settingsStore.getObdSettings())
    @JavascriptInterface fun getRemoteLoggingSettings(): String = ok(remoteLogUploader.settings())
    @JavascriptInterface fun getTcan485Settings(): String = ok(tcan485Repository.getSettings())
    @JavascriptInterface fun saveTcan485Settings(json: String): String = handle { tcan485Repository.saveSettings(validatedJson(json)) }
    @JavascriptInterface fun startTcan485Discovery(): String = handle { tcan485Repository.startDiscovery() }
    @JavascriptInterface fun stopTcan485Discovery(): String = handle { tcan485Repository.stopDiscovery() }
    @JavascriptInterface fun getTcan485DiscoverySnapshot(): String = ok(tcan485Repository.snapshot())
    @JavascriptInterface fun testTcan485Health(baseUrl: String): String = handle { tcan485Repository.health(baseUrl) }
    @JavascriptInterface fun getTcan485GatewayStatus(baseUrl: String): String = handle { tcan485Repository.gatewayStatus(baseUrl) }
    @JavascriptInterface fun getTcan485Rs485Status(baseUrl: String): String = handle { tcan485Repository.rs485Status(baseUrl) }
    @JavascriptInterface fun getTcan485BmsLatest(baseUrl: String): String = handle { tcan485Repository.bmsLatest(baseUrl) }
    @JavascriptInterface fun getTcan485Rs485RawLatest(baseUrl: String): String = handle { tcan485Repository.rs485RawLatest(baseUrl) }
    @JavascriptInterface fun getTcan485CanStatus(baseUrl: String): String = handle { tcan485Repository.canStatus(baseUrl) }
    @JavascriptInterface fun getTcan485CanFramesLatest(baseUrl: String): String = handle { tcan485Repository.canFramesLatest(baseUrl) }
    @JavascriptInterface fun saveTcan485WifiSettings(baseUrl: String, json: String): String = handle { tcan485Repository.saveWifiSettings(baseUrl, validatedJson(json)) }
    @JavascriptInterface fun saveTcan485CanSettings(baseUrl: String, json: String): String = handle { tcan485Repository.saveCanSettings(baseUrl, validatedJson(json)) }
    @JavascriptInterface fun saveTcan485Rs485Settings(baseUrl: String, json: String): String = handle { tcan485Repository.saveRs485Settings(baseUrl, validatedJson(json)) }
    @JavascriptInterface fun rebootTcan485(baseUrl: String): String = handle { tcan485Repository.reboot(baseUrl) }
    @JavascriptInterface fun openAndroidHotspotSettings(): String = handle {
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { appContext.startActivity(intent) }
            .recoverCatching {
                appContext.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        JSONObject().put("opened", true).put("target", "Android wireless settings")
    }

    @JavascriptInterface fun saveRemoteLoggingSettings(json: String): String = handle {
        val parsed = validatedJson(json)
        val before = remoteLogUploader.settings().toString()
        remoteLogUploader.setEnabled(parsed.optBoolean("enabled", true))
        remoteLogUploader.setServerUrl(parsed.optString("serverUrl", RemoteLogUploader.DEFAULT_URL))
        val after = remoteLogUploader.settings()
        if (before != after.toString()) {
            runBlocking { remoteLogUploader.uploadLog("INFO", "RemoteLogging", "tunnel/server settings changed", after) }
        }
        remoteLogUploader.settings()
    }

    @JavascriptInterface fun testRemoteLoggingServer(): String = handle {
        runBlocking { remoteLogUploader.testConnection() }
    }

    @JavascriptInterface fun getRemoteRuntimeStatus(): String = handle {
        runBlocking { remoteLogUploader.runtimeStatus() }
    }

    @JavascriptInterface fun fetchLatestRemoteLogs(): String = handle {
        runBlocking { remoteLogUploader.latestLogs() }
    }

    @JavascriptInterface fun uploadDiagnosticsNow(): String = handle {
        val diagnostics = diagnosticsJson()
        runBlocking { remoteLogUploader.uploadDiagnostics(diagnostics) }
    }

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
    @JavascriptInterface fun getUsbPermissionStatus(): String = ok(usbSerialManager.currentPermissionStatus().let { usbSerialManager.statusJson() })
    @JavascriptInterface fun getObdPidMappings(): String = ok(obdPidMappingStore.getJson())
    @JavascriptInterface fun getObdPidLibrary(): String = ok(obdPidMappingStore.getJson())
    @JavascriptInterface fun saveObdPidMappings(json: String): String = handle { obdPidMappingStore.save(validatedJson(json)) }
    @JavascriptInterface fun saveObdPidLibrary(json: String): String = handle { obdPidMappingStore.save(validatedJson(json)) }
    @JavascriptInterface fun setObdPidEnabled(id: String, enabled: String): String = handle {
        val enabledValue = enabled.equals("true", ignoreCase = true)
        val result = obdPidMappingStore.setEnabled(id, enabledValue)
        runBlocking { remoteLogUploader.uploadLog("INFO", "ObdPidLibrary", "PID ${if (enabledValue) "enabled" else "disabled"}", JSONObject().put("id", id).put("enabled", enabledValue)) }
        result
    }
    @JavascriptInterface fun resetObdPidMappingsToDefault(): String = ok(obdPidMappingStore.reset())
    @JavascriptInterface fun resetObdPidLibraryDefaults(): String = ok(obdPidMappingStore.reset())
    @JavascriptInterface fun exportObdPidLibrary(): String = ok(obdPidMappingStore.getJson())
    @JavascriptInterface fun importObdPidLibrary(json: String): String = handle { obdPidMappingStore.save(validatedJson(json)) }
    @JavascriptInterface fun getObdPidMappingStatus(): String = ok(pidMappingStatusJson())
    @JavascriptInterface fun getVehicleCommands(): String = ok(vehicleCommandStore.getJson())
    @JavascriptInterface fun saveVehicleCommands(json: String): String = handle { vehicleCommandStore.save(validatedJson(json)) }
    @JavascriptInterface fun saveVehicleCommand(json: String): String = handle {
        val command = VehicleCommandDefinition.fromJson(validatedJson(json))
        val result = vehicleCommandStore.saveCommand(command)
        runBlocking { remoteLogUploader.uploadLog("INFO", "VehicleCommand", "command saved", command.toJson()) }
        result
    }
    @JavascriptInterface fun exportVehicleCommands(): String = ok(vehicleCommandStore.exportJson())
    @JavascriptInterface fun importVehicleCommands(json: String): String = handle { vehicleCommandStore.save(validatedJson(json)) }
    @JavascriptInterface fun getVehicleCommandLog(): String = ok(JSONObject().put("log", JSONArray(commandLog.toList())))
    @JavascriptInterface fun testVehicleCommand(json: String): String = handle { executeVehicleCommandDefinition(VehicleCommandDefinition.fromJson(validatedJson(json)), persist = false) }
    @JavascriptInterface fun executeVehicleCommand(commandId: String): String = handle {
        val command = vehicleCommandStore.find(commandId) ?: error("Unknown vehicle command: $commandId")
        executeVehicleCommandDefinition(command, persist = true)
    }
    @JavascriptInterface fun getSystemHealthSnapshot(): String = ok(systemHealthSnapshot())
    @JavascriptInterface fun getNetworkStatus(): String = ok(networkStatusJson())
    @JavascriptInterface fun testInternetConnection(): String = handle { httpHealth("https://www.google.com/generate_204", false) }
    @JavascriptInterface fun testObdPidMapping(json: String): String = handle {
        val mapping = ObdPidMapping.fromJson(validatedJson(json))
        require(mapping.service.isNotBlank() && mapping.pid.isNotBlank()) { "Service and PID are required" }
        require(!isBlockedObdCommand(mapping.command)) { "Blocked unsafe OBD command" }
        if (usbSerialManager.status.value.open) mapping.setupCommands.forEach { setup ->
            if (setup.isNotBlank()) sendObdCommand(setup, 1_500, "PidMappingTestSetup")
        }
        val result = if (usbSerialManager.status.value.open) sendObdCommand(mapping.command, mapping.timeoutMs, "PidMappingTest") else JSONObject()
            .put("direction", "error")
            .put("command", mapping.command)
            .put("error", "USB serial port is not open")
        val decoded = runCatching { ObdFormulaEvaluator.decode(mapping, result.optString("response")) }.getOrNull()
        JSONObject()
            .put("tx", mapping.command)
            .put("rx", result.optString("response", ""))
            .put("decoded", decoded ?: JSONObject.NULL)
            .put("error", result.optString("error", ""))
            .put("rawLog", obdRepository.rawLogTail())
            .put("readOnly", true)
    }
    @JavascriptInterface fun requestUsbPermission(kind: String): String = handle {
        val permissionStatus = usbSerialManager.ensurePermissionFirst(kind)
        val data = JSONObject().put("kind", kind).put("status", usbSerialManager.statusJson()).put("readOnly", true)
        val message = if (permissionStatus.permissionGranted) "USB permission already granted" else "USB permission requested"
        runBlocking { remoteLogUploader.uploadLog("INFO", "UsbSerialManager", message, data) }
        data
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
        if (obdConnected) {
            return@handle obdConnectedJson("Already connected", "", "")
        }
        if (obdConnecting) {
            return@handle vehicleTelemetrySnapshot().put("state", "Connecting").put("message", "OBD connection already in progress")
        }
        obdConnecting = true
        try {
            obdState = "Connecting"
        val settings = validatedJson(json)
        val baudText = settings.optString("baudRate", "Auto")
        val baud = if (baudText == "Auto") FordTransitEcoBlue2016Profile.autoBaudOrder.first() else baudText.toIntOrNull() ?: 115200
        obdRepository.addLog("state", state = "OBD connect started")
        runBlocking { remoteLogUploader.uploadLog("INFO", "ObdRepository", "OBD connect started", JSONObject().put("baudRate", baud).put("protocol", "ISO15765-4 CAN 11/500")) }
        val permissionStatus = usbSerialManager.ensurePermissionFirst("obd")
        if (!permissionStatus.permissionGranted) {
            obdRepository.addLog("state", state = permissionStatus.state.name, response = permissionStatus.error)
            runBlocking { remoteLogUploader.uploadLog("INFO", "UsbSerialManager", "Waiting for USB permission", usbSerialManager.statusJson()) }
            return@handle JSONObject()
                .put("state", permissionStatus.state.name)
                .put("baudRate", baud)
                .put("protocol", "ISO15765-4 CAN 11/500")
                .put("message", permissionStatus.error ?: "Waiting for Android USB permission")
                .put("rawLog", obdRepository.rawLogTail())
                .put("readOnly", true)
        }
        val openStatus = usbSerialManager.openFirst(baud)
        if (!openStatus.open) {
            obdRepository.addLog("error", state = "SerialOpenFailed", response = openStatus.error)
            runBlocking { remoteLogUploader.uploadLog("ERROR", "UsbSerialManager", "Serial not open", usbSerialManager.statusJson()) }
            return@handle JSONObject()
                .put("state", "SerialOpenFailed")
                .put("baudRate", baud)
                .put("protocol", "ISO15765-4 CAN 11/500")
                .put("message", openStatus.error ?: "Serial port is not open")
                .put("rawLog", obdRepository.rawLogTail())
                .put("readOnly", true)
        }
        val probe = probeObdAdapter(baudText == "Auto")
        if (probe.optString("state") == "ObdConnected") {
            return@handle probe
                .put("baudRate", baud)
                .put("initSequence", JSONArray(FordTransitEcoBlue2016Profile.initSequence))
                .put("readOnly", true)
        }
        val finalState = probe.optString("state", "Serial open")
        JSONObject()
            .put("state", finalState)
            .put("baudRate", baud)
            .put("protocol", "ISO15765-4 CAN 11/500")
            .put("elmProtocol", FordTransitEcoBlue2016Profile.preferredElmProtocol)
            .put("initSequence", org.json.JSONArray(FordTransitEcoBlue2016Profile.initSequence))
            .put("message", probe.optString("message"))
            .put("lastTx", probe.optString("lastTx", ""))
            .put("lastRx", probe.optString("lastRx", ""))
            .put("lastError", probe.optString("lastError", ""))
            .put("rawLog", obdRepository.rawLogTail())
            .put("readOnly", true)
        } finally {
            obdConnecting = false
        }
    }
    @JavascriptInterface fun disconnectObd(): String = handle {
        obdPolling = false
        obdConnected = false
        obdState = "Disconnected"
        usbSerialManager.close()
        JSONObject().put("state", "Disconnected").put("readOnly", true)
    }
    @JavascriptInterface fun getObdConnectionStatus(): String = ok(vehicleTelemetrySnapshot())
    @JavascriptInterface fun getVehicleTelemetrySnapshot(): String = ok(vehicleTelemetrySnapshot())
    @JavascriptInterface fun sendReadOnlyObdCommand(command: String): String = handle {
        val normalized = command.trim().uppercase()
        require(!isBlockedObdCommand(normalized)) { "Blocked unsafe OBD command" }
        val result = if (usbSerialManager.status.value.open) sendObdCommand(normalized, 2_000, "RawTest") else JSONObject()
            .put("direction", "error")
            .put("command", normalized)
            .put("error", "USB serial port is not open")
            .put("state", "RawTest")
        result.put("rawLog", obdRepository.rawLogTail()).put("readOnly", true)
    }
    @JavascriptInterface fun scanSupportedPids(): String = ok(JSONObject().put("commands", org.json.JSONArray(listOf("0100", "0120", "0140", "0160"))).put("readOnly", true))
    @JavascriptInterface fun readDtcReadOnly(): String = ok(JSONObject().put("command", "03").put("dtcs", org.json.JSONArray()).put("readOnly", true))
    @JavascriptInterface fun startElmMonitorReadOnly(): String = handle {
        require(usbSerialManager.status.value.open) { "USB serial port is not open" }
        listOf("ATH1", "ATS0", "ATAL", "ATCAF0").forEach { sendObdCommand(it, 2_000, "ElmMonitorSetup") }
        sendObdCommand("ATMA", 5_000, "ElmMonitorReadOnly")
            .put("mode", "ELM ATMA experimental read-only")
            .put("rawLog", obdRepository.rawLogTail())
            .put("readOnly", true)
    }
    @JavascriptInterface fun stopElmMonitorReadOnly(): String = handle {
        if (usbSerialManager.status.value.open) usbSerialManager.writeBytes("\r".toByteArray(Charsets.US_ASCII))
        JSONObject().put("state", "ElmMonitorStopped").put("readOnly", true)
    }
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
    @JavascriptInterface fun exportIntegrationDiagnostics(): String = handle {
        val json = diagnosticsJson()
        val path = diagnosticsFile()
        path.parentFile?.mkdirs()
        path.writeText(json.toString(2), Charsets.UTF_8)
        runBlocking { remoteLogUploader.uploadLog("INFO", "Diagnostics", "diagnostics export created", JSONObject().put("path", path.absolutePath)) }
        JSONObject().put("path", path.absolutePath).put("json", json)
    }

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

    private fun ok(data: JSONObject): String = JSONObject().put("ok", true).put("data", data).put("error", JSONObject.NULL).toString()
    private fun fail(error: String): String = JSONObject().put("ok", false).put("data", JSONObject.NULL).put("error", error).toString()

    private fun diagnosticsJson(): JSONObject = JSONObject()
        .put("timestamp", nowIso())
        .put("appVersion", "0.1.0")
        .put("android", JSONObject()
            .put("manufacturer", Build.MANUFACTURER)
            .put("model", Build.MODEL)
            .put("sdk", Build.VERSION.SDK_INT))
        .put("usbStatus", usbSerialManager.statusJson())
        .put("obdState", usbSerialManager.status.value.state.name)
        .put("obdRawLogTail", obdRepository.rawLogTail())
        .put("obdTelemetry", obdRepository.telemetryJson())
        .put("obdSettings", settingsStore.getObdSettings())
        .put("vLinker", JSONObject()
            .put("vendorId", usbSerialManager.status.value.vendorId ?: JSONObject.NULL)
            .put("productId", usbSerialManager.status.value.productId ?: JSONObject.NULL)
            .put("lastPermissionState", usbSerialManager.status.value.state.name))
        .put("victronStatus", JSONObject().put("state", "Offline").put("readOnly", true))
        .put("garminStatus", JSONObject().put("state", "Offline").put("readOnly", true))
        .put("bmsStatus", repository.batteryBmsSnapshot())
        .put("remoteLoggingSettings", remoteLogUploader.settings())
        .put("recentErrors", JSONArray())

    private fun probeObdAdapter(autoBaud: Boolean): JSONObject {
        usbSerialManager.drainInput(300)
        runCatching {
            usbSerialManager.writeBytes("\r".toByteArray(Charsets.US_ASCII))
            usbSerialManager.readUntilPrompt(700)
        }
        var lastTx = ""
        var lastRx = ""
        var lastError = ""
        var adapterIdentity = ""
        var detectedProtocol = ""
        fun run(command: String, timeoutMs: Int, delayAfterMs: Long = 0): JSONObject {
            lastTx = command
            val result = sendObdCommand(command, timeoutMs, "AdapterProbing")
            lastRx = result.optString("response", "")
            lastError = result.optString("error", "")
            if (delayAfterMs > 0) Thread.sleep(delayAfterMs)
            return result
        }

        var ati = run("ATI", 2_000)
        if (ati.optString("direction") == "timeout") {
            runCatching {
                usbSerialManager.writeBytes("\r\r".toByteArray(Charsets.US_ASCII))
                usbSerialManager.drainInput(300)
            }
            ati = run("ATI", 2_000)
        }
        if (ati.optString("direction") == "timeout" && autoBaud) {
            return JSONObject()
                .put("state", "AdapterNoResponse")
                .put("message", "USB serial is open but vLinker did not answer ATI. Try different USB baud or reconnect adapter.")
                .put("lastTx", lastTx)
                .put("lastRx", lastRx)
                .put("lastError", lastError)
        }
        adapterIdentity = ati.optString("response")
        adapterName = adapterIdentity.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty().ifBlank { "ELM327 v2.3" }
        val atDescription = if (adapterIdentity.isBlank()) run("AT@1", 2_000).optString("response") else run("AT@1", 2_000).optString("response")
        adapterDescription = atDescription.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty().ifBlank { "OBDII to RS232 Interpreter" }
        obdState = "AdapterDetected"
        run("ATZ", 3_000, 1_000)
        listOf("ATE0", "ATL0", "ATS0", "ATH1", "ATAL", "ATST96", "ATSP6", "ATDP", "ATDPN").forEach { run(it, 2_000) }
        detectedProtocol = rawLogValue("ATDP").ifBlank { "ISO 15765-4 (CAN 11/500)" }
        val supported = run("0100", 5_000)
        val valid0100 = ObdResponseParser.isValid0100Response(supported.optString("response"))
        if (
            supported.optString("response").contains("NO DATA", ignoreCase = true) ||
            supported.optString("direction") == "timeout" ||
            !valid0100
        ) {
            return JSONObject()
                .put("state", "EcuNoResponse")
                .put("message", "vLinker answers AT commands, but vehicle ECU did not respond on ISO15765-4 CAN11/500.")
                .put("lastTx", lastTx)
                .put("lastRx", lastRx)
                .put("lastError", lastError)
        }
        val decodedSupportedPids = ObdPidDecoder.decodeSupportedPids(supported.optString("response"), 0x00)
        val supportedPids = if (decodedSupportedPids.isNotEmpty()) decodedSupportedPids else FALLBACK_POLL_PIDS
        val pidWarning = if (decodedSupportedPids.isEmpty()) "PID bitmap decode failed, using fallback poll list" else ""
        if (pidWarning.isNotBlank()) {
            obdRepository.addLog("state", state = "ObdConnected", response = pidWarning)
            runBlocking { remoteLogUploader.uploadLog("WARN", "ObdRepository", pidWarning, JSONObject().put("ecuResponse", supported.optString("response")).put("fallbackPids", JSONArray(FALLBACK_POLL_PIDS.sorted()))) }
        }
        obdRepository.updateSupportedPids(supportedPids)
        obdConnected = true
        obdConnecting = false
        lastObdSuccessEpochMs = System.currentTimeMillis()
        lastObdError = null
        obdState = "ObdConnected"
        startObdPolling(supportedPids)
        runBlocking {
            remoteLogUploader.uploadLog(
                "INFO",
                "ObdRepository",
                "state ObdConnected",
                JSONObject()
                    .put("protocol", "ISO15765-4 CAN 11/500")
                    .put("elmProtocol", "6")
                    .put("ecuResponse", supported.optString("response"))
                    .put("supportedPids", JSONArray(supportedPids.sorted()))
                    .put("polling", obdPolling)
            )
        }
        return obdConnectedJson("Adapter and ECU verified", lastTx, lastRx)
            .put("adapter", adapterIdentity)
            .put("detectedProtocol", detectedProtocol)
            .put("lastEcuResponse", supported.optString("response"))
            .put("supportedPids", JSONArray(supportedPids.sorted()))
            .put("warning", pidWarning)
            .put("lastError", lastError)
    }

    private fun sendObdCommand(command: String, timeoutMs: Int, state: String): JSONObject {
        val started = System.currentTimeMillis()
        return try {
            synchronized(obdCommandLock) {
                obdRepository.addLog("tx", command = command, state = state)
                runBlocking { remoteLogUploader.uploadLog("INFO", "ObdRepository", "OBD TX", JSONObject().put("direction", "tx").put("command", command).put("state", state)) }
                usbSerialManager.writeBytes((command.trim() + "\r").toByteArray(Charsets.US_ASCII))
                val response = usbSerialManager.readUntilPrompt(timeoutMs).trim()
                val elapsed = System.currentTimeMillis() - started
                if (response.isBlank()) {
                    obdRepository.addLog("timeout", command = command, state = state, response = "No prompt/response")
                    runBlocking { remoteLogUploader.uploadLog("WARN", "ObdRepository", "OBD timeout", JSONObject().put("direction", "timeout").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", "No prompt/response")) }
                    JSONObject().put("direction", "timeout").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", "No prompt/response")
                } else {
                    obdRepository.addLog("rx", command = command, response = response, state = state, elapsedMs = elapsed)
                    runBlocking { remoteLogUploader.uploadLog("INFO", "ObdRepository", "OBD RX", JSONObject().put("direction", "rx").put("command", command).put("response", response).put("elapsedMs", elapsed).put("state", state)) }
                    JSONObject().put("direction", "rx").put("command", command).put("response", response).put("elapsedMs", elapsed).put("state", state)
                }
            }
        } catch (error: Throwable) {
            val elapsed = System.currentTimeMillis() - started
            lastObdError = error.message ?: "unknown"
            obdRepository.addLog("error", command = command, response = error.message, state = state, elapsedMs = elapsed)
            runBlocking { remoteLogUploader.uploadLog("ERROR", "ObdRepository", "OBD error", JSONObject().put("direction", "error").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", error.message ?: "unknown")) }
            JSONObject().put("direction", "error").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", error.message ?: "unknown")
        }
    }

    private fun startObdPolling(supportedPids: Set<String>) {
        if (obdPolling) return
        val initialMappings = obdPidMappingStore.enabledMappings()
            .ifEmpty { ObdPidMapping.defaults().filter { it.enabled && it.pid.isNotBlank() } }
        obdPolling = true
        obdState = "Polling"
        runBlocking {
            remoteLogUploader.uploadLog("INFO", "ObdRepository", "polling start", JSONObject().put("pids", JSONArray(initialMappings.map { it.pid }.distinct())))
        }
        Thread {
            var nextFast = 0L
            var nextHealth = 0L
            val nextByFunction = ConcurrentHashMap<String, Long>()
            var recoveryAttempted = false
            while (obdPolling && usbSerialManager.status.value.open) {
                val now = System.currentTimeMillis()
                val mappings = obdPidMappingStore.enabledMappings()
                    .ifEmpty { ObdPidMapping.defaults().filter { it.enabled && it.pid.isNotBlank() } }
                if (now >= nextFast) {
                    mappings.filter { it.pollIntervalMs <= 1_000 }.forEach { pollMapping(it) }
                    nextFast = now + 750
                }
                mappings.filter { it.pollIntervalMs > 1_000 }.forEach { mapping ->
                    val next = nextByFunction[mapping.functionKey] ?: 0L
                    if (now >= next) {
                        pollMapping(mapping)
                        nextByFunction[mapping.functionKey] = now + mapping.pollIntervalMs
                    }
                }
                if (now >= nextHealth) {
                    val ageMs = if (lastObdSuccessEpochMs == 0L) Long.MAX_VALUE else now - lastObdSuccessEpochMs
                    if (ageMs > 10_000 && ageMs <= 20_000) {
                        obdState = "Stale"
                        runBlocking { remoteLogUploader.uploadLog("WARN", "ObdRepository", "stale detected", vehicleTelemetrySnapshot()) }
                        if (!recoveryAttempted) {
                            recoveryAttempted = true
                            recoverPolling()
                        }
                    }
                    if (ageMs > 20_000) {
                        obdState = "Reconnecting"
                        runBlocking { remoteLogUploader.uploadLog("ERROR", "ObdRepository", "recovery failure", vehicleTelemetrySnapshot()) }
                        recoverPolling()
                    }
                    nextHealth = now + 5_000
                }
                Thread.sleep(100L)
            }
            obdPolling = false
            if (!usbSerialManager.status.value.open) {
                obdConnected = false
                obdState = "Disconnected"
            }
        }.apply {
            name = "CamperObdPolling"
            isDaemon = true
            start()
        }
    }

    private fun pollPid(pid: String) {
        val mapping = ObdPidMapping.defaults().firstOrNull { it.pid == pid } ?: return
        pollMapping(mapping)
    }

    private fun pollMapping(mapping: ObdPidMapping) {
        val now = System.currentTimeMillis()
        if ((pidPausedUntil[mapping.functionKey] ?: 0L) > now) return
        mapping.setupCommands.forEach { setup ->
            if (setup.isNotBlank()) sendObdCommand(setup, 1_500, "PollingSetup")
        }
        val result = sendObdCommand(mapping.command, mapping.timeoutMs, "Polling")
        val response = result.optString("response")
        val decoded = runCatching { ObdFormulaEvaluator.decode(mapping, response) }.getOrNull()
        if (result.optString("direction") == "rx" && decoded != null && !response.contains("NO DATA", ignoreCase = true)) {
            lastObdSuccessEpochMs = System.currentTimeMillis()
            lastObdError = null
            obdState = "Polling"
            val value = decoded.optDouble("value")
            mappedValues[mapping.functionKey] = value
            if (mapping.service == "01") obdRepository.mergeTelemetry(ObdPidDecoder.decodeTelemetry(mapping.pid, response))
            runBlocking { remoteLogUploader.uploadLog("INFO", "ObdRepository", "polling success", JSONObject().put("pid", mapping.pid).put("functionKey", mapping.functionKey).put("response", response).put("value", value)) }
        } else {
            val errorCount = (pidErrorCounts[mapping.functionKey] ?: 0) + 1
            pidErrorCounts[mapping.functionKey] = errorCount
            if (errorCount >= 3) pidPausedUntil[mapping.functionKey] = now + 30_000
            lastObdError = result.optString("error").ifBlank { response.ifBlank { "No valid PID response" } }
            runBlocking { remoteLogUploader.uploadLog("WARN", "ObdRepository", "polling timeout", JSONObject().put("pid", mapping.pid).put("functionKey", mapping.functionKey).put("errorCount", errorCount).put("paused", errorCount >= 3).put("lastError", lastObdError)) }
        }
    }

    private fun executeVehicleCommandDefinition(command: VehicleCommandDefinition, persist: Boolean): JSONObject {
        if (!obdConnected || !usbSerialManager.status.value.open) {
            val reason = if (!obdConnected) "obd not connected" else "serial not open"
            logVehicleCommand("Vehicle command blocked - $reason", command, null, null, reason)
            error(reason)
        }
        command.blockedReason()?.let { reason ->
            logVehicleCommand("Vehicle command blocked - $reason", command, null, null, reason)
            error(reason)
        }
        val lastSent = command.lastSentEpochMs ?: 0L
        val cooldownLeft = command.cooldownMs - (System.currentTimeMillis() - lastSent)
        if (cooldownLeft > 0) {
            val reason = "cooldown active for ${cooldownLeft}ms"
            logVehicleCommand("Vehicle command blocked - $reason", command, null, null, reason)
            error(reason)
        }
        command.setupCommands.forEach { setup ->
            if (setup.isNotBlank()) sendObdCommand(setup, 1_500, "VehicleCommandSetup")
        }
        logVehicleCommand("Vehicle command TX", command, command.command, null, null)
        val result = sendObdCommand(command.command, 3_000, "VehicleCommand")
        val rx = result.optString("response")
        val error = result.optString("error").ifBlank { null }
        if (persist) vehicleCommandStore.updateResult(command, command.command, rx, error)
        val verified = verifyCommandStatus(command)
        val message = if (error == null) "Vehicle command RX" else "Vehicle command failed"
        logVehicleCommand(message, command, command.command, rx, error)
        return JSONObject()
            .put("commandId", command.id)
            .put("displayName", command.displayName)
            .put("enabled", command.enabled)
            .put("verifiedByUser", command.verifiedByUser)
            .put("tx", command.command)
            .put("rx", rx)
            .put("expectedPositiveResponse", command.expectedPositiveResponse ?: JSONObject.NULL)
            .put("expectedStatusFunctionKey", command.expectedStatusFunctionKey ?: JSONObject.NULL)
            .put("expectedStatusValue", command.expectedStatusValue ?: JSONObject.NULL)
            .put("verificationResult", verified)
            .put("statusVerified", verified)
            .put("error", error ?: JSONObject.NULL)
            .put("readOnly", false)
    }

    private fun verifyCommandStatus(command: VehicleCommandDefinition): Boolean {
        val key = command.expectedStatusFunctionKey ?: return false
        val expected = command.expectedStatusValue ?: return false
        val mapping = obdPidMappingStore.getMappings().firstOrNull { it.functionKey == key } ?: return false
        mapping.setupCommands.forEach { setup -> if (setup.isNotBlank()) sendObdCommand(setup, 1_500, "VehicleCommandVerifySetup") }
        val result = sendObdCommand(mapping.command, mapping.timeoutMs, "VehicleCommandVerify")
        val decoded = runCatching { ObdFormulaEvaluator.decode(mapping, result.optString("response")) }.getOrNull() ?: return false
        val actual = "%02X".format(decoded.optDouble("value").toInt())
        mappedValues[key] = decoded.optDouble("value")
        val ok = actual == expected.uppercase()
        runBlocking { remoteLogUploader.uploadLog(if (ok) "INFO" else "WARN", "VehicleCommand", "Vehicle command verified by status PID", JSONObject().put("commandId", command.id).put("functionKey", key).put("expected", expected).put("actual", actual)) }
        return ok
    }

    private fun logVehicleCommand(message: String, command: VehicleCommandDefinition, tx: String?, rx: String?, error: String?) {
        val entry = JSONObject()
            .put("timestamp", nowIso())
            .put("commandId", command.id)
            .put("displayName", command.displayName)
            .put("setupCommands", JSONArray(command.setupCommands))
            .put("tx", tx ?: JSONObject.NULL)
            .put("rx", rx ?: JSONObject.NULL)
            .put("success", error == null)
            .put("error", error ?: JSONObject.NULL)
            .put("userVerified", command.verifiedByUser)
        if (commandLog.size >= 100) commandLog.removeFirst()
        commandLog.addLast(entry)
        runBlocking { remoteLogUploader.uploadLog(if (error == null) "INFO" else "WARN", "VehicleCommand", message, entry) }
    }

    private fun recoverPolling() {
        runBlocking { remoteLogUploader.uploadLog("WARN", "ObdRepository", "recovery start", vehicleTelemetrySnapshot()) }
        val protocol = sendObdCommand("ATDP", 2_000, "Recovery")
        val supported = sendObdCommand("0100", 5_000, "Recovery")
        if (protocol.optString("direction") == "rx" && ObdResponseParser.isValid0100Response(supported.optString("response"))) {
            lastObdSuccessEpochMs = System.currentTimeMillis()
            lastObdError = null
            obdState = "Polling"
            runBlocking { remoteLogUploader.uploadLog("INFO", "ObdRepository", "recovery success", JSONObject().put("protocol", protocol.optString("response")).put("ecuResponse", supported.optString("response"))) }
        } else {
            obdState = "Reconnecting"
            lastObdError = supported.optString("error").ifBlank { "Recovery probe failed" }
            runBlocking { remoteLogUploader.uploadLog("ERROR", "ObdRepository", "recovery failure", JSONObject().put("lastError", lastObdError)) }
        }
    }

    private fun vehicleTelemetrySnapshot(): JSONObject {
        val now = System.currentTimeMillis()
        val stale = obdConnected && lastObdSuccessEpochMs > 0 && now - lastObdSuccessEpochMs > 10_000
        val errors = JSONObject()
        pidErrorCounts.toSortedMap().forEach { (pid, count) -> errors.put(pid, count) }
        val telemetry = obdRepository.telemetryJson()
            .put("oilTempC", JSONObject.NULL)
            .put("outsideTempC", obdRepository.telemetryJson().opt("ambientTempC"))
            .put("engineLoadPercent", JSONObject.NULL)
            .put("boostBar", JSONObject.NULL)
            .put("egtTempC", JSONObject.NULL)
            .put("dpfSootPercent", JSONObject.NULL)
        val genericValues = JSONObject()
        mappedValues.forEach { (key, value) ->
            telemetry.put(key, value)
            val mapping = obdPidMappingStore.getMappings().firstOrNull { it.functionKey == key }
            genericValues.put(key, JSONObject()
                .put("functionKey", key)
                .put("displayName", mapping?.label ?: key)
                .put("value", value)
                .put("unit", mapping?.unit ?: "")
                .put("sourcePidId", mapping?.functionKey ?: JSONObject.NULL)
                .put("status", if ((pidErrorCounts[key] ?: 0) > 0) "Warning" else "Online")
                .put("updatedAtEpochMs", if (lastObdSuccessEpochMs > 0) lastObdSuccessEpochMs else JSONObject.NULL))
        }
        telemetry.put("values", genericValues)
        return usbSerialManager.statusJson()
            .put("state", if (obdConnected) obdState else usbSerialManager.status.value.state.name)
            .put("connected", obdConnected)
            .put("verified", obdConnected)
            .put("polling", obdPolling)
            .put("stale", stale)
            .put("lastSuccessEpochMs", if (lastObdSuccessEpochMs > 0) lastObdSuccessEpochMs else JSONObject.NULL)
            .put("lastError", lastObdError ?: JSONObject.NULL)
            .put("adapterName", adapterName)
            .put("adapterDescription", adapterDescription)
            .put("protocol", "ISO 15765-4 (CAN 11/500)")
            .put("elmProtocol", "6")
            .put("supportedPids", obdRepository.telemetryJson().optJSONArray("supportedPids") ?: JSONArray())
            .put("pidErrorCounts", errors)
            .put("telemetry", telemetry)
    }

    private fun pidMappingStatusJson(): JSONObject {
        val paused = JSONObject()
        val now = System.currentTimeMillis()
        pidPausedUntil.toSortedMap().forEach { (key, until) ->
            paused.put(key, if (until > now) until - now else 0)
        }
        return JSONObject()
            .put("mappings", ObdPidMapping.toJsonArray(obdPidMappingStore.enabledMappings()))
            .put("lastValues", JSONObject(mappedValues.toMap()))
            .put("pidErrorCounts", JSONObject(pidErrorCounts.toMap()))
            .put("pausedMs", paused)
            .put("readOnly", true)
    }

    private fun systemHealthSnapshot(): JSONObject {
        val usb = usbSerialManager.statusJson()
        val remote = remoteLogUploader.settings()
        val tcan = tcan485Repository.snapshot()
        return JSONObject()
            .put("usb", usb)
            .put("obd", vehicleTelemetrySnapshot())
            .put("remoteLogging", remote)
            .put("tcan485", tcan)
            .put("bms", JSONObject().put("state", repository.batteryBmsSnapshot().optJSONObject("telemetry")?.optString("source", "unknown") ?: "unknown").put("source", "battery_bms_repository"))
            .put("victron", JSONObject().put("enabled", settingsStore.getVictronSettings().optBoolean("enabled")).put("state", "Disconnected").put("source", "settings"))
            .put("garmin", JSONObject().put("enabled", settingsStore.getGarminSettings().optBoolean("enabled")).put("state", canDiscoveryRepository.snapshot().optString("state", "Stopped")).put("source", "can_discovery"))
            .put("lastUpdated", nowIso())
    }

    private fun networkStatusJson(): JSONObject = JSONObject()
        .put("android", JSONObject()
            .put("sdk", Build.VERSION.SDK_INT)
            .put("localIps", JSONArray(localIps()))
            .put("activeConnectionType", "android_network"))
        .put("remoteLogging", remoteLogUploader.settings())
        .put("tcan485", tcan485Repository.snapshot())
        .put("lastUpdated", nowIso())

    private fun httpHealth(rawUrl: String, expectJson: Boolean): JSONObject {
        val connection = (URL(rawUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 3_000
            readTimeout = 5_000
            requestMethod = "GET"
        }
        return try {
            val code = connection.responseCode
            val body = runCatching { connection.inputStream.bufferedReader().readText() }.getOrDefault("")
            JSONObject()
                .put("url", rawUrl)
                .put("online", code in 200..299)
                .put("statusCode", code)
                .put("body", if (expectJson) body.take(2_000) else body.take(200))
        } finally {
            connection.disconnect()
        }
    }

    private fun localIps(): List<String> = NetworkInterface.getNetworkInterfaces().toList().flatMap { iface ->
        iface.inetAddresses.toList().mapNotNull { addr ->
            if (!addr.isLoopbackAddress && addr is Inet4Address) addr.hostAddress else null
        }
    }

    private fun obdConnectedJson(message: String, lastTx: String, lastRx: String): JSONObject = JSONObject()
        .put("state", "ObdConnected")
        .put("connected", true)
        .put("verified", true)
        .put("polling", obdPolling)
        .put("protocol", "ISO 15765-4 CAN 11/500")
        .put("elmProtocol", "6")
        .put("message", message)
        .put("adapter", "ELM327 v2.3")
        .put("ecu", "responding")
        .put("lastTx", lastTx)
        .put("lastRx", lastRx)
        .put("telemetry", obdRepository.telemetryJson())
        .put("supportedPids", obdRepository.telemetryJson().optJSONArray("supportedPids") ?: JSONArray())
        .put("rawLog", obdRepository.rawLogTail())
        .put("readOnly", true)

    private fun rawLogValue(command: String): String {
        val log = obdRepository.rawLogTail()
        for (index in log.length() - 1 downTo 0) {
            val row = log.optJSONObject(index) ?: continue
            if (row.optString("command") == command && row.optString("direction") == "rx") {
                return row.optString("response")
            }
        }
        return ""
    }

    private fun diagnosticsFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(Date())
        return File(appContext.getExternalFilesDir("diagnostics"), "camper-diagnostics-$stamp.json")
    }

    private fun nowIso(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date())
    }

    private fun isBlockedObdCommand(command: String): Boolean {
        val service = command.take(2)
        return service in setOf("04", "14", "2E", "10", "27", "28", "2F", "31", "3D", "85")
    }

    companion object {
        private val FALLBACK_POLL_ORDER = listOf("0C", "0D", "05", "0F")
        private val FALLBACK_POLL_PIDS = FALLBACK_POLL_ORDER.toSet()
    }

}
