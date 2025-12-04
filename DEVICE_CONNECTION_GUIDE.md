# Android Device Connection Guide - Device Not Showing in Android Studio

## Quick Fixes (Try These First)

### 1. Enable USB Debugging
1. **Go to Settings** → **About Phone**
2. **Tap "Build Number" 7 times** (if developer options not already enabled)
3. **Go back** → **Settings** → **Developer Options**
4. **Enable "USB Debugging"** (toggle ON)
5. **Enable "USB Debugging (Security Settings)"** if available
6. **Unplug and replug** USB cable

### 2. Check USB Connection Mode
When you connect USB, your phone should show a notification:
- **Tap the USB notification**
- **Select "File Transfer" or "MTP"** (NOT "Charging only")
- Some phones: Select **"PTP"** or **"Developer Mode"**

### 3. Authorize Computer
When you first connect:
- Phone will show popup: **"Allow USB debugging?"**
- **Check "Always allow from this computer"**
- **Tap "Allow"**
- If you denied before, revoke USB debugging authorizations:
  - Settings → Developer Options → **"Revoke USB debugging authorizations"**
  - Then reconnect

### 4. Restart ADB (Android Debug Bridge)
In Android Studio:
1. **View** → **Tool Windows** → **Terminal**
2. Type these commands:
```bash
adb kill-server
adb start-server
adb devices
```
You should see your device listed.

### 5. Check USB Cable & Port
- **Try a different USB cable** (some cables are charge-only)
- **Try a different USB port** on your computer
- **Try USB 2.0 port** instead of USB 3.0 (sometimes works better)

## Step-by-Step Troubleshooting

### Step 1: Verify Developer Options
1. Settings → About Phone → Build Number (tap 7 times)
2. Settings → Developer Options
3. Make sure these are ON:
   - ✅ USB Debugging
   - ✅ USB Debugging (Security Settings) - if available
   - ✅ Stay Awake (optional, helps during development)

### Step 2: Check Phone Notification
When connected via USB:
- Look for USB notification in notification bar
- Tap it and select **"File Transfer"** or **"MTP"**
- Some phones: Select **"PTP"** or **"Developer Mode"**

### Step 3: Check Android Studio
1. **Open Android Studio**
2. **Click "No Devices" dropdown** (top toolbar, next to run button)
3. **Click "Troubleshoot Device Connections"**
4. Follow the wizard

### Step 4: Check Device Manager (Windows)
On Windows:
1. **Right-click "This PC"** → **Properties** → **Device Manager**
2. Look for your phone under **"Portable Devices"** or **"Android Phone"**
3. If you see **yellow exclamation mark**:
   - Right-click → **Update Driver**
   - Or install phone's USB drivers from manufacturer website

### Step 5: Install USB Drivers (Windows)
For Windows users, install drivers:
- **Samsung**: Samsung USB Driver
- **Xiaomi**: Mi USB Driver
- **OnePlus**: OnePlus USB Driver
- **Generic**: Google USB Driver (comes with Android Studio SDK)

To install Google USB Driver:
1. **Tools** → **SDK Manager**
2. **SDK Tools** tab
3. Check **"Google USB Driver"**
4. **Apply** → **OK**

### Step 6: Check ADB Manually
Open Terminal/Command Prompt:

**Windows:**
```cmd
cd C:\Users\YourUsername\AppData\Local\Android\Sdk\platform-tools
adb devices
```

**Mac/Linux:**
```bash
cd ~/Library/Android/sdk/platform-tools
adb devices
```

**Expected Output:**
```
List of devices attached
ABC123XYZ    device
```

If you see **"unauthorized"**:
- Check phone for USB debugging authorization popup
- Revoke and reconnect

If you see **"offline"**:
- Restart ADB: `adb kill-server && adb start-server`
- Reconnect phone

If **nothing shows**:
- Check USB cable/port
- Check USB connection mode on phone
- Install USB drivers

## Phone-Specific Instructions

### Samsung Phones
1. Enable **"USB Debugging"**
2. Enable **"Install via USB"** (if available)
3. When connecting, select **"File Transfer"** or **"MTP"**
4. Install **Samsung USB Driver** from Samsung website

### Xiaomi/MIUI Phones
1. Enable **"USB Debugging"**
2. Enable **"USB Debugging (Security Settings)"**
3. Enable **"Install via USB"**
4. When connecting, select **"File Transfer"**
5. May need to enable **"Developer Options"** → **"USB Configuration"** → **"MTP"**

### OnePlus Phones
1. Enable **"USB Debugging"**
2. Enable **"OEM Unlocking"** (if available)
3. Select **"File Transfer"** when connecting

### Huawei Phones
1. Enable **"USB Debugging"**
2. Enable **"Allow ADB debugging in charge only mode"**
3. May need HiSuite installed

### Oppo/Realme Phones
1. Enable **"USB Debugging"**
2. Enable **"Install via USB"**
3. Select **"File Transfer"** when connecting

## Alternative: Use Wireless Debugging (Android 11+)

If USB doesn't work, try wireless debugging:

1. **Connect phone and computer to same WiFi**
2. **Settings** → **Developer Options**
3. **Enable "Wireless debugging"**
4. **Tap "Wireless debugging"** → **"Pair device with pairing code"**
5. **Note the IP address and port** (e.g., 192.168.1.100:12345)
6. In Android Studio Terminal:
```bash
adb pair 192.168.1.100:12345
```
7. Enter the pairing code from phone
8. Then:
```bash
adb connect 192.168.1.100:XXXXX
```
(Use the port shown after pairing)

## Check Android Studio Settings

1. **File** → **Settings** (or **Preferences** on Mac)
2. **Appearance & Behavior** → **System Settings** → **Android SDK**
3. **SDK Tools** tab
4. Make sure **"Android SDK Platform-Tools"** is installed
5. **Apply** → **OK**

## Still Not Working?

### Try These:
1. **Restart Android Studio**
2. **Restart your computer**
3. **Restart your phone**
4. **Try different USB cable**
5. **Try different computer** (to isolate if it's computer-specific)
6. **Check if phone works with other computers**

### Check Android Studio Logs
1. **Help** → **Show Log in Explorer**
2. Look for errors related to ADB or device connection

### Reinstall ADB
1. **Tools** → **SDK Manager**
2. **SDK Tools** tab
3. **Uncheck** "Android SDK Platform-Tools"
4. **Apply** → **OK**
5. **Check** "Android SDK Platform-Tools" again
6. **Apply** → **OK**

## Quick Checklist

- [ ] Developer Options enabled
- [ ] USB Debugging enabled
- [ ] USB connection mode set to "File Transfer" or "MTP"
- [ ] USB debugging authorization granted on phone
- [ ] USB cable is data cable (not charge-only)
- [ ] USB drivers installed (Windows)
- [ ] ADB running (`adb devices` shows device)
- [ ] Android Studio recognizes device in device dropdown

## Testing Connection

Once connected, test with:
```bash
adb devices
```

Should show:
```
List of devices attached
[DEVICE_ID]    device
```

Then in Android Studio:
- Device should appear in device dropdown (top toolbar)
- Logcat should show device name
- You can run/debug apps on device

