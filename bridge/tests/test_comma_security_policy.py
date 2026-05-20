import json
import tempfile
import time
import unittest
from pathlib import Path

from bridge_agent import server
from bridge_agent.security import sign_body
from bridge_agent.storage import Store


FORBIDDEN_FIELDS = ("shell", "command", "exec", "can_write", "control", "steer", "brake", "throttle")


class CommaSecurityPolicyTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.old_store = server.store
        self.old_secret = server.SECRET
        server.store = Store(Path(self.tmp.name))
        server.SECRET = "secret"
        self.addCleanup(self._restore_server)

    def test_post_without_signature_is_rejected(self):
        with self.assertRaises(Exception):
            server.handle_signed("/api/comma/report-status", {}, b"{}")

    def test_post_with_invalid_signature_is_rejected(self):
        body = b"{}"
        headers = self._headers(body, "nonce-bad")
        headers["x-signature"] = "bad"
        with self.assertRaises(Exception):
            server.handle_signed("/api/comma/report-status", headers, body)

    def test_config_is_explicitly_read_only(self):
        status, payload = server.handle_signed("/api/comma/config", self._headers(b"", "nonce-config"), b"")
        self.assertEqual(status, 200)
        self.assertEqual(payload["mode"], "read_only")
        self.assertFalse(payload["allow_shell"])
        self.assertFalse(payload["allow_can_write"])
        self.assertFalse(payload["allow_controls"])

    def test_forbidden_fields_are_rejected_on_comma_posts(self):
        for field in FORBIDDEN_FIELDS:
            with self.subTest(field=field):
                body = json.dumps({"node_id": "comma3-main", field: True}).encode("utf-8")
                status, payload = server.handle_signed(
                    "/api/comma/report-status",
                    self._headers(body, f"nonce-{field}"),
                    body,
                )
                self.assertEqual(status, 400)
                self.assertEqual(payload["error"], "forbidden field")

    def test_diagnostic_bundle_rejects_command_metadata(self):
        body = json.dumps({
            "node_id": "comma3-main",
            "bundle_id": "b1",
            "reported_at": "2026-05-19T00:00:00Z",
            "files": ["log.txt"],
            "command": "reboot",
        }).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/upload-diagnostic-bundle",
            self._headers(body, "nonce-diag"),
            body,
        )
        self.assertEqual(status, 400)
        self.assertEqual(payload["error"], "forbidden field")

    def test_route_summary_and_latest_route_round_trip(self):
        body = json.dumps({
            "node_id": "comma3-main",
            "route_id": "route-1",
            "uploaded_at": "2026-05-19T00:00:00Z",
        }).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/upload-route-summary",
            self._headers(body, "nonce-route"),
            body,
        )
        self.assertEqual(status, 200)
        self.assertTrue(Path(payload["stored"]).exists())
        latest = server.store.load_latest_payload("comma/routes")
        self.assertEqual(latest["status"]["route_id"], "route-1")

    def _headers(self, body: bytes, nonce: str) -> dict[str, str]:
        timestamp = str(int(time.time()))
        return {
            "x-agent-id": "comma3-main",
            "x-timestamp": timestamp,
            "x-nonce": nonce,
            "x-signature": sign_body("secret", timestamp, nonce, body),
        }

    def _restore_server(self):
        server.store = self.old_store
        server.SECRET = self.old_secret


if __name__ == "__main__":
    unittest.main()
