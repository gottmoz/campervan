import json
import tempfile
import time
import unittest
from pathlib import Path

from bridge_agent import server
from bridge_agent.security import sign_body
from bridge_agent.storage import Store


class TransitResearchSecurityTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.old_store = server.store
        self.old_secret = server.SECRET
        server.store = Store(Path(self.tmp.name))
        server.SECRET = "secret"
        self.addCleanup(self._restore_server)

    def test_upload_without_hmac_is_rejected(self):
        with self.assertRaises(Exception):
            server.handle_signed("/api/comma/transit-research/upload-can-summary", {}, b"{}")

    def test_upload_with_hmac_is_stored(self):
        body = json.dumps(self._safe_payload()).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/transit-research/upload-can-summary",
            self._headers(body, "nonce-can"),
            body,
        )
        self.assertEqual(status, 200)
        self.assertTrue(Path(payload["stored"]).exists())

    def test_can_write_true_is_rejected(self):
        body = json.dumps(self._safe_payload(can_write=True)).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/transit-research/upload-can-summary",
            self._headers(body, "nonce-write"),
            body,
        )
        self.assertEqual(status, 400)
        self.assertEqual(payload["error"], "forbidden field")

    def test_allow_controls_true_is_rejected(self):
        body = json.dumps(self._safe_payload(allow_controls=True)).encode("utf-8")
        status, payload = server.handle_signed(
            "/api/comma/transit-research/upload-fingerprint",
            self._headers(body, "nonce-controls"),
            body,
        )
        self.assertEqual(status, 400)

    def test_shell_command_exec_fields_are_rejected(self):
        for key in ("command", "shell", "exec"):
            with self.subTest(key=key):
                body = json.dumps(self._safe_payload(**{key: "whoami"})).encode("utf-8")
                status, payload = server.handle_signed(
                    "/api/comma/transit-research/upload-fw-summary",
                    self._headers(body, f"nonce-{key}"),
                    body,
                )
                self.assertEqual(status, 400)
                self.assertEqual(payload["error"], "forbidden field")

    def test_latest_returns_data_only(self):
        body = json.dumps(self._safe_payload()).encode("utf-8")
        server.handle_signed(
            "/api/comma/transit-research/upload-can-summary",
            self._headers(body, "nonce-latest"),
            body,
        )
        latest = server.latest_transit_research_payload()
        self.assertEqual(latest["status"]["safety"]["read_only"], True)
        self.assertNotIn("commands", latest)

    def _safe_payload(self, **extra):
        payload = {
            "vehicle": "Ford Transit Custom Camper",
            "vin": "WFOFXXTTGFGD20380",
            "source": "dry_run",
            "buses": [],
            "safety": {"read_only": True, "can_write_attempted": False},
        }
        payload.update(extra)
        return payload

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
