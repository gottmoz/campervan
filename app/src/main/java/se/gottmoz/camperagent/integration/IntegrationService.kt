package se.gottmoz.camperagent.integration

import android.app.Service
import android.content.Intent
import android.os.IBinder

class IntegrationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
