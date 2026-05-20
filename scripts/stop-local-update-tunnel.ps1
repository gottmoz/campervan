param(
    [string]$DataDir = "",
    [switch]$KeepServer
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($DataDir)) {
    $DataDir = Join-Path $root "local-vps"
}
$DataDir = [System.IO.Path]::GetFullPath($DataDir)
$pidPath = Join-Path $DataDir "cloudflared.pid"

if (Test-Path $pidPath) {
    $pidValue = Get-Content $pidPath -ErrorAction SilentlyContinue
    if ($pidValue -and (Get-Process -Id ([int]$pidValue) -ErrorAction SilentlyContinue)) {
        Stop-Process -Id ([int]$pidValue) -Force
        "Stopped tunnel PID $pidValue."
    } else {
        "Tunnel PID was stale."
    }
    Remove-Item $pidPath -Force -ErrorAction SilentlyContinue
} else {
    "Tunnel is not running."
}

if (-not $KeepServer) {
    & (Join-Path $root "scripts\stop-local-update-server.ps1") -DataDir $DataDir
}
