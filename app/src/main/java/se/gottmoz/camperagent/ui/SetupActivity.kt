package se.gottmoz.camperagent.ui

import android.Manifest
import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import se.gottmoz.camperagent.probes.AndroidProbe
import se.gottmoz.camperagent.service.TelemetryService
import se.gottmoz.camperagent.usb.UsbInventory

class SetupActivity : Activity() {
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()

        output = TextView(this).apply {
            textSize = 14f
            setPadding(24, 16, 24, 24)
        }

        val startButton = Button(this).apply {
            text = "Start telemetry"
            setOnClickListener {
                TelemetryService.start(this@SetupActivity)
                refresh()
            }
        }

        val refreshButton = Button(this).apply {
            text = "Refresh inventory"
            setOnClickListener { refresh() }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(startButton)
            addView(refreshButton)
            addView(output)
        }

        setContentView(
            ScrollView(this).apply { addView(layout) },
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        refresh()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    private fun refresh() {
        val system = AndroidProbe.collect()
        val usb = UsbInventory(this).collect()
        output.text = buildString {
            appendLine("System")
            system.values.forEach { (key, value) -> appendLine("$key: $value") }
            appendLine()
            appendLine("USB devices: ${usb.devices.size}")
            usb.devices.forEach { device ->
                appendLine("${device.deviceName} vid=${device.vendorId} pid=${device.productId} permission=${device.hasPermission}")
                appendLine("  manufacturer=${device.manufacturerName} product=${device.productName} serial=${device.serialNumber}")
                appendLine("  rawDescriptorLength=${device.rawDescriptorLength}")
                device.interfaces.forEach { intf ->
                    appendLine("  interface ${intf.id} class=${intf.interfaceClass} subclass=${intf.interfaceSubclass} endpoints=${intf.endpoints.size}")
                    intf.endpoints.forEach { endpoint ->
                        appendLine("    endpoint ${endpoint.endpointNumber} dir=${endpoint.direction} type=${endpoint.type} maxPacket=${endpoint.maxPacketSize}")
                    }
                }
            }
        }
    }
}
