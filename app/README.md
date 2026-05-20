# Camper Agent Android app

Minimal native Android scaffold for the headless media-unit agent.

## Build

From `E:\camper-agent`:

```powershell
.\scripts\build-debug.ps1
```

The scaffold expects Android SDK platform 35 and build-tools 35.0.0.

## Install and inspect

```powershell
adb install -r .\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n se.gottmoz.camperagent/.ui.SetupActivity
adb logcat -s TelemetryService
```

The first build only enumerates Android system metadata and USB devices. `AdapterSession` is intentionally read-only and has no CAN write commands.
