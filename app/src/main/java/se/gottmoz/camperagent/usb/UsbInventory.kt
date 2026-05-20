package se.gottmoz.camperagent.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbManager

class UsbInventory(context: Context) {
    private val usbManager = context.getSystemService(UsbManager::class.java)

    fun collect(): UsbInventoryReport {
        val devices = usbManager.deviceList.values.map { device ->
            val connection = if (usbManager.hasPermission(device)) usbManager.openDevice(device) else null
            try {
                device.toReport(
                    hasPermission = usbManager.hasPermission(device),
                    rawDescriptorLength = connection?.rawDescriptors?.size
                )
            } finally {
                connection?.close()
            }
        }
        return UsbInventoryReport(devices)
    }

    private fun UsbDevice.toReport(hasPermission: Boolean, rawDescriptorLength: Int?): UsbDeviceReport {
        return UsbDeviceReport(
            deviceName = deviceName,
            vendorId = vendorId,
            productId = productId,
            deviceClass = deviceClass,
            deviceSubclass = deviceSubclass,
            deviceProtocol = deviceProtocol,
            manufacturerName = safeString { manufacturerName },
            productName = safeString { productName },
            serialNumber = safeString { serialNumber },
            hasPermission = hasPermission,
            rawDescriptorLength = rawDescriptorLength,
            interfaces = (0 until interfaceCount).map { getInterface(it).toReport() }
        )
    }

    private fun android.hardware.usb.UsbInterface.toReport(): UsbInterfaceReport {
        return UsbInterfaceReport(
            id = id,
            interfaceClass = interfaceClass,
            interfaceSubclass = interfaceSubclass,
            interfaceProtocol = interfaceProtocol,
            endpoints = (0 until endpointCount).map { getEndpoint(it).toReport() }
        )
    }

    private fun UsbEndpoint.toReport(): UsbEndpointReport {
        return UsbEndpointReport(
            endpointNumber = endpointNumber,
            address = address,
            attributes = attributes,
            direction = direction,
            type = type,
            maxPacketSize = maxPacketSize,
            interval = interval
        )
    }

    private fun safeString(read: () -> String?): String? {
        return try {
            read()
        } catch (_: SecurityException) {
            null
        }
    }
}

data class UsbInventoryReport(
    val devices: List<UsbDeviceReport>
)

data class UsbDeviceReport(
    val deviceName: String,
    val vendorId: Int,
    val productId: Int,
    val deviceClass: Int,
    val deviceSubclass: Int,
    val deviceProtocol: Int,
    val manufacturerName: String?,
    val productName: String?,
    val serialNumber: String?,
    val hasPermission: Boolean,
    val rawDescriptorLength: Int?,
    val interfaces: List<UsbInterfaceReport>
)

data class UsbInterfaceReport(
    val id: Int,
    val interfaceClass: Int,
    val interfaceSubclass: Int,
    val interfaceProtocol: Int,
    val endpoints: List<UsbEndpointReport>
)

data class UsbEndpointReport(
    val endpointNumber: Int,
    val address: Int,
    val attributes: Int,
    val direction: Int,
    val type: Int,
    val maxPacketSize: Int,
    val interval: Int
)
