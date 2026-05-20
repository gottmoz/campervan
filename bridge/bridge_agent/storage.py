from __future__ import annotations

import json
import time
from pathlib import Path
from uuid import uuid4


class Store:
    def __init__(self, root: Path):
        self.root = root
        self.root.mkdir(parents=True, exist_ok=True)
        for name in (
            "inventory",
            "logs",
            "status",
            "comma-status",
            "comma-route-log",
            "comma-can-summary",
            "comma/status",
            "comma/routes",
            "comma/can",
            "comma/diagnostics",
            "comma/transit_research",
        ):
            (self.root / name).mkdir(parents=True, exist_ok=True)

    def save_payload(self, kind: str, agent_id: str, body: bytes) -> Path:
        safe_agent = "".join(ch if ch.isalnum() or ch in "-_." else "_" for ch in agent_id) or "unknown"
        (self.root / kind).mkdir(parents=True, exist_ok=True)
        target = self.root / kind / f"{int(time.time())}-{safe_agent}-{uuid4().hex}.json"
        target.write_bytes(body)
        return target

    def load_commands(self) -> list[dict]:
        path = self.root / "commands.json"
        if not path.exists():
            return []
        data = json.loads(path.read_text(encoding="utf-8"))
        if isinstance(data, dict):
            data = data.get("commands", [])
        if not isinstance(data, list):
            return []
        return [item for item in data if isinstance(item, dict)]

    def load_latest_payload(self, kind: str) -> dict | None:
        directory = self.root / kind
        if not directory.exists():
            return None
        files = [path for path in directory.iterdir() if path.is_file()]
        if not files:
            return None
        latest = max(files, key=lambda path: path.stat().st_mtime)
        data = json.loads(latest.read_text(encoding="utf-8"))
        if not isinstance(data, dict):
            return None
        return {
            "path": str(latest),
            "received_at": int(latest.stat().st_mtime),
            "status": data,
        }

    def remember_nonce(self, agent_id: str, nonce: str, timestamp: str) -> bool:
        path = self.root / "nonces.json"
        try:
            seen = json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}
        except json.JSONDecodeError:
            seen = {}
        key = f"{agent_id}:{nonce}"
        if key in seen:
            return False
        now = int(time.time())
        cutoff = now - 900
        seen = {k: v for k, v in seen.items() if isinstance(v, int) and v >= cutoff}
        try:
            seen[key] = int(timestamp)
        except ValueError:
            seen[key] = now
        path.write_text(json.dumps(seen, indent=2), encoding="utf-8")
        return True
