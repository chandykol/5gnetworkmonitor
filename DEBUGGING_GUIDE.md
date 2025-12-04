# Debugging Guide - Network Change Detection & Sound Playback

## How to Check Logs in Android Studio

### Step 1: Open Logcat
1. **Open Android Studio**
2. **Connect your phone** via USB (with USB debugging enabled)
3. **Click on "Logcat"** tab at the bottom of Android Studio
4. If you don't see Logcat, go to **View → Tool Windows → Logcat**

### Step 2: Filter Logs

#### Filter by Tag (Recommended)
In the Logcat filter box, enter one of these:

**For Network Detection:**
```
NetworkService
```

**For Network Callback:**
```
NetworkCallback
```

**For Sound Playback:**
```
NetworkService
```
(Then look for messages containing "playToneForType" or "Playing")

**For All App Logs:**
```
package:mine
```

#### Filter by Process
1. Click the dropdown next to the filter box
2. Select your app package: `com.example.networkmonitor`

### Step 3: What to Look For

#### ✅ Good Signs (Everything Working):
```
NetworkService: Started periodic network type checking (every 10 seconds)
NetworkService: Periodic check detected network change: 4G -> 5G
NetworkService: playToneForType called for: 5G
NetworkService: Loaded saved URI for 5G: content://...
NetworkService: Playing ringtone using RingtoneManager for 5G
NetworkService: Sound playback started for 5G using MediaPlayer
```

#### ❌ Problem Signs:

**1. Network Not Detected:**
```
NetworkService: Started periodic network type checking (every 10 seconds)
(But no "Periodic check detected network change" messages)
```
**Solution:** Network might not be changing, or check is not running

**2. Sound Not Playing:**
```
NetworkService: playToneForType called for: 5G
NetworkService: Loaded saved URI for 5G: null
NetworkService: Using default notification sound for 5G
(But no "Playing ringtone" or "Sound playback started" messages)
```
**Solution:** Sound URI might be invalid or MediaPlayer/RingtoneManager failing

**3. Service Not Running:**
```
(No logs at all)
```
**Solution:** Service might have crashed or stopped

**4. Permission Issues:**
```
NetworkService: Permission denied for TelephonyCallback
NetworkService: READ_PHONE_STATE permission not granted
```
**Solution:** Grant phone permission in app settings

## Step-by-Step Debugging Process

### 1. Check if Service is Running
Look for:
```
NetworkService: Service onCreate called
NetworkService: Foreground notification started
NetworkService: TelephonyCallback registered successfully
NetworkService: Started periodic network type checking (every 10 seconds)
```

**If missing:** Service might not be starting. Check:
- Did you tap "Start Monitoring"?
- Is permission granted?
- Check notification bar for "5G Monitor Running"

### 2. Check if Network Changes are Detected
Look for:
```
NetworkService: Periodic check detected network change: 4G -> 5G
```
or
```
NetworkCallback: Network changed - Type: NR (5G), Tech: 5G, State: CONNECTED
```

**If missing:** 
- Network might not be changing (stay in one area and wait)
- Try switching airplane mode on/off to force network change
- Check your phone's network settings to see current network type

### 3. Check if Sound Function is Called
Look for:
```
NetworkService: playToneForType called for: 5G
```

**If missing:** Network change detection is not triggering sound playback

### 4. Check if Sound URI is Loaded
Look for:
```
NetworkService: Loaded saved URI for 5G: content://settings/system/notification_sound
```
or
```
NetworkService: Loaded saved URI for 5G: null
NetworkService: Using default notification sound for 5G
```

**If null:** Sound was not saved properly. Try selecting sound again.

### 5. Check if Sound is Playing
Look for:
```
NetworkService: Playing ringtone using RingtoneManager for 5G
```
or
```
NetworkService: Sound playback started for 5G using MediaPlayer
```

**If missing but playToneForType was called:** Sound playback is failing silently

### 6. Check for Errors
Look for red error messages:
```
NetworkService: Error playing tone for 5G
NetworkService: MediaPlayer error: what=...
NetworkService: RingtoneManager returned null
```

## Quick Debug Commands

### Filter for Network Changes Only:
```
tag:NetworkService | tag:NetworkCallback
```

### Filter for Sound Playback Only:
```
tag:NetworkService playToneForType
```

### Filter for Errors Only:
```
tag:NetworkService level:error
```

### Filter for All App Activity:
```
package:com.example.networkmonitor
```

## Common Issues & Solutions

### Issue 1: No Logs at All
**Solution:**
- Make sure phone is connected via USB
- Enable USB debugging on phone
- Select your device in Logcat dropdown
- Restart the app

### Issue 2: Service Starts but No Network Detection
**Solution:**
- Check if you're in an area with 5G coverage
- Try toggling airplane mode to force network change
- Check phone's network settings to verify current network type
- Wait at least 10 seconds (check interval)

### Issue 3: Network Detected but No Sound
**Check:**
- Are sounds saved? (Tap logo in app to check)
- Check logs for "Loaded saved URI" - is it null?
- Check for error messages about MediaPlayer or RingtoneManager
- Try selecting sounds again

### Issue 4: Sound URI Saved but Not Playing
**Check logs for:**
- "RingtoneManager returned null" - URI might be invalid
- "MediaPlayer error" - Sound file might not be accessible
- "Error using RingtoneManager" - Permission issue

## Testing Network Changes Manually

Since network changes are hard to test, you can:

1. **Use Airplane Mode:**
   - Turn on airplane mode
   - Turn off airplane mode
   - This forces network reconnection and might trigger a change

2. **Move Between Areas:**
   - Move from 4G-only area to 5G area (or vice versa)
   - This should trigger network type change

3. **Check Phone Settings:**
   - Go to Settings → About Phone → Status
   - Check "Network type" to see current network
   - This helps verify if network is actually changing

## Exporting Logs

To save logs for analysis:

1. **Right-click in Logcat**
2. **Select "Save Logcat to File"**
3. **Choose location and save**
4. Share the log file if you need help debugging

## Real-Time Monitoring

To monitor logs in real-time while testing:

1. **Clear Logcat** (trash icon)
2. **Start monitoring** in the app
3. **Watch Logcat** for new messages
4. **Try to trigger network change** (move, toggle airplane mode)
5. **Observe** what happens in logs

## Expected Log Flow (When Working)

```
1. NetworkService: Service onCreate called
2. NetworkService: Foreground notification started
3. NetworkService: TelephonyCallback registered successfully
4. NetworkService: Started periodic network type checking (every 10 seconds)
5. NetworkService: Initial network type: 4G (networkType=13)
6. [Every 10 seconds] NetworkService: Periodic check...
7. NetworkService: Periodic check detected network change: 4G -> 5G
8. NetworkService: playToneForType called for: 5G
9. NetworkService: Loaded saved URI for 5G: content://...
10. NetworkService: Playing ringtone using RingtoneManager for 5G
```

If any step is missing, that's where the problem is!

