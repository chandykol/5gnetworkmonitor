package com.example.networkmonitor

import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log

class NetworkCallback(
    private val onNetworkChange: (String) -> Unit
) : TelephonyCallback() {
    
    // Use DisplayInfoListener for Android 11+ (API 30+) for better 5G detection
    @androidx.annotation.RequiresApi(Build.VERSION_CODES.R)
    inner class DisplayInfoListenerImpl : TelephonyCallback.DisplayInfoListener {
        override fun onDisplayInfoChanged(telephonyDisplayInfo: TelephonyCallback.TelephonyDisplayInfo) {
            val networkType = telephonyDisplayInfo.networkType
            val overrideNetworkType = telephonyDisplayInfo.overrideNetworkType
            
            val tech = when {
                networkType == TelephonyManager.NETWORK_TYPE_NR || 
                overrideNetworkType == TelephonyCallback.OVERRIDE_NETWORK_TYPE_NR_NSA ||
                overrideNetworkType == TelephonyCallback.OVERRIDE_NETWORK_TYPE_NR_NSA_MMWAVE ||
                overrideNetworkType == TelephonyCallback.OVERRIDE_NETWORK_TYPE_NR_NSA_SUB_6 ||
                overrideNetworkType == TelephonyCallback.OVERRIDE_NETWORK_TYPE_NR_SA -> "5G"
                networkType == TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> {
                    when (networkType) {
                        TelephonyManager.NETWORK_TYPE_NR -> "5G"
                        TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                        else -> "Other"
                    }
                }
            }
            
            Log.d("NetworkCallback", "DisplayInfo - Network type: $networkType, Override: $overrideNetworkType, Tech: $tech")
            onNetworkChange(tech)
        }
    }
    
    // Fallback to DataConnectionStateListener for older Android versions
    inner class DataConnectionListenerImpl : TelephonyCallback.DataConnectionStateListener {
        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            val tech = when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                else -> "Other"
            }
            Log.d("NetworkCallback", "DataConnection - Network type: $networkType, Tech: $tech")
            onNetworkChange(tech)
        }
    }
}
