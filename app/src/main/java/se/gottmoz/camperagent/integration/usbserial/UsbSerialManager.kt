package se.gottmoz.camperagent.integration.usbserial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.Cp21xxSerialDriver
import com.hoho.android.usbserial.driver.FtdiSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.ProlificSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject

enum class UsbSerialConnectionState {
    NoDevice,
    DeviceFound,
    PermissionRequired,
    PermissionRequested,
    PermissionGranted,
    Opening,
    Open,
    Error
}

data class UsbSerialDeviceStatus(
    val deviceName: String = "none",
    val driver: String = "none",
    val vendorId: Int? = null,
    val productId: Int? = null,
    val permissionGranted: Boolean = false,
    val open: Boolean = false,
    val state: UsbSerialConnectionState = UsbSerialConnectionState.NoDevice,
    val error: String? = null
)

class UsbSerialManager(private val context: Context) {
    private val appContext = context.applicationContext
    private val usbManager = appContext.getSystemService(Context.USB_SERVICE) as UsbManager
    private val prober = UsbSerialProber(customProbeTable())
    private var port: UsbSerialPort? = null
    private var pendingOpenBaudRate: Int = 115200
    private val _status = MutableStateFlow(UsbSerialDeviceStatus())
    val status: StateFlow<UsbSerialDeviceStatus> = _status

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                    if (granted && device != null) {
                        Log.i(TAG, "USB permission granted")
                        _status.value = _status.value.copy(permissionGranted = true, state = UsbSerialConnectionState.PermissionGranted, error = null)
                        runCatching { openFirst(pendingOpenBaudRate) }.onFailure { setError(it.message ?: "USB open failed") }
                    } else {
                        Log.w(TAG, "USB permission denied")
                        setError("USB permission denied")
                    }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> enumerate()
                UsbManager.ACTION_USB_DEVICE_DETACHED -> close()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(receiver, filter)
        }
        enumerate()
    }

    fun enumerate(): List<UsbSerialDeviceStatus> {
        val drivers = drivers()
        if (drivers.isEmpty()) {
            _status.value = UsbSerialDeviceStatus(state = UsbSerialConnectionState.NoDevice)
            return emptyList()
        }
        val statuses = drivers.map { driver ->
            val device = driver.device
            UsbSerialDeviceStatus(
                deviceName = device.deviceName,
                driver = driver.javaClass.simpleName,
                vendorId = device.vendorId,
                productId = device.productId,
                permissionGranted = usbManager.hasPermission(device),
                open = port?.isOpen == true,
                state = if (usbManager.hasPermission(device)) UsbSerialConnectionState.DeviceFound else UsbSerialConnectionState.PermissionRequired
            )
        }
        _status.value = statuses.first()
        return statuses
    }

    fun enumerateJson(): JSONArray = JSONArray(enumerate().map { it.toJson() })

    fun requestPermissionFirst(kind: String = "usb") {
        val driver = drivers().firstOrNull() ?: run {
            _status.value = UsbSerialDeviceStatus(state = UsbSerialConnectionState.NoDevice, error = "No USB serial driver found")
            return
        }
        requestPermission(driver.device, kind)
    }

    fun requestPermission(device: UsbDevice, kind: String = "usb") {
        Log.i(TAG, "Requesting USB permission for vid=${device.vendorId} pid=${device.productId} kind=$kind")
        _status.value = UsbSerialDeviceStatus(
            deviceName = device.deviceName,
            vendorId = device.vendorId,
            productId = device.productId,
            permissionGranted = false,
            state = UsbSerialConnectionState.PermissionRequested
        )
        val intent = PendingIntent.getBroadcast(
            appContext,
            device.deviceId,
            Intent(ACTION_USB_PERMISSION).setPackage(appContext.packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, intent)
    }

    fun openFirst(baudRate: Int = 115200): UsbSerialDeviceStatus {
        pendingOpenBaudRate = baudRate
        val driver = drivers().firstOrNull() ?: error("No USB serial driver found")
        val device = driver.device
        if (!usbManager.hasPermission(device)) {
            _status.value = statusFor(driver, UsbSerialConnectionState.PermissionRequired)
            requestPermission(device, "open")
            return _status.value
        }
        _status.value = statusFor(driver, UsbSerialConnectionState.Opening)
        val connection = usbManager.openDevice(device)
        if (connection == null) {
            requestPermission(device, "open-null")
            return _status.value.copy(state = UsbSerialConnectionState.PermissionRequested)
        }
        port = driver.ports.first().also {
            it.open(connection)
            it.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }
        _status.value = statusFor(driver, UsbSerialConnectionState.Open).copy(permissionGranted = true, open = true)
        return _status.value
    }

    fun close() {
        runCatching { port?.close() }
        port = null
        _status.value = _status.value.copy(open = false, state = UsbSerialConnectionState.DeviceFound)
    }

    fun dispose() {
        close()
        runCatching { appContext.unregisterReceiver(receiver) }
    }

    fun writeBytes(bytes: ByteArray) {
        port?.write(bytes, 1_000) ?: error("USB serial port is not open")
    }

    fun readBytes(timeoutMs: Int = 1_000): ByteArray {
        val buffer = ByteArray(1024)
        val count = port?.read(buffer, timeoutMs) ?: error("USB serial port is not open")
        return buffer.copyOf(count)
    }

    fun readUntilPrompt(timeoutMs: Int = 1_500): String {
        val deadline = System.currentTimeMillis() + timeoutMs
        val out = StringBuilder()
        while (System.currentTimeMillis() < deadline) {
            val chunk = runCatching { readBytes(200).toString(Charsets.US_ASCII) }.getOrDefault("")
            out.append(chunk)
            if (chunk.contains(">")) break
        }
        return out.toString()
    }

    fun statusJson(): JSONObject = _status.value.toJson()

    private fun drivers(): List<UsbSerialDriver> {
        val defaultDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        val customDrivers = prober.findAllDrivers(usbManager)
        return (defaultDrivers + customDrivers).distinctBy { it.device.deviceName }
    }

    private fun statusFor(driver: UsbSerialDriver, state: UsbSerialConnectionState): UsbSerialDeviceStatus {
        val device = driver.device
        return UsbSerialDeviceStatus(
            deviceName = device.deviceName,
            driver = driver.javaClass.simpleName,
            vendorId = device.vendorId,
            productId = device.productId,
            permissionGranted = usbManager.hasPermission(device),
            open = port?.isOpen == true,
            state = state
        )
    }

    private fun setError(error: String) {
        _status.value = _status.value.copy(state = UsbSerialConnectionState.Error, error = error)
    }

    private fun UsbSerialDeviceStatus.toJson(): JSONObject = JSONObject()
        .put("deviceName", deviceName)
        .put("driver", driver)
        .put("vendorId", vendorId ?: JSONObject.NULL)
        .put("productId", productId ?: JSONObject.NULL)
        .put("permissionGranted", permissionGranted)
        .put("open", open)
        .put("state", state.name)
        .put("error", error ?: JSONObject.NULL)

    companion object {
        private const val TAG = "UsbSerialManager"
        private const val ACTION_USB_PERMISSION = "se.gottmoz.camperagent.USB_PERMISSION"

        private fun customProbeTable(): ProbeTable = ProbeTable().apply {
            addProduct(0x0403, 0x6001, FtdiSerialDriver::class.java)
            addProduct(0x1A86, 0x7523, Ch34xSerialDriver::class.java)
            addProduct(0x1A86, 0x5523, Ch34xSerialDriver::class.java)
            addProduct(0x10C4, 0xEA60, Cp21xxSerialDriver::class.java)
            addProduct(0x067B, 0x2303, ProlificSerialDriver::class.java)
            addProduct(0x1209, 0x0001, CdcAcmSerialDriver::class.java)
        }
    }
}
