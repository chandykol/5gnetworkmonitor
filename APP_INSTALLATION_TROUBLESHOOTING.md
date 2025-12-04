# App Installation Troubleshooting - "Process ID not found"

## Quick Fixes

### 1. Install the App First
The error means the app is not installed on your device. You need to install it first:

**Method 1: Run from Android Studio**
1. **Connect your device** (follow DEVICE_CONNECTION_GUIDE.md if needed)
2. **Select your device** from the device dropdown (top toolbar)
3. **Click the green "Run" button** (or press Shift+F10)
4. Wait for app to install and launch

**Method 2: Install APK Manually**
1. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
2. Wait for build to complete
3. **Click "locate"** in the notification
4. Copy APK to your phone
5. Install APK on phone (enable "Install from Unknown Sources" if needed)

### 2. Check Device Connection
Make sure your device is connected and recognized:

1. **Check device dropdown** (top toolbar) - should show your device name
2. **Run in Terminal:**
   ```bash
   adb devices
   ```
   Should show your device listed

### 3. Check App Installation
Verify app is installed:

**In Terminal:**
```bash
adb shell pm list packages | grep networkmonitor
```

Should show:
```
package:com.example.networkmonitor
```

If not shown, app is not installed. Install it using Method 1 or 2 above.

## Step-by-Step Installation

### Step 1: Build the App
1. **Build** → **Clean Project** (optional, but recommended)
2. **Build** → **Rebuild Project**
3. Wait for build to complete (check bottom status bar)

### Step 2: Connect Device
1. **Connect phone via USB**
2. **Enable USB Debugging** (if not already)
3. **Select "File Transfer"** USB mode
4. **Authorize computer** if prompted

### Step 3: Select Device
1. **Click device dropdown** (top toolbar, next to Run button)
2. **Select your device** from list
3. If device not showing, see DEVICE_CONNECTION_GUIDE.md

### Step 4: Run the App
1. **Click green "Run" button** (or press Shift+F10)
2. **Select "app"** if asked
3. Wait for installation and launch

## Common Issues & Solutions

### Issue 1: "Process ID not found" when trying to Debug
**Solution:**
- Don't use "Debug" button if app isn't installed
- Use "Run" button first to install the app
- Then you can use "Debug" button

### Issue 2: Build Fails
**Check:**
- **Build** → **Rebuild Project**
- Check for errors in "Build" tab at bottom
- Fix any compilation errors
- Make sure Gradle sync completed successfully

### Issue 3: Installation Fails
**Check:**
- Device has enough storage space
- USB connection is stable
- USB Debugging is enabled
- Try uninstalling old version first:
  ```bash
  adb uninstall com.example.networkmonitor
  ```

### Issue 4: App Installs but Won't Run
**Check:**
- Check Logcat for crash logs
- Make sure all permissions are granted
- Check if app requires specific Android version

### Issue 5: "App not installed" Error
**Solutions:**
1. **Uninstall old version first:**
   ```bash
   adb uninstall com.example.networkmonitor
   ```

2. **Enable "Install from Unknown Sources":**
   - Settings → Security → Unknown Sources (enable)

3. **Check app signature:**
   - If you have debug and release versions, uninstall both
   - Reinstall fresh

## Manual Installation via ADB

If Android Studio installation fails:

1. **Build APK:**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Wait for completion

2. **Locate APK:**
   - Click "locate" in notification
   - Or go to: `app/build/outputs/apk/debug/app-debug.apk`

3. **Install via ADB:**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

4. **If app already exists:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
   (`-r` flag replaces existing app)

## Verify Installation

After installation, verify:

1. **Check if app is installed:**
   ```bash
   adb shell pm list packages | grep networkmonitor
   ```

2. **Launch app:**
   ```bash
   adb shell am start -n com.example.networkmonitor/.MainActivity
   ```

3. **Check app in device:**
   - Look for "5G Monitor" app icon on your phone
   - Tap to open

## Debugging Session Issues

If you're trying to debug:

1. **First install app** using Run button
2. **Then start app** manually on device
3. **Then attach debugger:**
   - Run → Attach Debugger to Android Process
   - Select `com.example.networkmonitor`

## Quick Checklist

- [ ] Device connected and recognized (`adb devices` shows device)
- [ ] App built successfully (no build errors)
- [ ] Device selected in Android Studio dropdown
- [ ] App installed on device (check with `adb shell pm list packages`)
- [ ] App can be launched manually on device
- [ ] Then try Run/Debug from Android Studio

## Still Having Issues?

### Check Logs
1. **View** → **Tool Windows** → **Build**
2. Look for errors during build/installation

### Check Device Logs
1. **View** → **Tool Windows** → **Logcat**
2. Filter by `package:mine`
3. Look for installation errors

### Try Clean Install
1. **Uninstall app** from device (Settings → Apps → 5G Monitor → Uninstall)
2. **Clean project:** Build → Clean Project
3. **Rebuild:** Build → Rebuild Project
4. **Reinstall:** Run button

## Alternative: Install via File Manager

1. **Build APK** (Build → Build Bundle(s) / APK(s) → Build APK(s))
2. **Copy APK** to phone (via USB, email, cloud, etc.)
3. **On phone:** Open file manager → Find APK → Tap to install
4. **Enable "Install from Unknown Sources"** if prompted
5. **Install** the APK

This method doesn't require ADB and works even if USB debugging has issues.

