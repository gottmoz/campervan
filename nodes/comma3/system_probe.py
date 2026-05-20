from __future__ import annotations

import os
import platform
import socket
import time
from pathlib import Path


OPENPILOT_PATHS = (Path("/data/openpilot"), Path.home() / "openpilot")


def collect_system_status(node_id: str) -> dict:
    openpilot_path = _first_existing(OPENPILOT_PATHS)
    return {
        "node_id": node_id,
        "device_type": "comma3",
        "hostname": socket.gethostname(),
        "openpilot_running": _process_seen(("manager.py", "controlsd", "plannerd")),
        "agnos_version": _read_first("/VERSION", "/AGNOS_VERSION"),
        "openpilot_version": None,
        "git_commit": _git_commit(openpilot_path) if openpilot_path else None,
        "started_at": None,
        "reported_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "storage_free_mb": _storage_free_mb(Path("/data") if Path("/data").exists() else Path.cwd()),
        "thermal_status": "unknown",
        "gps_status": "unknown",
        "camera_status": "unknown",
        "can_seen": None,
        "safety_mode": "read_only",
        "os": platform.platform(),
        "uptime_s": _uptime_s(),
        "openpilot_path": str(openpilot_path) if openpilot_path else None,
    }


def _first_existing(paths: tuple[Path, ...]) -> Path | None:
    for path in paths:
        if path.exists():
            return path
    return None


def _read_first(*paths: str) -> str | None:
    for raw in paths:
        path = Path(raw)
        if path.exists() and path.is_file():
            return path.read_text(encoding="utf-8", errors="ignore").strip() or None
    return None


def _storage_free_mb(path: Path) -> int | None:
    try:
        stat = os.statvfs(path)
        return int(stat.f_bavail * stat.f_frsize / 1024 / 1024)
    except Exception:
        try:
            import shutil

            return int(shutil.disk_usage(path).free / 1024 / 1024)
        except Exception:
            return None


def _uptime_s() -> int | None:
    path = Path("/proc/uptime")
    if not path.exists():
        return None
    try:
        return int(float(path.read_text(encoding="utf-8").split()[0]))
    except Exception:
        return None


def _process_seen(names: tuple[str, ...]) -> bool | None:
    proc = Path("/proc")
    if not proc.exists():
        return None
    for item in proc.iterdir():
        if not item.name.isdigit():
            continue
        try:
            cmdline = (item / "cmdline").read_text(encoding="utf-8", errors="ignore")
        except Exception:
            continue
        if any(name in cmdline for name in names):
            return True
    return False


def _git_commit(repo: Path | None) -> str | None:
    if repo is None:
        return None
    git_dir = repo / ".git"
    head = git_dir / "HEAD"
    if not head.exists():
        return None
    content = head.read_text(encoding="utf-8", errors="ignore").strip()
    if content.startswith("ref: "):
        ref = git_dir / content.removeprefix("ref: ").strip()
        return ref.read_text(encoding="utf-8", errors="ignore").strip()[:40] if ref.exists() else None
    return content[:40]
