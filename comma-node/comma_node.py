#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import hmac
import json
import os
import socket
import site
import sys
import time
import urllib.request
from pathlib import Path
from remote_update import poll_and_stage


ROOT = Path(__file__).resolve().parent
CONFIG_PATH = ROOT / "config.json"
OPENPILOT_ROOT = Path("/data/openpilot")
OPENPILOT_VENV_SITE = Path("/usr/local/venv/lib/python3.12/site-packages")


def load_config() -> dict:
    if CONFIG_PATH.exists():
        return json.loads(CONFIG_PATH.read_text(encoding="utf-8"))
    return json.loads((ROOT / "config.example.json").read_text(encoding="utf-8"))


def read_text(path: Path, default: str = "unknown") -> str:
    try:
        return path.read_text(encoding="utf-8", errors="replace").strip() or default
    except OSError:
        return default


def find_last_route() -> str:
    route_root = Path("/data/media/0/realdata")
    if not route_root.exists():
        return "none"
    try:
        routes = sorted((p for p in route_root.iterdir() if p.is_dir()), key=lambda p: p.stat().st_mtime, reverse=True)
    except OSError:
        return "unknown"
    return routes[0].name if routes else "none"


def collect_can_summary(duration_seconds: float) -> dict:
    if not OPENPILOT_ROOT.exists():
        return {"available": False, "frames": 0, "buses": [], "unique_addresses": 0, "error": "openpilot missing"}
    try:
        if str(OPENPILOT_ROOT) not in sys.path:
            sys.path.insert(0, str(OPENPILOT_ROOT))
        if OPENPILOT_VENV_SITE.exists():
            site.addsitedir(str(OPENPILOT_VENV_SITE))
        import cereal.messaging as messaging  # type: ignore

        logcan = messaging.sub_sock("can")
        end = time.monotonic() + max(0.1, duration_seconds)
        frames = 0
        buses: set[int] = set()
        addresses: set[int] = set()
        while time.monotonic() < end:
            msgs = messaging.drain_sock(logcan, wait_for_one=True)
            for msg in msgs:
                for frame in msg.can:
                    frames += 1
                    buses.add(int(frame.src))
                    addresses.add(int(frame.address))
        return {
            "available": True,
            "frames": frames,
            "buses": sorted(buses),
            "unique_addresses": len(addresses),
        }
    except Exception as error:
        return {
            "available": False,
            "frames": 0,
            "buses": [],
            "unique_addresses": 0,
            "error": error.__class__.__name__,
        }


def collect_status(config: dict) -> dict:
    can_summary = collect_can_summary(float(config.get("can_probe_seconds", 0.0)))
    route_id = find_last_route()
    return {
        "node_id": config.get("node_id", socket.gethostname()),
        "online": True,
        "mode": "read-only",
        "hostname": socket.gethostname(),
        "kernel": read_text(Path("/proc/version")),
        "openpilot_present": OPENPILOT_ROOT.exists(),
        "openpilot_git_head": read_text(OPENPILOT_ROOT / ".git" / "HEAD", "unknown") if OPENPILOT_ROOT.exists() else "missing",
        "last_route": route_id,
        "sensors": {
            "gps_lock": False,
            "camera_status": "unknown",
            "imu_status": "unknown"
        },
        "can_seen": can_summary["frames"] > 0,
        "can_summary": can_summary,
        "last_sync": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
    }


def build_can_upload(status: dict) -> dict:
    summary = status.get("can_summary", {})
    buses = summary.get("buses", [])
    return {
        "node_id": status["node_id"],
        "route_id": status.get("last_route", "unknown"),
        "bus_count": len(buses) if isinstance(buses, list) else 0,
        "frame_count": int(summary.get("frames", 0)),
        "arbitration_ids": [],
        "available": bool(summary.get("available", False)),
        "read_only": True,
        "last_sync": status["last_sync"],
    }


def sign_body(secret: str, timestamp: str, nonce: str, body: bytes) -> str:
    message = timestamp.encode("utf-8") + b"." + nonce.encode("utf-8") + b"." + body
    return hmac.new(secret.encode("utf-8"), message, hashlib.sha256).hexdigest()


def post_json(config: dict, endpoint: str, agent_id: str, payload: dict) -> None:
    body = json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8")
    timestamp = str(int(time.time()))
    nonce = hashlib.sha256(os.urandom(32)).hexdigest()[:24]
    secret = config["shared_secret"]
    request = urllib.request.Request(
        config["bridge_url"].rstrip("/") + endpoint,
        data=body,
        method="POST",
        headers={
            "content-type": "application/json",
            "x-agent-id": agent_id,
            "x-timestamp": timestamp,
            "x-nonce": nonce,
            "x-signature": sign_body(secret, timestamp, nonce, body),
        },
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        print(response.read().decode("utf-8"))


def post_status(config: dict, status: dict) -> None:
    post_json(config, "/api/comma/report-status", str(status["node_id"]), status)


def post_can_summary(config: dict, status: dict) -> None:
    post_json(config, "/api/comma/upload-can-summary", str(status["node_id"]), build_can_upload(status))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--once", action="store_true", help="collect one status payload and exit")
    parser.add_argument("--loop", action="store_true", help="run forever at config interval_seconds")
    parser.add_argument("--upload", action="store_true", help="upload even if config upload_enabled is false")
    args = parser.parse_args()

    config = load_config()
    while True:
        status = collect_status(config)
        print(json.dumps(status, indent=2, sort_keys=True), flush=True)

        if args.upload or config.get("upload_enabled", False):
            if not config.get("shared_secret") or config.get("shared_secret") == "replace-with-shared-secret":
                raise SystemExit("shared_secret is not configured")
            post_status(config, status)
            post_can_summary(config, status)
        for update_result in poll_and_stage(config):
            print(json.dumps({"remote_update": update_result}, sort_keys=True), flush=True)
        if not args.loop:
            break
        time.sleep(max(1, int(config.get("interval_seconds", 10))))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
