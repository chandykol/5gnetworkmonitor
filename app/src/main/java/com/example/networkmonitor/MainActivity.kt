package com.example.networkmonitor

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.networkmonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentType = "5G"

    private val pickRingtoneLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val uri = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                getSharedPreferences("tones", MODE_PRIVATE)
                    .edit()
                    .putString(currentType, uri.toString())
                    .apply()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSelect5g.setOnClickListener {
            currentType = "5G"
            openRingtonePicker()
        }

        binding.btnSelect4g.setOnClickListener {
            currentType = "4G"
            openRingtonePicker()
        }

        binding.btnStart.setOnClickListener {
            startService(Intent(this, NetworkService::class.java))
        }

        binding.btnStop.setOnClickListener {
            stopService(Intent(this, NetworkService::class.java))
        }

        binding.btnBattery.setOnClickListener {
            requestBatteryExemption()
        }
    }

    private fun openRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        pickRingtoneLauncher.launch(intent)
    }

    private fun requestBatteryExemption() {
        // Guide user to ignore battery optimizations for this app
        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }
}
