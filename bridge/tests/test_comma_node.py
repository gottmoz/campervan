import json
import tempfile
import time
import unittest
from pathlib import Path

from bridge_agent import server
from bridge_agent.security import sign_body
from bridge_agent.storage import Store
from server.api.comma_node import COMMA_CONFIG


class CommaNodeTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.old_store = server.store
        self.old_secret = server.SECRET
        server.store = Store(Path(self.tmp.name))
        server.SECRET = "secret"
        self.addCleanup(self._restore_server)

    def test_signed_status_upload_is_stored(self):
        body = json.dumps({"node_id": "comma-1", "online": True, "mode": "read-only"}).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/report-status",
            self._headers(body, "nonce-status"),
            body,
        )
        self.assertEqual(status, 200)
        self.assertTrue(payload["ok"])
        self.assertTrue(Path(payload["stored"]).exists())

    def test_unsigned_comma_upload_is_rejected(self):
        with self.assertRaises(Exception):
            server.handle_signed("/api/comma/upload-can-summary", {}, b"{}")

    def test_comma_config_is_read_only(self):
        self.assertEqual(COMMA_CONFIG["mode"], "read_only")
        self.assertFalse(COMMA_CONFIG["allow_shell"])
        self.assertFalse(COMMA_CONFIG["allow_can_write"])
        self.assertFalse(COMMA_CONFIG["allow_controls"])

    def test_unknown_control_path_is_not_added(self):
        body = b"{}"
        status, payload = server.handle_signed(
            "/api/comma/can-write",
            self._headers(body, "nonce-write"),
            body,
        )
        self.assertEqual(status, 404)
        self.assertEqual(payload["error"], "not found")

    def test_latest_status_is_read_only_payload(self):
        body = json.dumps({"node_id": "comma-1", "online": True, "mode": "read-only"}).encode("utf-8")
        server.handle_signed("/api/comma/report-status", self._headers(body, "nonce-latest"), body)
        latest = server.store.load_latest_payload("comma/status")
        self.assertIsNotNone(latest)
        self.assertEqual(latest["status"]["node_id"], "comma-1")
        self.assertEqual(latest["status"]["mode"], "read-only")

    def test_signed_can_summary_upload_is_stored(self):
        body = json.dumps({
            "node_id": "comma-1",
            "route_id": "none",
            "bus_count": 0,
            "frame_count": 0,
            "arbitration_ids": [],
            "read_only": True,
        }).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/upload-can-summary",
            self._headers(body, "nonce-can-summary"),
            body,
        )
        self.assertEqual(status, 200)
        latest = server.store.load_latest_payload("comma/can")
        self.assertIsNotNone(latest)
        self.assertTrue(latest["status"]["read_only"])
        self.assertEqual(latest["status"]["frame_count"], 0)

    def _headers(self, body: bytes, nonce: str) -> dict[str, str]:
        timestamp = str(int(time.time()))
        return {
            "x-agent-id": "comma-1",
            "x-timestamp": timestamp,
            "x-nonce": nonce,
            "x-signature": sign_body("secret", timestamp, nonce, body),
        }

    def _restore_server(self):
        server.store = self.old_store
        server.SECRET = self.old_secret


if __name__ == "__main__":
    unittest.main()
