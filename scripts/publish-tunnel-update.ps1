param(
    [Parameter(Mandatory=$true)][string]$Target,
    [Parameter(Mandatory=$true)][string]$Version,
    [Parameter(Mandatory=$true)][string]$ArtifactPath,
    [int]$Port = 8787,
    [string]$DataDir = "",
    [string]$Verb = "update.agent",
    [int]$TtlSeconds = 86400,
    [int]$Sequence = 1
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
if ([string]::IsNullOrWhiteSpace($DataDir)) {
    $DataDir = Join-Path $root "local-vps"
}
$DataDir = [System.IO.Path]::GetFullPath($DataDir)
$urlPath = Join-Path $DataDir "tunnel-url.txt"
if (-not (Test-Path $urlPath)) {
    throw "Start the tunnel first: .\scripts\start-local-update-tunnel.ps1"
}
$url = (Get-Content $urlPath -Raw).Trim()

& (Join-Path $root "scripts\publish-local-update.ps1") `
    -Target $Target `
    -Version $Version `
    -ArtifactPath $ArtifactPath `
    -QueueUrl "http://127.0.0.1:$Port" `
    -PublicBaseUrl $url `
    -DataDir $DataDir `
    -Verb $Verb `
    -TtlSeconds $TtlSeconds `
    -Sequence $Sequence
