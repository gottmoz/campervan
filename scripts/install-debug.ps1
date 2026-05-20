$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$localAdb = Join-Path $root ".android-sdk\platform-tools\adb.exe"
$adb = if (Test-Path $localAdb) { $localAdb } else { "adb" }
$apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"

if (-not (Test-Path $apk)) {
    throw "APK missing: $apk. Run scripts\build-debug.ps1 first."
}

& $adb devices
& $adb install -r $apk
