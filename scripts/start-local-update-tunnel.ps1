param(
    [int]$Port = 8787,
    [string]$DataDir = ""
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($DataDir)) {
    $DataDir = Join-Path $root "local-vps"
}
$DataDir = [System.IO.Path]::GetFullPath($DataDir)
$serverPidPath = Join-Path $DataDir "remote-queue.pid"
$tunnelPidPath = Join-Path $DataDir "cloudflared.pid"
$tunnelLogPath = Join-Path $DataDir "cloudflared.log"
$tunnelUrlPath = Join-Path $DataDir "tunnel-url.txt"
$cloudflared = Join-Path $root "tools\bin\cloudflared.exe"

if (-not (Test-Path $cloudflared)) {
    & (Join-Path $root "scripts\install-cloudflared.ps1") | Out-Host
}

$serverRunning = $false
if (Test-Path $serverPidPath) {
    $serverPid = Get-Content $serverPidPath -ErrorAction SilentlyContinue
    $serverRunning = $serverPid -and (Get-Process -Id ([int]$serverPid) -ErrorAction SilentlyContinue)
}
if (-not $serverRunning) {
    & (Join-Path $root "scripts\start-local-update-server.ps1") -Bind 127.0.0.1 -Port $Port -DataDir $DataDir | Out-Host
}

if (Test-Path $tunnelPidPath) {
    $oldPid = Get-Content $tunnelPidPath -ErrorAction SilentlyContinue
    if ($oldPid -and (Get-Process -Id ([int]$oldPid) -ErrorAction SilentlyContinue)) {
        if (Test-Path $tunnelUrlPath) {
            "Tunnel already running: $(Get-Content $tunnelUrlPath)"
        } else {
            "Tunnel already running as PID $oldPid."
        }
        exit 0
    }
}

Remove-Item $tunnelLogPath, $tunnelUrlPath -Force -ErrorAction SilentlyContinue
$args = @("tunnel", "--url", "http://127.0.0.1:$Port", "--logfile", $tunnelLogPath, "--no-autoupdate")
$proc = Start-Process -FilePath $cloudflared -ArgumentList $args -PassThru -WindowStyle Hidden
$proc.Id | Set-Content -Path $tunnelPidPath -Encoding ASCII

$url = $null
for ($i = 0; $i -lt 40; $i++) {
    Start-Sleep -Milliseconds 500
    if (Test-Path $tunnelLogPath) {
        $text = Get-Content $tunnelLogPath -Raw -ErrorAction SilentlyContinue
        $match = [regex]::Match($text, "https://[A-Za-z0-9.-]+\.trycloudflare\.com")
        if ($match.Success) {
            $url = $match.Value
            break
        }
    }
    if ($proc.HasExited) {
        throw "cloudflared exited early. See $tunnelLogPath"
    }
}
if (-not $url) {
    throw "Tunnel started, but no public URL was found yet. See $tunnelLogPath"
}

$url | Set-Content -Path $tunnelUrlPath -Encoding ASCII

"Local update tunnel started"
"PID: $($proc.Id)"
"Public queue URL: $url"
"Use this when publishing: -QueueUrl $url -PublicBaseUrl $url"
