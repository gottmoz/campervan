param(
    [Parameter(Mandatory=$true)][string]$Target,
    [Parameter(Mandatory=$true)][string]$Version,
    [Parameter(Mandatory=$true)][string]$ArtifactPath,
    [Parameter(Mandatory=$true)][string]$ArtifactUrl,
    [string]$QueueUrl = "http://127.0.0.1:8787",
    [string]$Verb = "update.agent",
    [int]$TtlSeconds = 86400,
    [int]$Sequence = 1
)

$ErrorActionPreference = "Stop"
$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$artifact = Resolve-Path $ArtifactPath
$secret = $env:CAMPER_UPDATE_ADMIN_SECRET
if ([string]::IsNullOrWhiteSpace($secret)) {
    throw "Set CAMPER_UPDATE_ADMIN_SECRET before publishing."
}
$queueToken = $env:CAMPER_REMOTE_QUEUE_ADMIN_TOKEN
if ([string]::IsNullOrWhiteSpace($queueToken)) {
    throw "Set CAMPER_REMOTE_QUEUE_ADMIN_TOKEN before publishing."
}

$env:PYTHONPATH = Join-Path $root "bridge"
$env:TARGET = $Target
$env:VERSION = $Version
$env:ARTIFACT_URL = $ArtifactUrl
$env:ARTIFACT_PATH = $artifact
$env:VERB = $Verb
$env:TTL_SECONDS = "$TtlSeconds"
$env:SEQUENCE = "$Sequence"

$manifestJson = @"
import json, os, time, uuid
from pathlib import Path
from bridge_agent.remote_update import UpdateManifest, sha256_file, signed_manifest

manifest = UpdateManifest(
    job_id=str(uuid.uuid4()),
    target=os.environ["TARGET"],
    version=os.environ["VERSION"],
    artifact_url=os.environ["ARTIFACT_URL"],
    artifact_sha256=sha256_file(Path(os.environ["ARTIFACT_PATH"])),
    verb=os.environ["VERB"],
    expires_at=int(time.time()) + int(os.environ["TTL_SECONDS"]),
    nonce=uuid.uuid4().hex,
    sequence=int(os.environ["SEQUENCE"]),
)
print(json.dumps(signed_manifest(manifest, os.environ["CAMPER_UPDATE_ADMIN_SECRET"]), separators=(",", ":"), sort_keys=True))
"@ | python -

$body = $manifestJson
Invoke-RestMethod -Method Post -Uri ($QueueUrl.TrimEnd("/") + "/jobs") -ContentType "application/json" -Body $body -Headers @{
    "x-admin-token" = $queueToken
}
