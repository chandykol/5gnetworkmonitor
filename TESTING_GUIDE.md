# Testing Guide - 5G Network Monitor App

## Quick Test Steps

### Step 1: Verify App is Running
1. **Open the app** on your phone
2. **Tap "Start Monitoring"**
3. **Check notification bar** - you should see "5G Monitor Running"
4. **Check Logcat** - should show "Service onCreate called"

### Step 2: Verify Ringtones are Saved
1. **Tap the logo/image** at the top of the app
2. **Toast message** should show: "5G: Saved ✓" and "4G: Saved ✓"
3. **Button text** should show "(✓ Saved)" next to each button

### Step 3: Test Sound Playback
1. **Tap "Start Monitoring"** button
2. **You should hear a test sound** (plays when monitoring starts)
3. This confirms sounds are working

### Step 4: Test Network Change Detection

## Methods to Test Network Changes

### Method 1: Airplane Mode (Easiest)
1. **Start monitoring** in the app
2. **Turn on Airplane Mode** (swipe down → tap airplane icon)
3. **Wait 5-10 seconds**
4. **Turn off Airplane Mode**
5. **Watch Logcat** - should show network change detection
6. **Listen for sound** - should play when network reconnects

### Method 2: Move Between Network Areas
1. **Start monitoring** in the app
2. **Move to an area with 5G coverage** (if available)
3. **Move to an area with only 4G coverage**
4. **Watch Logcat** for network change messages
5. **Listen for sound** when network type changes

### Method 3: Force Network Type (Some Phones)
1. **Settings** → **Mobile Networks** → **Preferred Network Type**
2. **Switch between "5G/LTE" and "LTE only"**
3. **Watch Logcat** for changes
4. **Listen for sound**

### Method 4: Manual Network Type Check
1. **Settings** → **About Phone** → **Status** → **Network Type**
2. **Note current network type** (4G or 5G)
3. **Try to force change** using Method 3
4. **Check if network type actually changed**
5. **App should detect and play sound**

## What to Check in Logcat

### Open Logcat
1. **View** → **Tool Windows** → **Logcat**
2. **Filter by:** `NetworkService`

### Expected Logs When Working:

**1. Service Started:**
```
NetworkService: Service onCreate called
NetworkService: Foreground notification started
NetworkService: TelephonyCallback registered successfully
NetworkService: Started periodic network type checking (every 10 seconds)
```

**2. Network Check Running (every 10 seconds):**
```
NetworkService: Initial network type: 4G (networkType=13)
```

**3. Network Change Detected:**
```
NetworkService: Periodic check detected network change: 4G -> 5G
NetworkService: playToneForType called for: 5G
NetworkService: Loaded saved URI for 5G: content://...
NetworkService: Playing ringtone using RingtoneManager for 5G
```

**4. Sound Playing:**
```
NetworkService: Sound playback started for 5G using MediaPlayer
```

### Problem Logs to Watch For:

**No Network Detection:**
- Missing "Periodic check detected network change"
- **Solution:** Network might not be changing, try airplane mode

**Sound Not Playing:**
- Shows "playToneForType called" but no "Playing ringtone"
- **Solution:** Check if URI is null or invalid

**URI is Null:**
```
NetworkService: Loaded saved URI for 5G: null
NetworkService: Using default notification sound for 5G
```
- **Solution:** Ringtones not saved properly, select them again

## Testing Checklist

### Before Testing:
- [ ] App is installed and running
- [ ] "Start Monitoring" button was tapped
- [ ] Notification shows "5G Monitor Running"
- [ ] Ringtones are saved (buttons show "✓ Saved")
- [ ] Test sound played when starting monitoring

### During Testing:
- [ ] Logcat is open and filtered by `NetworkService`
- [ ] Airplane mode toggled (or network area changed)
- [ ] Logcat shows "Periodic check detected network change"
- [ ] Logcat shows "playToneForType called"
- [ ] Sound actually plays on phone

### After Testing:
- [ ] Notification updated to show current network type
- [ ] Sound played for the correct network type (4G or 5G)
- [ ] No errors in Logcat

## Quick Test Scenarios

### Scenario 1: Test Sound Playback (No Network Change Needed)
1. **Start monitoring**
2. **You should hear test sound immediately**
3. **If you hear it:** Sounds are working ✅
4. **If no sound:** Check Logcat for errors

### Scenario 2: Test Network Detection
1. **Start monitoring**
2. **Check current network type:**
   - Settings → About Phone → Status → Network Type
3. **Toggle airplane mode**
4. **Check Logcat** for "Periodic check detected network change"
5. **If you see it:** Detection is working ✅

### Scenario 3: Test Full Flow
1. **Start monitoring**
2. **Toggle airplane mode**
3. **Wait 10 seconds** (check interval)
4. **Turn off airplane mode**
5. **Wait 10 seconds**
6. **Check Logcat** - should show network change
7. **Listen for sound** - should play

## Troubleshooting Tests

### Test 1: Verify Ringtones are Saved
**In Logcat, filter by `MainActivity`:**
```
MainActivity: Current saved sounds - 5G: content://..., 4G: content://...
```

**Or tap logo in app:**
- Should show "5G: Saved ✓" and "4G: Saved ✓"

### Test 2: Verify Service is Running
**Check notification bar:**
- Should see "5G Monitor Running" notification

**In Logcat:**
- Should see periodic "Initial network type" messages every 10 seconds

### Test 3: Verify Network Type Detection
**Check phone settings:**
- Settings → About Phone → Status → Network Type
- Note the current type

**In Logcat:**
- Should show same type in "Initial network type" message

### Test 4: Force Network Change
**If network won't change naturally:**
1. **Settings** → **Mobile Networks** → **Network Operators**
2. **Select "Choose automatically"** or manually select different operator
3. **This might trigger network type change**

## Expected Behavior

### When Network Changes from 4G to 5G:
1. **Logcat shows:** "Periodic check detected network change: 4G -> 5G"
2. **Logcat shows:** "playToneForType called for: 5G"
3. **Logcat shows:** "Playing ringtone using RingtoneManager for 5G"
4. **Phone plays:** 5G ringtone sound
5. **Notification updates:** Shows "Current Network: 5G"

### When Network Changes from 5G to 4G:
1. **Logcat shows:** "Periodic check detected network change: 5G -> 4G"
2. **Logcat shows:** "playToneForType called for: 4G"
3. **Logcat shows:** "Playing ringtone using RingtoneManager for 4G"
4. **Phone plays:** 4G ringtone sound
5. **Notification updates:** Shows "Current Network: 4G"

## Quick Debug Commands

### Check if App is Running:
```bash
adb shell ps | grep networkmonitor
```

### Check Current Network Type:
```bash
adb shell dumpsys telephony.registry | grep mDataNetworkType
```

### Force Stop and Restart App:
```bash
adb shell am force-stop com.example.networkmonitor
adb shell am start -n com.example.networkmonitor/.MainActivity
```

### Check Logs in Real-Time:
```bash
adb logcat -s NetworkService NetworkCallback MainActivity
```

## Tips for Testing

1. **Keep Logcat open** while testing to see what's happening
2. **Wait at least 10 seconds** between network changes (check interval)
3. **Use airplane mode** for reliable testing
4. **Check notification** to see current network type
5. **Test in areas with both 4G and 5G** if possible

## Success Indicators

✅ **App is working correctly if:**
- Notification shows "5G Monitor Running"
- Test sound plays when starting monitoring
- Logcat shows periodic network checks every 10 seconds
- Network changes are detected in Logcat
- Sounds play when network changes
- Notification updates with current network type

❌ **App has issues if:**
- No notification appears
- No logs in Logcat
- Network changes detected but no sound
- Sound plays but wrong network type
- App crashes or stops

## Still Not Working?

1. **Check Logcat** for error messages
2. **Verify ringtones are saved** (tap logo)
3. **Restart monitoring** (Stop → Start)
4. **Check battery optimization** is disabled
5. **Check phone permission** is granted
6. **Try uninstalling and reinstalling** the app

