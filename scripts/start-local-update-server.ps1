param(
    [string]$Bind = "0.0.0.0",
    [int]$Port = 8787,
    [string]$DataDir = ""
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($DataDir)) {
    $DataDir = Join-Path $root "local-vps"
}
$DataDir = [System.IO.Path]::GetFullPath($DataDir)
$secretsPath = Join-Path $DataDir "secrets.ps1"
$pidPath = Join-Path $DataDir "remote-queue.pid"

New-Item -ItemType Directory -Force $DataDir | Out-Null
New-Item -ItemType Directory -Force (Join-Path $DataDir "artifacts") | Out-Null

function New-Token {
    $bytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    } finally {
        $rng.Dispose()
    }
    return [Convert]::ToBase64String($bytes).TrimEnd("=").Replace("+", "-").Replace("/", "_")
}

if (-not (Test-Path $secretsPath)) {
    @"
`$env:CAMPER_UPDATE_ADMIN_SECRET = "$(New-Token)"
`$env:CAMPER_REMOTE_QUEUE_ADMIN_TOKEN = "$(New-Token)"
`$env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3 = "$(New-Token)"
`$env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_HIKITY_ANDROID = "$(New-Token)"
"@ | Set-Content -Path $secretsPath -Encoding ASCII
}

. $secretsPath
$env:CAMPER_REMOTE_QUEUE_DATA = $DataDir

if (Test-Path $pidPath) {
    $oldPid = Get-Content $pidPath -ErrorAction SilentlyContinue
    if ($oldPid -and (Get-Process -Id ([int]$oldPid) -ErrorAction SilentlyContinue)) {
        throw "Local update server is already running as PID $oldPid. Stop it first with scripts\stop-local-update-server.ps1."
    }
}

$args = @(
    (Join-Path $root "tools\remote-queue\queue_server.py"),
    "--host", $Bind,
    "--port", "$Port"
)
$proc = Start-Process -FilePath python -ArgumentList $args -PassThru -WindowStyle Hidden
$proc.Id | Set-Content -Path $pidPath -Encoding ASCII

Start-Sleep -Milliseconds 700
$ip = (Get-NetIPAddress -AddressFamily IPv4 |
    Where-Object { $_.IPAddress -notlike "127.*" -and $_.PrefixOrigin -ne "WellKnown" } |
    Select-Object -First 1 -ExpandProperty IPAddress)
if (-not $ip) { $ip = "127.0.0.1" }

"Local update server started"
"PID: $($proc.Id)"
"Queue URL from this PC: http://127.0.0.1:$Port"
"Queue URL from LAN nodes: http://$ip`:$Port"
"Data dir: $DataDir"
