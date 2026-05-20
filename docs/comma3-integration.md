# Comma 3 Integration

## Role

Comma 3 / 3X is a separate vehicle intelligence node, not a replacement for the Hikity Android media unit.

```text
Hikity Android stereo = main screen, camper GUI, user interface
comma 3              = Linux-based logger, camera, GPS/IMU, CAN observer
vLinker FS           = simple OBD/HS-MS CAN adapter for the Android app
EmpirBus Logix       = separate camper/control domain, handled cautiously
Desktop bridge       = log intake, analysis, updates, Codex workflow
```

## Phase 1 Data

The comma node is read-only in phase 1. Expected uploads are:

- status: online/offline, software mode, last sync
- route summaries: route and segment metadata
- sensor metadata: GPS lock, camera status, IMU status
- CAN summaries: bus count, frame count, observed arbitration IDs
- diagnostic bundles: file metadata only

Raw camera data and full route archives should be handled as files later. The first bridge API only records signed metadata/log payloads.

## Bridge API

Signed comma endpoints:

```text
POST /api/comma/report-status
POST /api/comma/upload-route-summary
POST /api/comma/upload-can-summary
POST /api/comma/upload-diagnostic-bundle
GET  /api/comma/config
```

Local GUI read endpoints:

```text
GET /api/comma/latest-status
GET /api/comma/latest-route
GET /api/comma/latest-can-summary
```

Bridge storage:

```text
bridge/data/comma/status/
bridge/data/comma/routes/
bridge/data/comma/can/
bridge/data/comma/diagnostics/
```

## Safety Model

- No gas, brake, steering, or actuator commands.
- No CAN write/control endpoints.
- No generic shell endpoint.
- Same HMAC envelope as the Android agent bridge endpoints.
- Comma data is treated as observational evidence until validated against real hardware logs.
- Sidecar reads metadata only and ignores any non-read-only remote config.
- Diagnostic bundles contain filenames/notes only, not executable instructions.

Forbidden in phase 1:

```text
controls, CAN write, openpilot carcontroller changes, remote shell,
steer/brake/throttle automation, EmpirBus control from comma
```

## Relationship To Other Nodes

Hikity remains the cockpit GUI and camper dashboard. It can show comma node health, but it should not become a remote-control surface for openpilot or vehicle actuation.

vLinker FS remains the Android-side USB/OBD adapter for simple inventory and read-only capture.

EmpirBus remains a separate domain. Do not infer that Ford OBD, comma harness CAN, and EmpirBus/NMEA 2000 are the same bus or have the same safety profile.

The desktop bridge stores comma uploads beside Android uploads so Codex can analyze both streams later.

## Phase Plan

1. Status reporting: show comma as a separate node in bridge and Android GUI.
2. Route/log summaries: import metadata without raw video upload.
3. Compare Android/vLinker and comma observations on a shared timeline.
4. Optional openpilot fork sidecar, still read-only and outside controls.

## Ford Transit Research

The Transit research module lives in:

```text
nodes/comma3/transit_research/
```

It is for dashcam/read-only support investigation only. It records CAN summaries, fingerprint placeholders, firmware summaries and gap-analysis docs. It does not perform CAN write, UDS write, steering, brake, throttle, panda safety or openpilot controls work.
