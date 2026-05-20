from __future__ import annotations

import hashlib
import hmac
import json
import time
import urllib.request
from pathlib import Path


ALLOWED_UPDATE_VERBS = {"update.agent", "restart.service"}
STAGING_ROOT = Path("/data/camper-agent/staged-updates")


class RemoteUpdateError(ValueError):
    pass


def canonical_json(payload: dict) -> bytes:
    return json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8")


def verify_manifest(secret: str, signed: dict) -> dict:
    payload = {key: value for key, value in signed.items() if key != "signature"}
    expected = hmac.new(secret.encode("utf-8"), canonical_json(payload), hashlib.sha256).hexdigest()
    if not hmac.compare_digest(expected, str(signed.get("signature", ""))):
        raise RemoteUpdateError("bad manifest signature")
    if payload.get("verb") not in ALLOWED_UPDATE_VERBS:
        raise RemoteUpdateError("verb is not allow-listed")
    if int(payload.get("expires_at", 0)) <= int(time.time()):
        raise RemoteUpdateError("manifest expired")
    return payload


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def fetch_jobs(config: dict) -> list[dict]:
    queue_url = config.get("remote_update", {}).get("queue_url")
    if not queue_url:
        return []
    node_id = config.get("node_id", "comma-3")
    request = urllib.request.Request(
        f"{queue_url.rstrip('/')}/nodes/{node_id}/jobs",
        method="GET",
        headers={"x-node-token": config.get("remote_update", {}).get("node_token", "")},
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        payload = json.loads(response.read().decode("utf-8"))
    jobs = payload.get("jobs", [])
    return jobs if isinstance(jobs, list) else []


def stage_job(config: dict, signed: dict) -> dict:
    secret = config.get("remote_update", {}).get("admin_secret", "")
    manifest = verify_manifest(secret, signed)
    job_dir = STAGING_ROOT / str(manifest["job_id"])
    job_dir.mkdir(parents=True, exist_ok=True)
    artifact_path = job_dir / "artifact.bin"
    urllib.request.urlretrieve(str(manifest["artifact_url"]), artifact_path)
    digest = sha256_file(artifact_path)
    if digest != manifest["artifact_sha256"]:
        artifact_path.unlink(missing_ok=True)
        raise RemoteUpdateError("artifact sha256 mismatch")
    (job_dir / "manifest.json").write_text(json.dumps(signed, indent=2, sort_keys=True), encoding="utf-8")
    return {
        "job_id": manifest["job_id"],
        "target": manifest["target"],
        "verb": manifest["verb"],
        "status": "staged",
        "artifact_path": str(artifact_path),
    }


def poll_and_stage(config: dict) -> list[dict]:
    if not config.get("remote_update", {}).get("enabled", False):
        return []
    results = []
    for job in fetch_jobs(config):
        try:
            results.append(stage_job(config, job))
        except Exception as error:
            results.append({
                "job_id": job.get("job_id", "unknown") if isinstance(job, dict) else "unknown",
                "status": "rejected",
                "error": error.__class__.__name__,
            })
    return results
