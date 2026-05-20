from __future__ import annotations

import argparse
import json
import time
from pathlib import Path

from can_summary import collect_can_summary
from log_probe import collect_route_summary
from system_probe import collect_system_status
from uploader import is_read_only_config, signed_get_config, signed_post


def load_config(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def run_once(config: dict) -> list[dict]:
    node_id = config["node_id"]
    bridge_url = config["bridge_url"]
    secret = config["shared_secret"]
    results: list[dict] = []

    remote_config = signed_get_config(bridge_url, node_id, secret)
    if not is_read_only_config(remote_config):
        return [{"warning": "ignored non-read-only remote config"}]

    if config.get("upload_status", True):
        status = collect_system_status(node_id)
        results.append(signed_post(bridge_url, "/api/comma/report-status", node_id, secret, status))

    route = collect_route_summary(node_id)
    if config.get("upload_route_summary", True) and route is not None:
        results.append(signed_post(bridge_url, "/api/comma/upload-route-summary", node_id, secret, route))

    if config.get("upload_can_summary", False):
        route_id = route.get("route_id") if isinstance(route, dict) else None
        results.append(signed_post(bridge_url, "/api/comma/upload-can-summary", node_id, secret, collect_can_summary(node_id, route_id)))

    return results


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", default="config.json")
    parser.add_argument("--dry-run", action="store_true")
    parser.add_argument("--once", action="store_true")
    args = parser.parse_args()

    config = load_config(Path(args.config))
    if config.get("mode") != "read_only":
        raise SystemExit("comma sidecar only supports mode=read_only")

    while True:
        result = run_once(config)
        if args.dry_run or args.once:
            print(json.dumps({"ok": True, "results": result}, indent=2, sort_keys=True))
            return
        time.sleep(int(config.get("upload_interval_seconds", 30)))


if __name__ == "__main__":
    main()
