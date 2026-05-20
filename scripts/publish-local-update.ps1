param(
    [Parameter(Mandatory=$true)][string]$Target,
    [Parameter(Mandatory=$true)][string]$Version,
    [Parameter(Mandatory=$true)][string]$ArtifactPath,
    [string]$QueueUrl = "http://127.0.0.1:8787",
    [string]$PublicBaseUrl = "",
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
$secretsPath = Join-Path $DataDir "secrets.ps1"
if (-not (Test-Path $secretsPath)) {
    throw "Start the local update server first: .\scripts\start-local-update-server.ps1"
}
. $secretsPath

$source = Resolve-Path $ArtifactPath
$artifactDir = Join-Path $DataDir "artifacts"
New-Item -ItemType Directory -Force $artifactDir | Out-Null
$safeVersion = $Version -replace "[^A-Za-z0-9_.-]", "-"
$safeTarget = $Target -replace "[^A-Za-z0-9_.-]", "-"
$name = "$safeTarget-$safeVersion-$(Get-Date -Format yyyyMMddHHmmss)$([System.IO.Path]::GetExtension($source))"
$dest = Join-Path $artifactDir $name
Copy-Item -Path $source -Destination $dest -Force

if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
    $PublicBaseUrl = $QueueUrl
}
$artifactUrl = $PublicBaseUrl.TrimEnd("/") + "/artifacts/" + [uri]::EscapeDataString($name)

& (Join-Path $root "scripts\publish-update.ps1") `
    -Target $Target `
    -Version $Version `
    -ArtifactPath $dest `
    -ArtifactUrl $artifactUrl `
    -QueueUrl $QueueUrl `
    -Verb $Verb `
    -TtlSeconds $TtlSeconds `
    -Sequence $Sequence | Out-Null

"Published local update"
"Target: $Target"
"Artifact URL: $artifactUrl"
