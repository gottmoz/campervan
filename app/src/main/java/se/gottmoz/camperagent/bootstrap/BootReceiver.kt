package se.gottmoz.camperagent.bootstrap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import se.gottmoz.camperagent.service.TelemetryService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> TelemetryService.start(context)
        }
    }
}
