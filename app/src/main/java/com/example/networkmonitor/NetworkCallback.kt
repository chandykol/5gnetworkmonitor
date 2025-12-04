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
            val tech = when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> "Other"
            }
            Log.d("NetworkCallback", "Network changed - Type: $networkType, Tech: $tech, State: $state")
            onNetworkChange(tech)
        }
    }
}
