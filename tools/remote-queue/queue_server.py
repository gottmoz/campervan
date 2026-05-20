#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import unquote, urlparse


ROOT = Path(os.environ.get("CAMPER_REMOTE_QUEUE_DATA", Path(__file__).resolve().parent / "data"))
ADMIN_TOKEN = os.environ.get("CAMPER_REMOTE_QUEUE_ADMIN_TOKEN", "")
NODE_TOKENS = {
    key.removeprefix("CAMPER_REMOTE_QUEUE_NODE_TOKEN_").lower().replace("_", "-"): value
    for key, value in os.environ.items()
    if key.startswith("CAMPER_REMOTE_QUEUE_NODE_TOKEN_")
}


class QueueStore:
    def __init__(self, root: Path):
        self.root = root
        (root / "artifacts").mkdir(parents=True, exist_ok=True)
        (root / "jobs").mkdir(parents=True, exist_ok=True)
        (root / "status").mkdir(parents=True, exist_ok=True)

    def save_job(self, payload: dict) -> Path:
        target = str(payload.get("target", "unknown"))
        job_id = str(payload.get("job_id", "unknown"))
        path = self.root / "jobs" / f"{target}-{job_id}.json"
        path.write_text(json.dumps(payload, indent=2, sort_keys=True), encoding="utf-8")
        return path

    def jobs_for(self, target: str) -> list[dict]:
        jobs: list[dict] = []
        for path in sorted((self.root / "jobs").glob(f"{target}-*.json")):
            data = json.loads(path.read_text(encoding="utf-8"))
            if isinstance(data, dict):
                jobs.append(data)
        return jobs

    def save_status(self, job_id: str, payload: dict) -> Path:
        path = self.root / "status" / f"{job_id}.jsonl"
        with path.open("a", encoding="utf-8") as handle:
            handle.write(json.dumps(payload, separators=(",", ":"), sort_keys=True) + "\n")
        return path


store = QueueStore(ROOT)


class Handler(BaseHTTPRequestHandler):
    def _json(self, status: int, payload: dict) -> None:
        raw = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("content-type", "application/json")
        self.send_header("content-length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def do_GET(self) -> None:
        parts = [part for part in urlparse(self.path).path.split("/") if part]
        if parts == ["health"]:
            self._json(200, {"ok": True})
            return
        if len(parts) == 2 and parts[0] == "artifacts":
            self._artifact(parts[1])
            return
        if len(parts) == 3 and parts[0] == "nodes" and parts[2] == "jobs":
            if not self._node_authorized(parts[1]):
                self._json(401, {"error": "unauthorized"})
                return
            self._json(200, {"jobs": store.jobs_for(parts[1])})
            return
        self._json(404, {"error": "not found"})

    def _artifact(self, raw_name: str) -> None:
        name = Path(unquote(raw_name)).name
        path = (ROOT / "artifacts" / name).resolve()
        artifact_root = (ROOT / "artifacts").resolve()
        if artifact_root not in path.parents or not path.is_file():
            self._json(404, {"error": "not found"})
            return
        data = path.read_bytes()
        self.send_response(200)
        self.send_header("content-type", "application/octet-stream")
        self.send_header("content-length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def do_POST(self) -> None:
        length = int(self.headers.get("content-length", "0"))
        try:
            payload = json.loads(self.rfile.read(length).decode("utf-8"))
        except json.JSONDecodeError:
            self._json(400, {"error": "invalid json"})
            return
        parts = [part for part in urlparse(self.path).path.split("/") if part]
        if parts == ["jobs"]:
            if not self._admin_authorized():
                self._json(401, {"error": "unauthorized"})
                return
            path = store.save_job(payload)
            self._json(200, {"ok": True, "stored": str(path)})
            return
        if len(parts) == 3 and parts[0] == "jobs" and parts[2] == "status":
            node_id = str(payload.get("target") or payload.get("node_id") or "")
            if not (self._admin_authorized() or self._node_authorized(node_id)):
                self._json(401, {"error": "unauthorized"})
                return
            path = store.save_status(parts[1], payload)
            self._json(200, {"ok": True, "stored": str(path)})
            return
        self._json(404, {"error": "not found"})

    def _admin_authorized(self) -> bool:
        if not ADMIN_TOKEN:
            return False
        return self.headers.get("x-admin-token", "") == ADMIN_TOKEN

    def _node_authorized(self, node_id: str) -> bool:
        expected = NODE_TOKENS.get(node_id)
        if not expected:
            return False
        return self.headers.get("x-node-token", "") == expected

    def log_message(self, fmt: str, *args: object) -> None:
        print("%s - %s" % (self.address_string(), fmt % args))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default=os.environ.get("CAMPER_REMOTE_QUEUE_HOST", "127.0.0.1"))
    parser.add_argument("--port", default=int(os.environ.get("CAMPER_REMOTE_QUEUE_PORT", "8787")), type=int)
    args = parser.parse_args()
    server = ThreadingHTTPServer((args.host, args.port), Handler)
    print(f"remote queue listening on http://{args.host}:{args.port}")
    server.serve_forever()


if __name__ == "__main__":
    main()
