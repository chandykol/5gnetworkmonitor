package com.example.networkmonitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

class NetworkService : Service() {

    private var telephonyManager: TelephonyManager? = null
    private var callback: NetworkCallback? = null
    private var registeredListener: TelephonyCallback.DataConnectionStateListener? = null
    private var lastType: String? = null
    private var mp: MediaPlayer? = null
    private val TAG = "NetworkService"
    private var isCallbackRegistered = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate called")
        
        try {
            createNotificationChannel()
            val notification = Notification.Builder(this, "monitor")
                .setContentTitle("5G Monitor Running")
                .setContentText("Monitoring network status…")
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .build()
            startForeground(1, notification)
            Log.d(TAG, "Foreground notification started")

            // Check permission before registering callback
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "READ_PHONE_STATE permission not granted")
                stopSelf()
                return
            }

            telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            
            // Get initial network type
            try {
                val currentNetworkType = telephonyManager?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
                lastType = when (currentNetworkType) {
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                    else -> "Other"
                }
                Log.d(TAG, "Initial network type: $lastType (networkType=$currentNetworkType)")
            } catch (e: Exception) {
                Log.w(TAG, "Could not get initial network type", e)
            }
            
            callback = NetworkCallback { type -> onNetworkTypeChanged(type) }
            
            // Register DataConnectionStateListener - works on all API levels
            val dataListener: TelephonyCallback.DataConnectionStateListener = callback!!.DataConnectionListenerImpl()
            registeredListener = dataListener
            telephonyManager?.registerTelephonyCallback(mainExecutor, dataListener)
            
            isCallbackRegistered = true
            Log.d(TAG, "TelephonyCallback registered successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied for TelephonyCallback", e)
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error registering TelephonyCallback", e)
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun onNetworkTypeChanged(type: String) {
        Log.d(TAG, "Network changed: $type (last=$lastType)")
        
        // Only process 4G and 5G changes, ignore "Other"
        if (type != "4G" && type != "5G") {
            Log.d(TAG, "Ignoring network type: $type")
            return
        }
        
        // Play sound when switching between 4G and 5G
        if (type != lastType && (type == "5G" || type == "4G")) {
            playToneForType(type)
            lastType = type
        }
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand called")
        // Return START_STICKY to ensure service restarts if killed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy called")
        try {
            if (isCallbackRegistered && registeredListener != null && telephonyManager != null) {
                telephonyManager?.unregisterTelephonyCallback(registeredListener!!)
                isCallbackRegistered = false
                registeredListener = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callback", e)
        }
        mp?.release()
        mp = null
    }
}
