# Quick Guide: How to Check Logcat for "Service onCreate called"

## Step-by-Step Instructions

### Step 1: Open Logcat in Android Studio
1. **Open Android Studio**
2. **Look at the bottom of the screen** - you should see tabs like "Build", "Run", "Logcat"
3. **Click on the "Logcat" tab**
4. If you don't see it: **View** → **Tool Windows** → **Logcat**

### Step 2: Select Your Device
1. **At the top of Logcat**, there's a dropdown that might say "No Devices" or show device names
2. **Click the dropdown** and select your connected phone/emulator
3. If no device shows: Connect your phone via USB (see DEVICE_CONNECTION_GUIDE.md)

### Step 3: Filter Logs
In the **filter box** (search box) at the top of Logcat, type:

```
NetworkService
```

This will show only logs from the NetworkService.

### Step 4: Clear Old Logs (Optional but Recommended)
1. **Click the trash/bin icon** in Logcat toolbar to clear old logs
2. This makes it easier to see new logs

### Step 5: Start Monitoring in App
1. **On your phone**, open the 5G Monitor app
2. **Tap "Start Monitoring"** button
3. **Watch Logcat** - you should immediately see new log messages

### Step 6: Look for "Service onCreate called"
After tapping "Start Monitoring", you should see in Logcat:

```
NetworkService: Service onCreate called
NetworkService: Foreground notification started
NetworkService: TelephonyCallback registered successfully
NetworkService: Started periodic network type checking (every 10 seconds)
```

## Visual Guide

### Logcat Window Layout:
```
┌─────────────────────────────────────────┐
│ [Device Dropdown ▼] [Filter: NetworkService] [Clear 🗑] │
├─────────────────────────────────────────┤
│ NetworkService: Service onCreate called │ ← Look for this!
│ NetworkService: Foreground notification │
│ NetworkService: TelephonyCallback...    │
│ NetworkService: Started periodic...     │
└─────────────────────────────────────────┘
```

## What Each Log Means

### ✅ "Service onCreate called"
- **Meaning:** Service is starting up
- **When:** Immediately when you tap "Start Monitoring"
- **If missing:** Service might not be starting (check permissions, check if app crashed)

### ✅ "Foreground notification started"
- **Meaning:** Notification is created and shown
- **When:** Right after service starts
- **If missing:** Check notification bar on phone - should see "5G Monitor Running"

### ✅ "TelephonyCallback registered successfully"
- **Meaning:** Network monitoring is set up
- **When:** After service starts
- **If missing:** Permission issue or callback registration failed

### ✅ "Started periodic network type checking (every 10 seconds)"
- **Meaning:** Network checking is active
- **When:** After callback registration
- **If missing:** Periodic check didn't start

## Troubleshooting

### Problem: No Logs Appearing
**Solutions:**
1. **Check device is connected:**
   - Look at device dropdown - should show your phone
   - If "No Devices", connect phone via USB

2. **Check filter:**
   - Make sure filter says `NetworkService` or is empty
   - Try removing filter to see all logs

3. **Check app is running:**
   - Make sure you tapped "Start Monitoring"
   - Check notification bar for "5G Monitor Running"

4. **Restart Logcat:**
   - Click the refresh icon in Logcat
   - Or close and reopen Logcat tab

### Problem: See Logs but Not "Service onCreate called"
**Possible causes:**
1. **Service crashed immediately** - Look for error messages (red text)
2. **Permission denied** - Look for "Permission denied" messages
3. **Service didn't start** - Check if notification appears on phone

### Problem: Logs Show Errors
**Look for red error messages:**
```
NetworkService: Error registering TelephonyCallback
NetworkService: Permission denied for TelephonyCallback
NetworkService: READ_PHONE_STATE permission not granted
```

**Solutions:**
- Grant phone permission in app
- Check if service stopped immediately

## Quick Filter Commands

### Show Only NetworkService Logs:
```
NetworkService
```

### Show Only Errors:
```
NetworkService level:error
```

### Show Service Startup:
```
NetworkService Service onCreate
```

### Show All App Logs:
```
package:mine
```

### Show Network Changes:
```
NetworkService Periodic check detected
```

## Real-Time Monitoring

To watch logs in real-time:

1. **Clear Logcat** (trash icon)
2. **Set filter:** `NetworkService`
3. **Start monitoring** in app
4. **Watch logs appear** in real-time
5. **Look for:** "Service onCreate called" should appear immediately

## Expected Log Sequence

When you tap "Start Monitoring", you should see this sequence:

```
1. NetworkService: Service onCreate called
2. NetworkService: Foreground notification started
3. NetworkService: TelephonyCallback registered successfully
4. NetworkService: Initial network type: 4G (networkType=13)
5. NetworkService: Started periodic network type checking (every 10 seconds)
```

Then every 10 seconds:
```
6. NetworkService: Initial network type: 4G (networkType=13)
```

When network changes:
```
7. NetworkService: Periodic check detected network change: 4G -> 5G
8. NetworkService: playToneForType called for: 5G
9. NetworkService: Playing ringtone using RingtoneManager for 5G
```

## Color Coding in Logcat

- **Red:** Errors (look for these if something's wrong)
- **Orange:** Warnings
- **Blue:** Debug/Info messages (most of our logs)
- **Green:** Usually system messages

## Exporting Logs

To save logs for later:

1. **Right-click in Logcat**
2. **Select "Save Logcat to File"**
3. **Choose location and save**
4. Share the file if you need help debugging

## Quick Checklist

- [ ] Logcat tab is open
- [ ] Device is selected in dropdown
- [ ] Filter is set to `NetworkService` (or empty)
- [ ] Old logs cleared (optional)
- [ ] "Start Monitoring" tapped in app
- [ ] "Service onCreate called" appears in Logcat
- [ ] Other service logs appear
- [ ] No red error messages

## Still Not Seeing Logs?

1. **Check device connection:** `adb devices` in Terminal
2. **Check app is installed:** Look for app icon on phone
3. **Try restarting:** Stop and start monitoring again
4. **Check Android Studio:** Make sure it's not paused or stopped
5. **Try different filter:** Remove filter to see all logs

