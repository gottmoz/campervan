from __future__ import annotations

import hashlib
import hmac
import json
import time
from dataclasses import dataclass
from pathlib import Path


ALLOWED_UPDATE_VERBS = {"update.agent", "restart.service"}


class RemoteUpdateError(ValueError):
    pass


@dataclass(frozen=True)
class UpdateManifest:
    job_id: str
    target: str
    version: str
    artifact_url: str
    artifact_sha256: str
    verb: str
    expires_at: int
    nonce: str
    sequence: int

    def unsigned_dict(self) -> dict:
        return {
            "job_id": self.job_id,
            "target": self.target,
            "version": self.version,
            "artifact_url": self.artifact_url,
            "artifact_sha256": self.artifact_sha256,
            "verb": self.verb,
            "expires_at": self.expires_at,
            "nonce": self.nonce,
            "sequence": self.sequence,
        }


def canonical_json(payload: dict) -> bytes:
    return json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8")


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def sign_payload(secret: str, payload: dict) -> str:
    if not secret:
        raise RemoteUpdateError("missing signing secret")
    return hmac.new(secret.encode("utf-8"), canonical_json(payload), hashlib.sha256).hexdigest()


def verify_signed_manifest(secret: str, signed: dict, *, now: int | None = None) -> dict:
    payload = {key: value for key, value in signed.items() if key != "signature"}
    expected = sign_payload(secret, payload)
    signature = str(signed.get("signature", ""))
    if not hmac.compare_digest(expected, signature):
        raise RemoteUpdateError("bad manifest signature")
    validate_manifest(payload, now=now)
    return payload


def validate_manifest(payload: dict, *, now: int | None = None) -> None:
    missing = [
        key for key in (
            "job_id",
            "target",
            "version",
            "artifact_url",
            "artifact_sha256",
            "verb",
            "expires_at",
            "nonce",
            "sequence",
        )
        if key not in payload
    ]
    if missing:
        raise RemoteUpdateError(f"missing fields: {', '.join(missing)}")
    if payload["verb"] not in ALLOWED_UPDATE_VERBS:
        raise RemoteUpdateError("verb is not allow-listed")
    if int(payload["expires_at"]) <= (int(time.time()) if now is None else now):
        raise RemoteUpdateError("manifest expired")
    if not isinstance(payload["artifact_sha256"], str) or len(payload["artifact_sha256"]) != 64:
        raise RemoteUpdateError("invalid artifact sha256")


def signed_manifest(manifest: UpdateManifest, secret: str) -> dict:
    payload = manifest.unsigned_dict()
    validate_manifest(payload)
    return {**payload, "signature": sign_payload(secret, payload)}
