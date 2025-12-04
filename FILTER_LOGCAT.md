# How to Filter Logcat to See NetworkService Logs

## Current Problem
The logs you're seeing are **system logs**, not your app logs. You need to **filter** Logcat to see only your app's logs.

## Step-by-Step Filtering

### Step 1: Open Logcat
- Click "Logcat" tab at bottom of Android Studio

### Step 2: Clear the Filter Box
1. **Find the filter/search box** at the top of Logcat (usually says "Show only selected application" or has a search icon)
2. **Click in the filter box**
3. **Clear any existing text**

### Step 3: Set Filter to NetworkService
In the filter box, type exactly:
```
NetworkService
```

**OR** type:
```
tag:NetworkService
```

### Step 4: Alternative - Filter by Package
If NetworkService filter doesn't work, try:
```
package:com.example.networkmonitor
```

**OR** click the dropdown next to filter box and select:
- **"Show only selected application"** → Select your app
- **Or "Edit Filter Configuration"** → Create custom filter

## What You Should See After Filtering

After setting filter to `NetworkService`, you should see logs like:

```
NetworkService: Service onCreate called
NetworkService: Foreground notification started
NetworkService: TelephonyCallback registered successfully
NetworkService: Started periodic network type checking (every 10 seconds)
NetworkService: Initial network type: 4G (networkType=13)
NetworkService: Periodic check detected network change: 4G -> 5G
NetworkService: playToneForType called for: 5G
```

## If You Still Don't See NetworkService Logs

### Check 1: Is Service Running?
1. **On your phone**, check notification bar
2. **Look for:** "5G Monitor Running" notification
3. **If not there:** Service isn't running - tap "Start Monitoring" button

### Check 2: Try Different Filters

**Filter 1 - All App Logs:**
```
package:mine
```

**Filter 2 - MainActivity:**
```
MainActivity
```

**Filter 3 - NetworkCallback:**
```
NetworkCallback
```

**Filter 4 - Combined:**
```
NetworkService | NetworkCallback | MainActivity
```

### Check 3: Remove All Filters
1. **Clear filter box** completely
2. **Click dropdown** next to filter → Select **"No Filters"**
3. **Scroll through logs** and look for `com.example.networkmonitor` entries
4. **Look for:** "NetworkService" or "Service onCreate called"

## Quick Filter Setup

### Method 1: Simple Filter
1. **Click in filter box** (top of Logcat)
2. **Type:** `NetworkService`
3. **Press Enter**

### Method 2: Advanced Filter
1. **Click the filter icon** (funnel icon) next to filter box
2. **Click "Edit Filter Configuration"**
3. **Create new filter:**
   - **Name:** NetworkService
   - **Log Tag:** `NetworkService`
   - **Package Name:** `com.example.networkmonitor`
4. **Click OK**
5. **Select your filter** from dropdown

### Method 3: Log Level Filter
1. **Click log level dropdown** (usually says "Verbose" or "Debug")
2. **Select "Debug"** or "Info"
3. **This shows only Debug/Info level logs** (hides Verbose system logs)

## Visual Guide

### Logcat Filter Box Location:
```
┌─────────────────────────────────────────────┐
│ [Device ▼] [Filter: NetworkService] [🗑]   │ ← Filter box here!
├─────────────────────────────────────────────┤
│ NetworkService: Service onCreate called     │ ← Your logs appear here
│ NetworkService: Foreground notification... │
└─────────────────────────────────────────────┘
```

## Expected Logs When Service Starts

After tapping "Start Monitoring", with filter `NetworkService`, you should see:

```
NetworkService: Service onCreate called
NetworkService: Foreground notification started
NetworkService: TelephonyCallback registered successfully
NetworkService: Initial network type: 4G (networkType=13)
NetworkService: Started periodic network type checking (every 10 seconds)
```

## Troubleshooting

### Problem: Filter Shows Nothing
**Solutions:**
1. **Service might not be running** - Tap "Start Monitoring" button
2. **Try removing filter** - See all logs, then search for "NetworkService"
3. **Check device is selected** - Make sure your phone is selected in device dropdown

### Problem: Still See System Logs
**Solutions:**
1. **Check filter is applied** - Filter box should show `NetworkService`
2. **Try log level filter** - Set to "Debug" or "Info" instead of "Verbose"
3. **Clear old logs** - Click trash icon, then start monitoring again

### Problem: See MainActivity Logs But Not NetworkService
**This means:**
- App is running ✅
- But service is NOT starting ❌

**Solution:**
- Check if you tapped "Start Monitoring" button
- Check notification bar for "5G Monitor Running"
- Check for permission errors in logs

## Quick Test

1. **Set filter:** `NetworkService`
2. **Clear logs** (trash icon)
3. **On phone:** Tap "Start Monitoring"
4. **Watch Logcat** - Should immediately show "Service onCreate called"

If you don't see it, the service isn't starting. Check for errors or permission issues.

