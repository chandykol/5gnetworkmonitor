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
                Log.d(TAG, "Ringtone picker result data: $data")
                
                // Try multiple ways to get the URI (different Android versions handle this differently)
                var uri: Uri? = null
                
                if (data != null) {
                    // Method 1: Standard way - get from EXTRA_RINGTONE_PICKED_URI
                    uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        data.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    }
                    
                    Log.d(TAG, "Method 1 - Got URI from EXTRA_RINGTONE_PICKED_URI: $uri")
                    
                    // Method 2: Try getting from data URI directly
                    if (uri == null && data.data != null) {
                        uri = data.data
                        Log.d(TAG, "Method 2 - Got URI from data.data: $uri")
                    }
                    
                    // Method 3: Try getting all extras to see what's available
                    if (uri == null) {
                        val extras = data.extras
                        if (extras != null) {
                            Log.d(TAG, "Method 3 - Checking all extras: ${extras.keySet()}")
                            for (key in extras.keySet()) {
                                val value = extras.get(key)
                                Log.d(TAG, "Extra key: $key, value: $value, type: ${value?.javaClass?.name}")
                                if (value is Uri) {
                                    uri = value
                                    Log.d(TAG, "Found URI in extras with key: $key")
                                    break
                                }
                            }
                        }
                    }
                }
                
                Log.d(TAG, "Selected URI for $currentType: $uri")
                
                if (uri != null && uri.toString().isNotEmpty() && uri.toString() != "null") {
                    val uriString = uri.toString()
                    val prefs = getSharedPreferences("tones", MODE_PRIVATE)
                    
                    // Save the URI
                    val editor = prefs.edit()
                    editor.putString(currentType, uriString)
                    val success = editor.commit() // Use commit() for immediate save
                    
                    Log.d(TAG, "Save attempt - Success: $success, URI: $uriString")
                    
                    // Verify the save worked by reading it back immediately
                    val savedUri = prefs.getString(currentType, null)
                    Log.d(TAG, "Verification - Saved URI: $savedUri, Expected: $uriString")
                    
                    if (success && savedUri == uriString) {
                        Log.d(TAG, "Sound saved and verified for $currentType: $uriString")
                        Toast.makeText(this, "Sound saved for $currentType", Toast.LENGTH_SHORT).show()
                        displaySavedRingtones()
                    } else {
                        Log.e(TAG, "Failed to save sound for $currentType. Success: $success, Saved: $savedUri, Expected: $uriString")
                        Toast.makeText(this, "Failed to save sound. Saved: ${savedUri?.take(50)}...", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Log.w(TAG, "No sound selected or URI is null/empty. URI: $uri")
                    Toast.makeText(this, "No sound selected. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving sound", e)
                e.printStackTrace()
                Toast.makeText(this, "Error saving sound: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG, "Ringtone picker cancelled or failed. Result code: ${result.resultCode}")
        }
    }
    
    private fun displaySavedRingtones() {
        val prefs = getSharedPreferences("tones", MODE_PRIVATE)
        val sound5G = prefs.getString("5G", null)
        val sound4G = prefs.getString("4G", null)
        
        Log.d(TAG, "Current saved sounds - 5G: $sound5G, 4G: $sound4G")
        
        // Update button text to show if sound is saved
        binding.btnSelect5g.text = if (sound5G != null) {
            "Select 5G Alert Sound (✓ Saved)"
        } else {
            "Select 5G Alert Sound"
        }
        
        binding.btnSelect4g.text = if (sound4G != null) {
            "Select 4G Alert Sound (✓ Saved)"
        } else {
            "Select 4G Alert Sound"
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

        // Display currently saved ringtones
        displaySavedRingtones()

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
            Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show()
        }

        binding.btnBattery.setOnClickListener {
            requestBatteryExemption()
        }
        
        // Test: Click on logo to see saved sounds
        binding.imgAppLogo.setOnClickListener {
            val prefs = getSharedPreferences("tones", MODE_PRIVATE)
            val sound5G = prefs.getString("5G", null)
            val sound4G = prefs.getString("4G", null)
            val message = "5G: ${if (sound5G != null) "Saved ✓" else "Not saved"}\n4G: ${if (sound4G != null) "Saved ✓" else "Not saved"}"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            Log.d(TAG, "Current saved sounds - 5G: $sound5G, 4G: $sound4G")
        }
    }

    private fun openRingtonePicker() {
        try {
            // Get currently saved ringtone to pre-select it
            val prefs = getSharedPreferences("tones", MODE_PRIVATE)
            val savedUriString = prefs.getString(currentType, null)
            val savedUri = if (savedUriString != null) Uri.parse(savedUriString) else null
            
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            if (savedUri != null) {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, savedUri)
            }
            pickRingtoneLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening ringtone picker", e)
            Toast.makeText(this, "Error opening ringtone picker: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
