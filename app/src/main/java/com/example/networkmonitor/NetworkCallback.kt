package com.example.networkmonitor

import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log

class NetworkCallback(
    private val onNetworkChange: (String) -> Unit
) : TelephonyCallback() {
    
    // Use DataConnectionStateListener for all Android versions
    // This works reliably across all API levels and detects 5G via NETWORK_TYPE_NR
    inner class DataConnectionListenerImpl : TelephonyCallback.DataConnectionStateListener {
        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            // Log all network types for debugging
            val networkTypeName = when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "NR (5G)"
                TelephonyManager.NETWORK_TYPE_LTE -> "LTE (4G)"
                TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS (3G)"
                TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE (2G)"
                TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS (2G)"
                TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN"
                else -> "OTHER ($networkType)"
            }
            
            val tech = when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> "Other"
            }
            
            val stateName = when (state) {
                TelephonyManager.DATA_CONNECTED -> "CONNECTED"
                TelephonyManager.DATA_CONNECTING -> "CONNECTING"
                TelephonyManager.DATA_DISCONNECTED -> "DISCONNECTED"
                TelephonyManager.DATA_SUSPENDED -> "SUSPENDED"
                else -> "UNKNOWN ($state)"
            }
            
            Log.d("NetworkCallback", "Network changed - Type: $networkTypeName, Tech: $tech, State: $stateName")
            onNetworkChange(tech)
        }
    }
}
