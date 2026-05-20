from __future__ import annotations

import argparse
import json
import time
from pathlib import Path


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", default="nodes/comma3/transit_research/config.example.json")
    parser.add_argument("--output")
    args = parser.parse_args()
    config = json.loads(Path(args.config).read_text(encoding="utf-8"))
    output_dir = Path(config.get("output_dir", "data/comma/transit_research"))
    output_dir.mkdir(parents=True, exist_ok=True)
    output = Path(args.output) if args.output else output_dir / f"firmware_{int(time.time())}.json"
    payload = {
        "vehicle": config["vehicle_name"],
        "vin": config["vin"],
        "captured_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "source": "dry_run",
        "firmware": [],
        "allow_firmware_query": bool(config.get("allow_firmware_query", True)),
        "notes": "No firmware query is performed in dry-run.",
        "safety": {"read_only": True, "uds_write_attempted": False},
    }
    output.write_text(json.dumps(payload, indent=2, sort_keys=True), encoding="utf-8")
    print(output)


if __name__ == "__main__":
    main()
