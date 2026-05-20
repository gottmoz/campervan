from __future__ import annotations

from dataclasses import dataclass, field


@dataclass(frozen=True)
class CommaNodeStatus:
    node_id: str
    device_type: str
    openpilot_running: bool | None
    agnos_version: str | None
    openpilot_version: str | None
    git_commit: str | None
    started_at: str | None
    reported_at: str
    storage_free_mb: int | None
    thermal_status: str | None
    gps_status: str | None
    camera_status: str | None
    can_seen: bool | None
    safety_mode: str


@dataclass(frozen=True)
class CommaRouteSummary:
    node_id: str
    route_id: str
    started_at: str | None
    ended_at: str | None = None
    duration_s: float | None = None
    segment_count: int | None = None
    has_camera: bool | None = None
    has_gps: bool | None = None
    has_can: bool | None = None
    uploaded_at: str = ""


@dataclass(frozen=True)
class CommaCanSummary:
    node_id: str
    route_id: str | None
    bus_count: int | None
    frame_count: int | None
    unique_arbitration_ids: int | None
    sample_ids: list[str] = field(default_factory=list)
    started_at: str | None = None
    ended_at: str | None = None
    uploaded_at: str = ""


@dataclass(frozen=True)
class CommaDiagnosticBundle:
    node_id: str
    bundle_id: str
    reported_at: str
    files: list[str] = field(default_factory=list)
    notes: str | None = None


@dataclass(frozen=True)
class CommaSensorSummary:
    gps_lock: bool
    camera_status: str
    imu_status: str
