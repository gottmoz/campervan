from __future__ import annotations

import argparse
import json
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Callable

from .policy import filter_allowed_commands
from .security import Envelope, SignatureError, verify_envelope
from .storage import Store
from server.api.comma_node import handle_comma_signed, is_comma_path


ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = Path(os.environ.get("CAMPER_BRIDGE_DATA", ROOT / "data"))
SECRET = os.environ.get("CAMPER_BRIDGE_SECRET", "dev-change-me")
HOST = os.environ.get("CAMPER_BRIDGE_HOST", "127.0.0.1")
PORT = int(os.environ.get("CAMPER_BRIDGE_PORT", "8765"))


store = Store(DATA_DIR)


def latest_transit_research_payload() -> dict | None:
    return store.load_latest_payload("comma/transit_research")


def _headers_to_envelope(headers: dict | object) -> Envelope:
    get: Callable[[str, str], str]
    if isinstance(headers, dict):
        get = headers.get
    else:
        get = headers.get  # type: ignore[assignment]
    return Envelope(
        agent_id=get("x-agent-id", ""),
        timestamp=get("x-timestamp", ""),
        nonce=get("x-nonce", ""),
        signature=get("x-signature", ""),
    )


def handle_signed(path: str, headers: dict | object, body: bytes) -> tuple[int, dict]:
    envelope = _headers_to_envelope(headers)
    verify_envelope(SECRET, envelope, body)
    if not store.remember_nonce(envelope.agent_id, envelope.nonce, envelope.timestamp):
        raise SignatureError("replayed nonce")
    if is_comma_path(path):
        return handle_comma_signed(path, store, envelope.agent_id, body)
    if path == "/api/agent/report-inventory":
        saved = store.save_payload("inventory", envelope.agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/agent/upload-log":
        saved = store.save_payload("logs", envelope.agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/agent/self-update/status":
        saved = store.save_payload("status", envelope.agent_id, body)
        return 200, {"ok": True, "stored": str(saved)}
    if path == "/api/agent/pull-commands":
        return 200, {"commands": filter_allowed_commands(store.load_commands())}
    return 404, {"error": "not found"}


def create_fastapi_app():
    from fastapi import FastAPI, Header, Request
    from fastapi.responses import JSONResponse

    app = FastAPI(title="camper-agent desktop bridge")

    @app.get("/health")
    def health():
        return {"ok": True}

    @app.get("/api/comma/latest-status")
    def latest_comma_status():
        latest = store.load_latest_payload("comma/status")
        if latest is None:
            return JSONResponse({"error": "not found"}, status_code=404)
        return latest

    @app.get("/api/comma/latest-route")
    def latest_comma_route():
        latest = store.load_latest_payload("comma/routes")
        if latest is None:
            return JSONResponse({"error": "not found"}, status_code=404)
        return latest

    @app.get("/api/comma/latest-can-summary")
    def latest_comma_can_summary():
        latest = store.load_latest_payload("comma/can")
        if latest is None:
            return JSONResponse({"error": "not found"}, status_code=404)
        return latest

    @app.get("/api/comma/transit-research/latest")
    def latest_transit_research():
        latest = latest_transit_research_payload()
        if latest is None:
            return JSONResponse({"error": "not found"}, status_code=404)
        return latest

    async def signed_endpoint(
        request: Request,
        x_agent_id: str = Header(default=""),
        x_timestamp: str = Header(default=""),
        x_nonce: str = Header(default=""),
        x_signature: str = Header(default=""),
    ):
        body = await request.body()
        headers = {
            "x-agent-id": x_agent_id,
            "x-timestamp": x_timestamp,
            "x-nonce": x_nonce,
            "x-signature": x_signature,
        }
        try:
            status, payload = handle_signed(request.url.path, headers, body)
        except SignatureError as exc:
            return JSONResponse({"error": str(exc)}, status_code=401)
        return JSONResponse(payload, status_code=status)

    app.post("/api/agent/report-inventory")(signed_endpoint)
    app.post("/api/agent/upload-log")(signed_endpoint)
    app.post("/api/agent/pull-commands")(signed_endpoint)
    app.post("/api/agent/self-update/status")(signed_endpoint)
    app.post("/api/comma/report-status")(signed_endpoint)
    app.post("/api/comma/upload-route-log")(signed_endpoint)
    app.post("/api/comma/upload-route-summary")(signed_endpoint)
    app.post("/api/comma/upload-can-summary")(signed_endpoint)
    app.post("/api/comma/upload-diagnostic-bundle")(signed_endpoint)
    app.post("/api/comma/transit-research/upload-can-summary")(signed_endpoint)
    app.post("/api/comma/transit-research/upload-fingerprint")(signed_endpoint)
    app.post("/api/comma/transit-research/upload-fw-summary")(signed_endpoint)
    app.get("/api/comma/config")(signed_endpoint)
    return app


class Handler(BaseHTTPRequestHandler):
    def _json(self, status: int, payload: dict) -> None:
        raw = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("content-type", "application/json")
        self.send_header("content-length", str(len(raw)))
        self.end_headers()
        self.wfile.write(raw)

    def do_GET(self) -> None:
        if self.path == "/health":
            self._json(200, {"ok": True})
        elif self.path == "/api/comma/latest-status":
            latest = store.load_latest_payload("comma/status")
            if latest is None:
                self._json(404, {"error": "not found"})
            else:
                self._json(200, latest)
        elif self.path == "/api/comma/latest-route":
            latest = store.load_latest_payload("comma/routes")
            if latest is None:
                self._json(404, {"error": "not found"})
            else:
                self._json(200, latest)
        elif self.path == "/api/comma/latest-can-summary":
            latest = store.load_latest_payload("comma/can")
            if latest is None:
                self._json(404, {"error": "not found"})
            else:
                self._json(200, latest)
        elif self.path == "/api/comma/transit-research/latest":
            latest = latest_transit_research_payload()
            if latest is None:
                self._json(404, {"error": "not found"})
            else:
                self._json(200, latest)
        elif self.path == "/api/comma/config":
            try:
                status, payload = handle_signed(self.path, self.headers, b"")
            except SignatureError as exc:
                status, payload = 401, {"error": str(exc)}
            self._json(status, payload)
        else:
            self._json(404, {"error": "not found"})

    def do_POST(self) -> None:
        length = int(self.headers.get("content-length", "0"))
        body = self.rfile.read(length)
        try:
            status, payload = handle_signed(self.path, self.headers, body)
        except SignatureError as exc:
            status, payload = 401, {"error": str(exc)}
        self._json(status, payload)

    def log_message(self, fmt: str, *args: object) -> None:
        print("%s - %s" % (self.address_string(), fmt % args))


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--stdlib", action="store_true", help="force standard-library HTTP server")
    args = parser.parse_args()
    if not args.stdlib:
        try:
            import uvicorn

            uvicorn.run(create_fastapi_app(), host=HOST, port=PORT)
            return
        except ImportError:
            pass
    server = ThreadingHTTPServer((HOST, PORT), Handler)
    print(f"camper bridge listening on http://{HOST}:{PORT}")
    server.serve_forever()


if __name__ == "__main__":
    main()
