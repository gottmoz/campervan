from __future__ import annotations

import time


def collect_can_summary(node_id: str, route_id: str | None = None) -> dict:
    return {
        "node_id": node_id,
        "route_id": route_id,
        "bus_count": 0,
        "frame_count": 0,
        "unique_arbitration_ids": 0,
        "sample_ids": [],
        "started_at": None,
        "ended_at": None,
        "uploaded_at": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "read_only": True,
    }
