import json
import time
import unittest
from pathlib import Path

from bridge_agent.policy import filter_allowed_commands
from bridge_agent.security import Envelope, SignatureError, sign_body, verify_envelope
from bridge_agent.storage import Store


class SecurityPolicyTests(unittest.TestCase):
    def test_valid_signature(self):
        body = json.dumps({"ok": True}).encode("utf-8")
        timestamp = str(int(time.time()))
        signature = sign_body("secret", timestamp, "nonce-1", body)
        verify_envelope("secret", Envelope("agent", timestamp, "nonce-1", signature), body)

    def test_bad_signature_rejected(self):
        body = b"{}"
        timestamp = str(int(time.time()))
        with self.assertRaises(SignatureError):
            verify_envelope("secret", Envelope("agent", timestamp, "nonce-1", "bad"), body)

    def test_policy_filters_unknown_verbs(self):
        commands = [
            {"verb": "upload.logs"},
            {"verb": "shell", "args": ["whoami"]},
            {"verb": "restart.service"},
        ]
        self.assertEqual(
            filter_allowed_commands(commands),
            [{"verb": "upload.logs"}, {"verb": "restart.service"}],
        )

    def test_nonce_replay_detected(self):
        store = Store(Path(self._tmpdir()))
        self.assertTrue(store.remember_nonce("agent", "n1", str(int(time.time()))))
        self.assertFalse(store.remember_nonce("agent", "n1", str(int(time.time()))))

    def _tmpdir(self):
        import shutil
        import tempfile

        path = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, path)
        return path


if __name__ == "__main__":
    unittest.main()
