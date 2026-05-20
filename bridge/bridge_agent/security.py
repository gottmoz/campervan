from __future__ import annotations

import hashlib
import hmac
import time
from dataclasses import dataclass


class SignatureError(ValueError):
    pass


@dataclass(frozen=True)
class Envelope:
    agent_id: str
    timestamp: str
    nonce: str
    signature: str


def canonical_message(timestamp: str, nonce: str, body: bytes) -> bytes:
    return timestamp.encode("utf-8") + b"." + nonce.encode("utf-8") + b"." + body


def sign_body(secret: str, timestamp: str, nonce: str, body: bytes) -> str:
    return hmac.new(
        secret.encode("utf-8"),
        canonical_message(timestamp, nonce, body),
        hashlib.sha256,
    ).hexdigest()


def verify_envelope(
    secret: str,
    envelope: Envelope,
    body: bytes,
    *,
    now: int | None = None,
    max_skew_seconds: int = 300,
) -> None:
    if not secret:
        raise SignatureError("missing shared secret")
    if not envelope.agent_id or not envelope.timestamp or not envelope.nonce or not envelope.signature:
        raise SignatureError("missing signature headers")
    try:
        ts = int(envelope.timestamp)
    except ValueError as exc:
        raise SignatureError("invalid timestamp") from exc
    current = int(time.time()) if now is None else now
    if abs(current - ts) > max_skew_seconds:
        raise SignatureError("timestamp outside allowed window")
    expected = sign_body(secret, envelope.timestamp, envelope.nonce, body)
    if not hmac.compare_digest(expected, envelope.signature):
        raise SignatureError("bad signature")

