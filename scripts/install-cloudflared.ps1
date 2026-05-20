param(
    [string]$ToolsDir = ""
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($ToolsDir)) {
    $ToolsDir = Join-Path $root "tools\bin"
}
$ToolsDir = [System.IO.Path]::GetFullPath($ToolsDir)
$exe = Join-Path $ToolsDir "cloudflared.exe"

New-Item -ItemType Directory -Force $ToolsDir | Out-Null
if (Test-Path $exe) {
    "cloudflared already installed: $exe"
    exit 0
}

$url = "https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-windows-amd64.exe"
Invoke-WebRequest -Uri $url -OutFile $exe

"cloudflared installed: $exe"
