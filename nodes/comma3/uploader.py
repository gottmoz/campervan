from __future__ import annotations

import json
import urllib.request

from security import signed_headers


def signed_post(bridge_url: str, path: str, node_id: str, secret: str, payload: dict) -> dict:
    body = json.dumps(payload, separators=(",", ":"), sort_keys=True).encode("utf-8")
    request = urllib.request.Request(
        bridge_url.rstrip("/") + path,
        data=body,
        method="POST",
        headers=signed_headers(node_id, secret, body),
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def signed_get_config(bridge_url: str, node_id: str, secret: str) -> dict:
    body = b""
    request = urllib.request.Request(
        bridge_url.rstrip("/") + "/api/comma/config",
        method="GET",
        headers=signed_headers(node_id, secret, body),
    )
    with urllib.request.urlopen(request, timeout=10) as response:
        return json.loads(response.read().decode("utf-8"))


def is_read_only_config(config: dict) -> bool:
    return (
        config.get("mode") == "read_only"
        and config.get("allow_shell") is False
        and config.get("allow_can_write") is False
        and config.get("allow_controls") is False
    )
