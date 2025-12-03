package com.example.networkmonitor

import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager

class NetworkCallback(
    private val onNetworkChange: (String) -> Unit
) : TelephonyCallback(), TelephonyCallback.DataConnectionStateListener {

    override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
        val tech = when (networkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G"
            else -> "Other"
        }
        onNetworkChange(tech)
    }
}
