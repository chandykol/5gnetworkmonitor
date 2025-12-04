# How to Add Your App Icon (display.png)

To add your custom app icon PNG file:

## Option 1: Using Android Studio (Recommended)

1. **Right-click** on `app/src/main/res` folder in Android Studio
2. Select **New > Image Asset**
3. Choose **Launcher Icons (Adaptive and Legacy)**
4. Click on **Foreground Layer** tab
5. Click **Path** and select your `display.png` file
6. Adjust the icon as needed
7. Click **Next** and then **Finish**

Android Studio will automatically create icons in all required densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).

## Option 2: Manual Method

1. **Create these folders** in `app/src/main/res/`:
   - `mipmap-mdpi/`
   - `mipmap-hdpi/`
   - `mipmap-xhdpi/`
   - `mipmap-xxhdpi/`
   - `mipmap-xxxhdpi/`

2. **Resize your PNG** to these sizes:
   - mdpi: 48x48 pixels
   - hdpi: 72x72 pixels
   - xhdpi: 96x96 pixels
   - xxhdpi: 144x144 pixels
   - xxxhdpi: 192x192 pixels

3. **Name the file** `ic_launcher.png` in each folder

4. **Place the resized images** in their respective folders

## Option 3: Quick Test (Single Icon)

If you just want to test quickly, you can:
1. Place your `display.png` file in `app/src/main/res/mipmap-hdpi/`
2. Rename it to `ic_launcher.png`
3. Replace the existing `ic_launcher.png` file

**Note:** The app is already configured to use `@mipmap/ic_launcher` in AndroidManifest.xml, so once you add the icon files, they will be used automatically.

