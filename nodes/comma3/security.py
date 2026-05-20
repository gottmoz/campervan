from __future__ import annotations

import hashlib
import hmac
import time
import uuid


def sign_body(secret: str, timestamp: str, nonce: str, body: bytes) -> str:
    message = timestamp.encode("utf-8") + b"." + nonce.encode("utf-8") + b"." + body
    return hmac.new(secret.encode("utf-8"), message, hashlib.sha256).hexdigest()


def signed_headers(node_id: str, secret: str, body: bytes) -> dict[str, str]:
    timestamp = str(int(time.time()))
    nonce = uuid.uuid4().hex
    return {
        "content-type": "application/json",
        "x-agent-id": node_id,
        "x-timestamp": timestamp,
        "x-nonce": nonce,
        "x-signature": sign_body(secret, timestamp, nonce, body),
    }
