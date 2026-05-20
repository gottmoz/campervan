from __future__ import annotations

import argparse
import json
import time
from collections import Counter, defaultdict
from pathlib import Path


def load_config(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def summarize(config: dict, input_path: Path | None) -> dict:
    buses: dict[int, Counter[str]] = defaultdict(Counter)
    source = "dry_run"
    if input_path is not None:
        source = "file"
        for line in input_path.read_text(encoding="utf-8").splitlines():
            if not line.strip():
                continue
            frame = json.loads(line)
            bus = int(frame.get("bus", 0))
            arb_id = frame.get("id", frame.get("address", frame.get("arbitration_id", "unknown")))
            buses[bus][normalize_id(arb_id)] += 1

    return {
        "vehicle": config["vehicle_name"],
        "vin": config["vin"],
        "captured_at": now(),
        "source": source,
        "buses": [
            {
                "bus": bus,
                "frame_count": sum(counts.values()),
                "unique_ids": len(counts),
                "sample_ids": list(counts.keys())[:16],
                "top_ids_by_frequency": [
                    {"id": arb_id, "frames": count}
                    for arb_id, count in counts.most_common(20)
                ],
            }
            for bus, counts in sorted(buses.items())
        ],
        "safety": {
            "read_only": True,
            "can_write_attempted": False,
        },
    }


def normalize_id(value: object) -> str:
    if isinstance(value, int):
        return f"0x{value:X}"
    text = str(value)
    if text.startswith("0x"):
        return text
    try:
        return f"0x{int(text):X}"
    except ValueError:
        return text


def now() -> str:
    return time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime())


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--config", default="nodes/comma3/transit_research/config.example.json")
    parser.add_argument("--input")
    parser.add_argument("--output")
    args = parser.parse_args()

    config = load_config(Path(args.config))
    if config.get("mode") != "read_only" or config.get("allow_can_write") or config.get("allow_controls"):
        raise SystemExit("Transit research must be read-only with controls disabled")
    output_dir = Path(config.get("output_dir", "data/comma/transit_research"))
    output_dir.mkdir(parents=True, exist_ok=True)
    output = Path(args.output) if args.output else output_dir / f"can_summary_{int(time.time())}.json"
    payload = summarize(config, Path(args.input) if args.input else None)
    output.write_text(json.dumps(payload, indent=2, sort_keys=True), encoding="utf-8")
    print(output)


if __name__ == "__main__":
    main()
