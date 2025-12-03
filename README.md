# NetworkMonitor5G - GitHub-ready Project

This Android (Kotlin) project monitors mobile network type (5G / 4G) using TelephonyCallback (Android 12+).
When network switches to 5G or drops back to 4G, it plays the user-selected ringtone.

## Features included
- TelephonyCallback (Android 12+) implementation
- Foreground Service that runs on boot (BootReceiver)
- Ringtone picker: user can choose notification sounds from device
- Battery optimization request button (user is guided to exempt the app)
- Simple start/stop controls

## How to open & build
1. Open Android Studio.
2. File -> New -> Import Project and select this folder, or clone the repository.
3. Gradle will sync and download required SDKs.
4. Connect a real Android device (emulator won't emulate cellular changes).
5. Build -> Generate Signed Bundle / APK to create an installable APK.

## Notes about APK generation
I cannot build and provide an APK from this environment. Please build APK in Android Studio locally.
If you want, I can provide a GitHub Actions workflow to auto-build APK on push.

## Battery & Auto-start notes
- The app registers a BroadcastReceiver for BOOT_COMPLETED to auto-start the foreground service on device boot.
- Some OEMs (Xiaomi, Oppo, Vivo) require additional "auto-start" permission toggles by the user — the app **cannot** programmatically grant those; include instructions for users to manually enable auto-start in device settings.

## Security & Play Store
- READ_PHONE_STATE may trigger Play Store review. When publishing, explain usage and request limited permissions only when necessary.

