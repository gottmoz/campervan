package se.gottmoz.camperagent.integration

import android.content.Context
import android.os.Build
import android.webkit.JavascriptInterface
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import se.gottmoz.camperagent.integration.canbus.CanDiscoveryRepository
import se.gottmoz.camperagent.integration.logging.RemoteLogUploader
import se.gottmoz.camperagent.integration.obd.FordTransitEcoBlue2016Profile
import se.gottmoz.camperagent.integration.obd.ObdPidDecoder
import se.gottmoz.camperagent.integration.obd.ObdRepository
import se.gottmoz.camperagent.integration.obd.ObdResponseParser
import se.gottmoz.camperagent.integration.usbserial.UsbSerialManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class CamperAgentBridge(context: Context) {
    private val appContext = context.applicationContext
    private val settingsStore = IntegrationSettingsStore(context.applicationContext)
    private val repository = IntegrationRepository(settingsStore)
    private val usbSerialManager = UsbSerialManager(context.applicationContext)
    private val canDiscoveryRepository = CanDiscoveryRepository()
    private val remoteLogUploader = RemoteLogUploader(context.applicationContext)
    private val obdRepository = ObdRepository()
    @Volatile private var obdPolling = false
    @Volatile private var obdConnected = false

    @JavascriptInterface fun getIntegrationSnapshot(): String = ok(repository.snapshot())
    @JavascriptInterface fun getBatteryBmsSnapshot(): String = ok(repository.batteryBmsSnapshot())
    @JavascriptInterface fun getBatteryBmsSettings(): String = ok(settingsStore.getBatteryBmsSettings())
    @JavascriptInterface fun getVictronSettings(): String = ok(settingsStore.getVictronSettings())
    @JavascriptInterface fun getGarminSettings(): String = ok(settingsStore.getGarminSettings())
    @JavascriptInterface fun getObdSettings(): String = ok(settingsStore.getObdSettings())
    @JavascriptInterface fun getRemoteLoggingSettings(): String = ok(remoteLogUploader.settings())

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
        if (obdConnected && obdPolling) {
            return@handle obdConnectedJson("Already connected", "", "")
        }
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
    }
    @JavascriptInterface fun disconnectObd(): String = handle {
        obdPolling = false
        obdConnected = false
        usbSerialManager.close()
        JSONObject().put("state", "Disconnected").put("readOnly", true)
    }
    @JavascriptInterface fun getObdConnectionStatus(): String = ok(usbSerialManager.statusJson()
        .put("protocol", "ISO15765-4 CAN 11/500")
        .put("elmProtocol", "6")
        .put("connected", obdConnected)
        .put("verified", obdConnected)
        .put("polling", obdPolling)
        .put("telemetry", obdRepository.telemetryJson()))
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

    private fun ok(data: JSONObject): String = JSONObject().put("ok", true).put("data", data).toString()
    private fun fail(error: String): String = JSONObject().put("ok", false).put("error", error).toString()

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
        if (adapterIdentity.isBlank()) adapterIdentity = run("AT@1", 2_000).optString("response")
        else run("AT@1", 2_000)
        run("ATZ", 3_000, 1_000)
        listOf("ATE0", "ATL0", "ATS0", "ATH1", "ATAL", "ATST96", "ATSP6", "ATDP", "ATDPN").forEach { run(it, 2_000) }
        detectedProtocol = rawLogValue("ATDP").ifBlank { "ISO 15765-4 (CAN 11/500)" }
        val supported = run("0100", 5_000)
        val parsedSupported = ObdResponseParser.parseMode01(supported.optString("response"))
        if (
            supported.optString("response").contains("NO DATA", ignoreCase = true) ||
            supported.optString("direction") == "timeout" ||
            parsedSupported?.pid != "00"
        ) {
            return JSONObject()
                .put("state", "EcuNoResponse")
                .put("message", "vLinker answers AT commands, but vehicle ECU did not respond on ISO15765-4 CAN11/500.")
                .put("lastTx", lastTx)
                .put("lastRx", lastRx)
                .put("lastError", lastError)
        }
        val supportedPids = ObdPidDecoder.decodeSupportedPids(supported.optString("response"), 0x00)
        obdRepository.updateSupportedPids(supportedPids)
        obdConnected = true
        startObdPolling(supportedPids)
        runBlocking {
            remoteLogUploader.uploadLog(
                "INFO",
                "ObdRepository",
                "state ObdConnected",
                JSONObject()
                    .put("protocol", "ISO15765-4 CAN 11/500")
                    .put("elmProtocol", "6")
                    .put("supportedPids", JSONArray(supportedPids.sorted()))
                    .put("polling", obdPolling)
            )
        }
        return obdConnectedJson("Adapter and ECU verified", lastTx, lastRx)
            .put("adapter", adapterIdentity)
            .put("detectedProtocol", detectedProtocol)
            .put("lastEcuResponse", supported.optString("response"))
            .put("supportedPids", JSONArray(supportedPids.sorted()))
            .put("lastError", lastError)
    }

    private fun sendObdCommand(command: String, timeoutMs: Int, state: String): JSONObject {
        val started = System.currentTimeMillis()
        return try {
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
        } catch (error: Throwable) {
            val elapsed = System.currentTimeMillis() - started
            obdRepository.addLog("error", command = command, response = error.message, state = state, elapsedMs = elapsed)
            runBlocking { remoteLogUploader.uploadLog("ERROR", "ObdRepository", "OBD error", JSONObject().put("direction", "error").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", error.message ?: "unknown")) }
            JSONObject().put("direction", "error").put("command", command).put("elapsedMs", elapsed).put("state", state).put("error", error.message ?: "unknown")
        }
    }

    private fun startObdPolling(supportedPids: Set<String>) {
        if (obdPolling) return
        val pollPids = listOf("0C", "0D", "05", "0F", "10", "11", "42", "46").filter { it in supportedPids }
        if (pollPids.isEmpty()) return
        obdPolling = true
        runBlocking {
            remoteLogUploader.uploadLog("INFO", "ObdRepository", "polling start", JSONObject().put("pids", JSONArray(pollPids)))
        }
        Thread {
            while (obdPolling && usbSerialManager.status.value.open) {
                pollPids.forEach { pid ->
                    val result = sendObdCommand("01$pid", 2_000, "Polling")
                    val response = result.optString("response")
                    if (response.isNotBlank()) {
                        obdRepository.mergeTelemetry(ObdPidDecoder.decodeTelemetry(pid, response))
                    }
                    Thread.sleep(if (pid in setOf("0C", "0D")) 250L else 500L)
                }
                Thread.sleep(500L)
            }
            obdPolling = false
        }.apply {
            name = "CamperObdPolling"
            isDaemon = true
            start()
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
}
