package com.example.networkmonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
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

        try {
            telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            callback = NetworkCallback { type -> onNetworkTypeChanged(type) }
            telephonyManager.registerTelephonyCallback(mainExecutor, callback)
            Log.d(TAG, "TelephonyCallback registered successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for TelephonyCallback", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error registering TelephonyCallback", e)
            stopSelf()
        }
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
            // Release previous MediaPlayer
            mp?.release()
            mp = null
            
            val prefs = getSharedPreferences("tones", MODE_PRIVATE)
            val uriString = prefs.getString(type, null)
            var uri: Uri? = null
            
            // Try to parse the saved URI
            if (!uriString.isNullOrEmpty()) {
                try {
                    uri = Uri.parse(uriString)
                    Log.d(TAG, "Using saved sound for $type: $uriString")
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing URI for $type: $uriString", e)
                    uri = null
                }
            }
            
            // If no valid URI, use default notification sound
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                Log.d(TAG, "Using default notification sound for $type")
            }
            
            // Create and play MediaPlayer
            mp = MediaPlayer.create(this, uri)
            if (mp != null) {
                mp?.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    mp?.release()
                    mp = null
                    // Try default sound if custom sound fails
                    if (uriString != null && uri != RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) {
                        Log.d(TAG, "Retrying with default sound")
                        try {
                            val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                            mp = MediaPlayer.create(this, defaultUri)
                            mp?.start()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to play default sound", e)
                        }
                    }
                    true
                }
                mp?.setOnCompletionListener {
                    it.release()
                    mp = null
                }
                mp?.start()
                Log.d(TAG, "Playing sound for $type")
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for $type, trying default sound")
                // Fallback to default sound
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mp = MediaPlayer.create(this, defaultUri)
                mp?.start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tone for $type", e)
            mp?.release()
            mp = null
            // Last resort: try default sound
            try {
                val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                mp = MediaPlayer.create(this, defaultUri)
                mp?.start()
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to play default sound as fallback", ex)
            }
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
