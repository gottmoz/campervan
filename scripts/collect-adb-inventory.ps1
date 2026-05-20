$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$localAdb = Join-Path $root ".android-sdk\platform-tools\adb.exe"
$adb = if (Test-Path $localAdb) { $localAdb } else { "adb" }
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$outDir = Join-Path $root "logs\adb-inventory\$timestamp"

New-Item -ItemType Directory -Force -Path $outDir | Out-Null

& $adb devices | Set-Content -Path (Join-Path $outDir "adb-devices.txt") -Encoding UTF8
& $adb shell getprop | Set-Content -Path (Join-Path $outDir "getprop.txt") -Encoding UTF8
& $adb shell dumpsys usb | Set-Content -Path (Join-Path $outDir "dumpsys-usb.txt") -Encoding UTF8
& $adb shell dumpsys -l | Set-Content -Path (Join-Path $outDir "dumpsys-services.txt") -Encoding UTF8
& $adb shell pm list packages -f | Set-Content -Path (Join-Path $outDir "packages.txt") -Encoding UTF8
& $adb shell dumpsys package se.gottmoz.camperagent | Set-Content -Path (Join-Path $outDir "package-camperagent.txt") -Encoding UTF8
& $adb logcat -d | Set-Content -Path (Join-Path $outDir "logcat.txt") -Encoding UTF8

Write-Host "Inventory written to $outDir"
