$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$localSdk = Join-Path $root ".android-sdk"

if (Test-Path $localSdk) {
    $env:ANDROID_SDK_ROOT = $localSdk
    $env:ANDROID_HOME = $localSdk
}

Push-Location $root
try {
    .\gradlew.bat :app:assembleDebug --stacktrace
} finally {
    Pop-Location
}
