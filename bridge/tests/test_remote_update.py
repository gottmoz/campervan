import time
import unittest

from bridge_agent.remote_update import (
    RemoteUpdateError,
    UpdateManifest,
    signed_manifest,
    verify_signed_manifest,
)


class RemoteUpdateTests(unittest.TestCase):
    def test_signed_manifest_verifies(self):
        manifest = UpdateManifest(
            job_id="job-1",
            target="comma-3",
            version="0.1.1",
            artifact_url="https://updates.example/artifact.zip",
            artifact_sha256="a" * 64,
            verb="update.agent",
            expires_at=int(time.time()) + 3600,
            nonce="n1",
            sequence=1,
        )
        signed = signed_manifest(manifest, "admin-secret")
        verified = verify_signed_manifest("admin-secret", signed)
        self.assertEqual(verified["target"], "comma-3")

    def test_rejects_shell_verb(self):
        manifest = UpdateManifest(
            job_id="job-2",
            target="comma-3",
            version="0.1.1",
            artifact_url="https://updates.example/artifact.zip",
            artifact_sha256="b" * 64,
            verb="shell",
            expires_at=int(time.time()) + 3600,
            nonce="n2",
            sequence=2,
        )
        with self.assertRaises(RemoteUpdateError):
            signed_manifest(manifest, "admin-secret")

    def test_rejects_tampered_manifest(self):
        manifest = UpdateManifest(
            job_id="job-3",
            target="hikity-android",
            version="0.1.1",
            artifact_url="https://updates.example/app.apk",
            artifact_sha256="c" * 64,
            verb="update.agent",
            expires_at=int(time.time()) + 3600,
            nonce="n3",
            sequence=3,
        )
        signed = signed_manifest(manifest, "admin-secret")
        signed["artifact_sha256"] = "d" * 64
        with self.assertRaises(RemoteUpdateError):
            verify_signed_manifest("admin-secret", signed)


if __name__ == "__main__":
    unittest.main()
