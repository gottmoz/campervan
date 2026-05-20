package se.gottmoz.camperagent.adapter

import se.gottmoz.camperagent.usb.UsbDeviceReport

enum class AdapterSessionState {
    Disconnected,
    Enumerated,
    PermissionGranted,
    PortOpened,
    IdentityKnown,
    ReadOnlyCapture,
    Error
}

class AdapterSession {
    var state: AdapterSessionState = AdapterSessionState.Disconnected
        private set

    var lastDevice: UsbDeviceReport? = null
        private set

    val policy: CapturePolicy = CapturePolicy.ReadOnly

    fun onEnumerated(device: UsbDeviceReport) {
        lastDevice = device
        state = if (device.hasPermission) {
            AdapterSessionState.PermissionGranted
        } else {
            AdapterSessionState.Enumerated
        }
    }

    fun onPortOpened() {
        check(state == AdapterSessionState.PermissionGranted) { "USB permission is required before opening a port" }
        state = AdapterSessionState.PortOpened
    }

    fun onIdentityKnown() {
        check(state == AdapterSessionState.PortOpened) { "Port must be open before adapter identity is known" }
        state = AdapterSessionState.IdentityKnown
    }

    fun beginReadOnlyCapture() {
        check(state == AdapterSessionState.IdentityKnown) { "Adapter identity must be known before capture" }
        state = AdapterSessionState.ReadOnlyCapture
    }

    fun onError() {
        state = AdapterSessionState.Error
    }

    fun close() {
        state = AdapterSessionState.Disconnected
        lastDevice = null
    }
}

enum class CapturePolicy {
    ReadOnly
}
