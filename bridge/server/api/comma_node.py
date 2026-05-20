from __future__ import annotations

import json

from bridge_agent.security import Envelope, verify_envelope
from bridge_agent.storage import Store


COMMA_CONFIG = {
    "ok": True,
    "mode": "read_only",
    "upload_status": True,
    "upload_route_summary": True,
    "upload_can_summary": False,
    "allow_shell": False,
    "allow_can_write": False,
    "allow_controls": False,
    "controlAllowed": False,
    "canWriteAllowed": False,
}

FORBIDDEN_FIELDS = {
    "shell",
    "command",
    "exec",
    "can_write",
    "allow_can_write",
    "control",
    "allow_controls",
    "steer",
    "brake",
    "throttle",
}


def is_comma_path(path: str) -> bool:
    return path in {
        "/api/comma/report-status",
        "/api/comma/upload-route-log",
        "/api/comma/upload-route-summary",
        "/api/comma/upload-can-summary",
        "/api/comma/upload-diagnostic-bundle",
        "/api/comma/transit-research/upload-can-summary",
        "/api/comma/transit-research/upload-fingerprint",
        "/api/comma/transit-research/upload-fw-summary",
        "/api/comma/config",
    }


def handle_comma_signed(path: str, store: Store, agent_id: str, body: bytes) -> tuple[int, dict]:
    if path != "/api/comma/config" and _has_forbidden_field(body):
        return 400, {"error": "forbidden field"}
    if path == "/api/comma/report-status":
        saved = store.save_payload("comma/status", agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path in {"/api/comma/upload-route-log", "/api/comma/upload-route-summary"}:
        saved = store.save_payload("comma/routes", agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/comma/upload-can-summary":
        saved = store.save_payload("comma/can", agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/comma/upload-diagnostic-bundle":
        saved = store.save_payload("comma/diagnostics", agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path in {
        "/api/comma/transit-research/upload-can-summary",
        "/api/comma/transit-research/upload-fingerprint",
        "/api/comma/transit-research/upload-fw-summary",
    }:
        saved = store.save_payload("comma/transit_research", agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/comma/config":
        return 200, dict(COMMA_CONFIG)
    return 404, {"error": "not found"}


def verify_comma_request(secret: str, envelope: Envelope, body: bytes) -> None:
    verify_envelope(secret, envelope, body)


def _has_forbidden_field(body: bytes) -> bool:
    try:
        payload = json.loads(body.decode("utf-8") or "{}")
    except json.JSONDecodeError:
        return False
    return _contains_forbidden_key(payload)


def _contains_forbidden_key(value: object) -> bool:
    if isinstance(value, dict):
        return any(str(key).lower() in FORBIDDEN_FIELDS or _contains_forbidden_key(child) for key, child in value.items())
    if isinstance(value, list):
        return any(_contains_forbidden_key(item) for item in value)
    return False
