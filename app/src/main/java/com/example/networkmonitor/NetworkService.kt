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
    private var registeredListener: TelephonyCallback? = null
    private var lastType: String? = null
    private var mp: MediaPlayer? = null
    private val TAG = "NetworkService"
    private var isCallbackRegistered = false
    private var networkCheckHandler: android.os.Handler? = null
    private var networkCheckRunnable: Runnable? = null

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
            val dataListener = callback!!.DataConnectionListenerImpl()
            registeredListener = dataListener as TelephonyCallback
            telephonyManager?.registerTelephonyCallback(mainExecutor, dataListener)
            
            isCallbackRegistered = true
            Log.d(TAG, "TelephonyCallback registered successfully")
            
            // Start periodic network type checking (DataConnectionStateListener doesn't fire on type changes)
            startPeriodicNetworkCheck()
            
            // Log initial network state for debugging
            try {
                val currentNetworkType = telephonyManager?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
                Log.d(TAG, "Initial network type: $currentNetworkType")
            } catch (e: Exception) {
                Log.w(TAG, "Could not get initial network type", e)
            }
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
            Log.d(TAG, "Network change detected: $lastType -> $type, playing sound...")
            playToneForType(type)
            lastType = type
            
            // Update notification to show current network
            try {
                val notification = Notification.Builder(this, "monitor")
                    .setContentTitle("5G Monitor Running")
                    .setContentText("Current Network: $type")
                    .setSmallIcon(android.R.drawable.ic_popup_sync)
                    .build()
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(1, notification)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating notification", e)
            }
        } else {
            Log.d(TAG, "No change detected (type=$type, lastType=$lastType)")
        }
    }

    private fun playToneForType(type: String) {
        Log.d(TAG, "playToneForType called for: $type")
        
        // Release previous MediaPlayer first
        try {
            mp?.stop()
            mp?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing previous MediaPlayer", e)
        }
        mp = null
        
        try {
            val prefs = getSharedPreferences("tones", MODE_PRIVATE)
            val uriString = prefs.getString(type, null)
            Log.d(TAG, "Loaded saved URI for $type: $uriString")
            
            var uri: Uri? = null
            
            // Try to parse the saved URI
            if (!uriString.isNullOrEmpty()) {
                try {
                    uri = Uri.parse(uriString)
                    Log.d(TAG, "Parsed URI for $type: $uri")
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing URI for $type: $uriString", e)
                    uri = null
                }
            } else {
                Log.d(TAG, "No saved URI for $type")
            }
            
            // If no valid URI, use default notification sound
            if (uri == null) {
                uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                Log.d(TAG, "Using default notification sound for $type: $uri")
            }
            
            // Try using RingtoneManager first (better for ringtones)
            try {
                val ringtone = RingtoneManager.getRingtone(this, uri)
                if (ringtone != null) {
                    Log.d(TAG, "Playing ringtone using RingtoneManager for $type")
                    ringtone.play()
                    // Stop after 3 seconds
                    android.os.Handler(mainLooper).postDelayed({
                        try {
                            ringtone.stop()
                        } catch (e: Exception) {
                            Log.w(TAG, "Error stopping ringtone", e)
                        }
                    }, 3000)
                    return
                } else {
                    Log.w(TAG, "RingtoneManager returned null, trying MediaPlayer")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error using RingtoneManager, trying MediaPlayer", e)
            }
            
            // Fallback to MediaPlayer
            Log.d(TAG, "Creating MediaPlayer with URI: $uri")
            mp = MediaPlayer.create(this, uri)
            
            if (mp != null) {
                Log.d(TAG, "MediaPlayer created successfully")
                mp?.setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra, uri=$uri")
                    mp?.release()
                    mp = null
                    true
                }
                mp?.setOnCompletionListener {
                    Log.d(TAG, "Sound playback completed")
                    it.release()
                    mp = null
                }
                
                mp?.setAudioStreamType(android.media.AudioManager.STREAM_NOTIFICATION)
                mp?.start()
                Log.d(TAG, "Sound playback started for $type using MediaPlayer")
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for $type with URI: $uri")
                // Last resort: try default sound with RingtoneManager
                try {
                    val defaultUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val defaultRingtone = RingtoneManager.getRingtone(this, defaultUri)
                    if (defaultRingtone != null) {
                        Log.d(TAG, "Playing default ringtone using RingtoneManager")
                        defaultRingtone.play()
                        android.os.Handler(mainLooper).postDelayed({
                            try {
                                defaultRingtone.stop()
                            } catch (e: Exception) {
                                Log.w(TAG, "Error stopping default ringtone", e)
                            }
                        }, 3000)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Exception playing default sound", e)
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tone for $type", e)
            e.printStackTrace()
            mp?.release()
            mp = null
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

    private fun startPeriodicNetworkCheck() {
        networkCheckHandler = android.os.Handler(mainLooper)
        networkCheckRunnable = object : Runnable {
            override fun run() {
                try {
                    val currentNetworkType = telephonyManager?.dataNetworkType ?: TelephonyManager.NETWORK_TYPE_UNKNOWN
                    val currentType = when (currentNetworkType) {
                        TelephonyManager.NETWORK_TYPE_NR -> "5G"
                        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                        else -> "Other"
                    }
                    
                    // Only check for 4G and 5G, ignore other types
                    if (currentType == "4G" || currentType == "5G") {
                        // Check if network type changed
                        if (currentType != lastType) {
                            Log.d(TAG, "Periodic check detected network change: $lastType -> $currentType")
                            // Update lastType before calling to prevent duplicate triggers
                            val previousType = lastType
                            lastType = currentType
                            playToneForType(currentType)
                            
                            // Update notification
                            try {
                                val notification = Notification.Builder(this@NetworkService, "monitor")
                                    .setContentTitle("5G Monitor Running")
                                    .setContentText("Current Network: $currentType")
                                    .setSmallIcon(android.R.drawable.ic_popup_sync)
                                    .build()
                                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                nm.notify(1, notification)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating notification", e)
                            }
                        }
                    }
                    
                    // Schedule next check (every 10 seconds)
                    networkCheckHandler?.postDelayed(this, 10000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in periodic network check", e)
                    // Retry after error
                    networkCheckHandler?.postDelayed(this, 10000)
                }
            }
        }
        networkCheckHandler?.post(networkCheckRunnable!!)
        Log.d(TAG, "Started periodic network type checking (every 10 seconds)")
    }
    
    private fun stopPeriodicNetworkCheck() {
        networkCheckRunnable?.let {
            networkCheckHandler?.removeCallbacks(it)
        }
        networkCheckRunnable = null
        networkCheckHandler = null
        Log.d(TAG, "Stopped periodic network type checking")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy called")
        stopPeriodicNetworkCheck()
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
