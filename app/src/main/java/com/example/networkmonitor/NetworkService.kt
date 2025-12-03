package com.example.networkmonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log

class NetworkService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private lateinit var callback: NetworkCallback
    private var lastType: String? = null
    private var mp: MediaPlayer? = null
    private val TAG = "NetworkService"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification = Notification.Builder(this, "monitor")
            .setContentTitle("5G Monitor Running")
            .setContentText("Monitoring network status…")
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .build()
        startForeground(1, notification)

        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        callback = NetworkCallback { type -> onNetworkTypeChanged(type) }
        telephonyManager.registerTelephonyCallback(mainExecutor, callback)
    }

    private fun onNetworkTypeChanged(type: String) {
        Log.d(TAG, "Network changed: $type (last=$lastType)")
        if (type == lastType) return

        if (type == "5G") {
            playToneForType("5G")
        } else if (type == "4G" && lastType == "5G") {
            playToneForType("4G")
        }
        lastType = type
    }

    private fun playToneForType(type: String) {
        try {
            mp?.release()
            val prefs = getSharedPreferences("tones", MODE_PRIVATE)
            val uriString = prefs.getString(type, null)
            val uri = if (uriString != null) Uri.parse(uriString) else null

            if (uri != null) {
                mp = MediaPlayer.create(this, uri)
                mp?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tone", e)
        }
    }

    private fun createNotificationChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("monitor", "Network Monitor", NotificationManager.IMPORTANCE_LOW)
        nm.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            telephonyManager.unregisterTelephonyCallback(callback)
        } catch (ignored: Exception) {}
        mp?.release()
    }
}
