from __future__ import annotations

import time
from pathlib import Path


LOG_ROOTS = (Path("/data/media/0/realdata"), Path("/data/realdata"), Path("/data/openpilot/logs"))


def collect_route_summary(node_id: str) -> dict | None:
    root = _first_existing(LOG_ROOTS)
    if root is None:
        return {
            "node_id": node_id,
            "route_id": "unknown",
            "started_at": None,
            "ended_at": None,
            "duration_s": None,
            "segment_count": 0,
            "has_camera": None,
            "has_gps": None,
            "has_can": None,
            "uploaded_at": _now(),
        }
    segments = [path for path in root.iterdir() if path.is_dir()]
    if not segments:
        return None
    latest = max(segments, key=lambda path: path.stat().st_mtime)
    children = [path for path in latest.iterdir()]
    names = [path.name.lower() for path in children]
    return {
        "node_id": node_id,
        "route_id": latest.name,
        "started_at": None,
        "ended_at": None,
        "duration_s": None,
        "segment_count": len([path for path in children if path.is_dir()]),
        "has_camera": any("camera" in name or name.endswith(".hevc") for name in names),
        "has_gps": any("gps" in name or "qlog" in name or "rlog" in name for name in names),
        "has_can": any("can" in name or "rlog" in name for name in names),
        "uploaded_at": _now(),
    }


def _first_existing(paths: tuple[Path, ...]) -> Path | None:
    for path in paths:
        if path.exists():
            return path
    return None


def _now() -> str:
    return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())
