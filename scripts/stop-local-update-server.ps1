param(
    [string]$DataDir = ""
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($DataDir)) {
    $DataDir = Join-Path $root "local-vps"
}
$pidPath = Join-Path ([System.IO.Path]::GetFullPath($DataDir)) "remote-queue.pid"

if (-not (Test-Path $pidPath)) {
    "Local update server is not running."
    exit 0
}

$pidValue = Get-Content $pidPath -ErrorAction SilentlyContinue
if ($pidValue -and (Get-Process -Id ([int]$pidValue) -ErrorAction SilentlyContinue)) {
    Stop-Process -Id ([int]$pidValue) -Force
    "Stopped local update server PID $pidValue."
} else {
    "Local update server PID was stale."
}
Remove-Item $pidPath -Force -ErrorAction SilentlyContinue
