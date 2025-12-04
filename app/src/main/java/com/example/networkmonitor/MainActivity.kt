package com.example.networkmonitor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.networkmonitor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentType = "5G"
    private val TAG = "MainActivity"
    
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permission denied. App may not work correctly.", Toast.LENGTH_LONG).show()
        }
    }

    private val pickRingtoneLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val data = result.data
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                }
                
                if (uri != null && uri.toString().isNotEmpty()) {
                    val prefs = getSharedPreferences("tones", MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString(currentType, uri.toString())
                    val success = editor.commit() // Use commit() for immediate save
                    
                    // Verify the save worked by reading it back
                    val savedUri = prefs.getString(currentType, null)
                    if (success && savedUri == uri.toString()) {
                        Log.d(TAG, "Sound saved and verified for $currentType: $uri")
                        Toast.makeText(this, "Sound saved for $currentType", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "Failed to save sound for $currentType. Saved: $savedUri, Expected: $uri")
                        Toast.makeText(this, "Failed to save sound. Please try again.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.w(TAG, "No sound selected or URI is null/empty")
                    Toast.makeText(this, "No sound selected", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving sound", e)
                Toast.makeText(this, "Error saving sound: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Ringtone picker cancelled")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Request READ_PHONE_STATE permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
        }

        binding.btnSelect5g.setOnClickListener {
            currentType = "5G"
            openRingtonePicker()
        }

        binding.btnSelect4g.setOnClickListener {
            currentType = "4G"
            openRingtonePicker()
        }

        binding.btnStart.setOnClickListener {
            // Check permission before starting service
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please grant phone permission first", Toast.LENGTH_SHORT).show()
                permissionLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                return@setOnClickListener
            }
            
            val intent = Intent(this, NetworkService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Monitoring started", Toast.LENGTH_SHORT).show()
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
        try {
            // Guide user to ignore battery optimizations for this app
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:$packageName")
            
            // Check if the intent can be resolved
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
                Toast.makeText(this, "Please allow battery optimization exemption", Toast.LENGTH_LONG).show()
            } else {
                // Fallback: Open battery optimization settings
                val batteryIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                if (batteryIntent.resolveActivity(packageManager) != null) {
                    startActivity(batteryIntent)
                    Toast.makeText(this, "Please find and exempt this app from battery optimization", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Please manually disable battery optimization for this app in Settings", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening battery exemption settings", e)
            Toast.makeText(this, "Error: ${e.message}. Please manually disable battery optimization in Settings.", Toast.LENGTH_LONG).show()
        }
    }
}
