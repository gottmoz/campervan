package se.gottmoz.camperagent.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import se.gottmoz.camperagent.probes.AndroidProbe
import se.gottmoz.camperagent.usb.UsbInventory

class TelemetryService : Service() {
    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        logSnapshot()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logSnapshot()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun logSnapshot() {
        val usbDevices = UsbInventory(this).collect()
        val systemInfo = AndroidProbe.collect()
        Log.i(TAG, "system=${systemInfo.summary} usbDevices=${usbDevices.devices.size}")
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Camper Agent telemetry",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("Camper Agent")
            .setContentText("Read-only telemetry service is running")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
            .apply { flags = flags or Notification.FLAG_ONGOING_EVENT }
    }

    companion object {
        private const val CHANNEL_ID = "telemetry"
        private const val NOTIFICATION_ID = 1001
        private const val TAG = "TelemetryService"

        fun start(context: Context) {
            val intent = Intent(context, TelemetryService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (error: RuntimeException) {
                Log.w(TAG, "Unable to start telemetry service", error)
            }
        }
    }
}
