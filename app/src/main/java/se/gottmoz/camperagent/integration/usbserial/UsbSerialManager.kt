package se.gottmoz.camperagent.integration.usbserial

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class UsbSerialDeviceStatus(
    val deviceName: String = "none",
    val driver: String = "none",
    val permissionGranted: Boolean = false,
    val open: Boolean = false,
    val error: String? = null
)

class UsbSerialManager(private val context: Context) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var port: UsbSerialPort? = null
    private val _status = MutableStateFlow(UsbSerialDeviceStatus())
    val status: StateFlow<UsbSerialDeviceStatus> = _status

    fun enumerate(): List<UsbSerialDeviceStatus> {
        return UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).map { driver ->
            val device = driver.device
            UsbSerialDeviceStatus(
                deviceName = device.deviceName,
                driver = driver.javaClass.simpleName,
                permissionGranted = usbManager.hasPermission(device),
                open = port?.isOpen == true
            )
        }
    }

    fun requestPermission(device: UsbDevice) {
        val intent = PendingIntent.getBroadcast(
            context,
            device.deviceId,
            Intent(ACTION_USB_PERMISSION),
            PendingIntent.FLAG_IMMUTABLE
        )
        usbManager.requestPermission(device, intent)
    }

    fun openFirst(baudRate: Int = 115200) {
        val driver = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager).firstOrNull()
            ?: error("No USB serial driver found")
        val device = driver.device
        if (!usbManager.hasPermission(device)) {
            _status.value = UsbSerialDeviceStatus(device.deviceName, driver.javaClass.simpleName, permissionGranted = false)
            requestPermission(device)
            return
        }
        val connection = usbManager.openDevice(device) ?: error("USB device open failed")
        port = driver.ports.first().also {
            it.open(connection)
            it.setParameters(baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        }
        _status.value = UsbSerialDeviceStatus(device.deviceName, driver.javaClass.simpleName, permissionGranted = true, open = true)
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
            val chunk = readBytes(200).toString(Charsets.US_ASCII)
            out.append(chunk)
            if (chunk.contains(">")) break
        }
        return out.toString()
    }

    companion object {
        const val ACTION_USB_PERMISSION = "se.gottmoz.camperagent.USB_SERIAL_PERMISSION"
    }
}
