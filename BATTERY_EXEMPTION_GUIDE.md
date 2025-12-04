# Battery Exemption Guide for 5G Network Monitor

## Why Battery Exemption is Needed

Android's battery optimization can stop background services to save battery. For the 5G Network Monitor app to work properly and detect network changes in the background, you need to disable battery optimization for this app.

## How to Set Battery Exemption

### Method 1: Using the App Button (Easiest)

1. **Open the 5G Network Monitor app**
2. **Tap the "Request Battery Exemption" button**
3. **A popup will appear** asking to allow battery optimization exemption
4. **Tap "Allow"** on the popup
5. **Done!** The app will now work in the background

### Method 2: Manual Setup (If Method 1 doesn't work)

#### For Most Android Phones:

1. Go to **Settings** → **Apps** → **5G Monitor**
2. Tap on **"Battery"** or **"Power usage"**
3. Select **"Unrestricted"** or **"Don't optimize"**
4. Done!

#### Alternative Path:

1. Go to **Settings** → **Battery**
2. Tap on **"Battery optimization"** or **"Power saving exclusions"**
3. Find **"5G Monitor"** in the list
4. Select it and choose **"Don't optimize"** or **"Unrestricted"**
5. Tap **"Done"** or **"Save"**

#### For Samsung Phones:

1. Go to **Settings** → **Apps**
2. Find **"5G Monitor"**
3. Tap **"Battery"**
4. Select **"Unrestricted"**
5. Also check **"Allow background activity"**

#### For Xiaomi/MIUI Phones:

1. Go to **Settings** → **Apps** → **Manage apps**
2. Find **"5G Monitor"**
3. Tap **"Battery saver"**
4. Select **"No restrictions"**
5. Also go to **Settings** → **Battery** → **App battery saver** and set to **"No restrictions"**

#### For Oppo/ColorOS Phones:

1. Go to **Settings** → **Battery**
2. Tap **"App battery management"**
3. Find **"5G Monitor"**
4. Select **"Allow background activity"** and **"Allow auto-start"**

#### For Vivo/FuntouchOS Phones:

1. Go to **Settings** → **Battery**
2. Tap **"Background app management"**
3. Find **"5G Monitor"**
4. Enable **"Allow background activity"**
5. Also enable **"Auto-start"** in **Settings** → **More settings** → **Application management**

## Verify Battery Exemption is Set

After setting battery exemption:
1. Open the app
2. Tap the **"Request Battery Exemption"** button
3. You should see: **"✓ Battery optimization is already disabled for this app"**

## Troubleshooting

### If the app still stops working:

1. **Check if the service is running**: Look for the notification "5G Monitor Running"
2. **Restart the app**: Stop and start monitoring again
3. **Check app permissions**: Make sure phone permission is granted
4. **Some phones need additional settings**:
   - Enable "Auto-start" permission
   - Add app to "Protected apps" list
   - Disable "Doze mode" for this app

### Still having issues?

Check the app logs in Logcat:
- Filter by "NetworkService" to see if the service is running
- Look for "Permission denied" or "Service stopped" messages

## Important Notes

- Battery exemption is **safe** - it only allows the app to run in the background
- The app uses minimal battery - it only checks network type when it changes
- You can revoke battery exemption anytime from Settings if needed
- Some phone manufacturers (Xiaomi, Oppo, Vivo) have additional battery saving features that may need to be disabled separately

