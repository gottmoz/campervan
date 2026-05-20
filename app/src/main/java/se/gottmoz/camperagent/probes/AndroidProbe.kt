package se.gottmoz.camperagent.probes

import android.os.Build

object AndroidProbe {
    fun collect(): AndroidProbeReport {
        val values = linkedMapOf(
            "manufacturer" to Build.MANUFACTURER,
            "model" to Build.MODEL,
            "brand" to Build.BRAND,
            "device" to Build.DEVICE,
            "product" to Build.PRODUCT,
            "hardware" to Build.HARDWARE,
            "board" to Build.BOARD,
            "bootloader" to Build.BOOTLOADER,
            "fingerprint" to Build.FINGERPRINT,
            "display" to Build.DISPLAY,
            "host" to Build.HOST,
            "id" to Build.ID,
            "type" to Build.TYPE,
            "tags" to Build.TAGS,
            "user" to Build.USER,
            "time" to Build.TIME.toString(),
            "sdkInt" to Build.VERSION.SDK_INT.toString(),
            "release" to Build.VERSION.RELEASE,
            "incremental" to Build.VERSION.INCREMENTAL,
            "supportedAbis" to Build.SUPPORTED_ABIS.joinToString(","),
            "javaVm" to System.getProperty("java.vm.version").orEmpty(),
            "osArch" to System.getProperty("os.arch").orEmpty()
        )
        return AndroidProbeReport(values)
    }
}

data class AndroidProbeReport(
    val values: Map<String, String>
) {
    val summary: String
        get() = "${values["manufacturer"]} ${values["model"]} sdk=${values["sdkInt"]}"
}
