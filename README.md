# Camper Agent

Native Android telemetry agent plus a local desktop bridge.

The first prototype is intentionally read-only on the vehicle side:

- Android app enumerates USB devices and gathers local device inventory.
- Adapter/session code models read-only capture states only.
- Desktop bridge accepts inventory, logs, status, and a narrow command allow-list.
- No arbitrary shell execution and no CAN write/control verbs.

## Layout

```text
app/      Android Kotlin app for the media unit
bridge/   Local desktop bridge for upload and command exchange
comma-node/  Read-only comma 3 companion agent
docs/     Notes and captured plans
```

## Comma 3 Node

Comma 3 / 3X is modeled as a future separate read-only logger/sensor node, not as a replacement for the Hikity head unit. It can upload status, route-log metadata, sensor summaries, and CAN summaries to the same bridge.

See [docs/comma3-integration.md](docs/comma3-integration.md).
See [docs/remote-update-plan.md](docs/remote-update-plan.md) for the remote update model.

Installed comma path:

```text
/data/camper-agent/comma-node/
```

Local staging path:

```text
E:\camper-agent\comma-node\
```

Sidecar scaffold:

```text
E:\camper-agent\nodes\comma3\
```

First PC dry-run:

```powershell
cd E:\camper-agent
python .\nodes\comma3\camper_sidecar.py --config .\nodes\comma3\config.example.json --dry-run
```

Bridge verification:

```text
GET /api/comma/latest-status
GET /api/comma/latest-route
GET /api/comma/latest-can-summary
```

Safety boundary: this is read-only metadata integration. It is not an openpilot car port, does not control the vehicle, does not write CAN, and does not expose remote shell.

Transit research:

```powershell
cd E:\camper-agent
python .\nodes\comma3\transit_research\collect_can_summary.py
python .\nodes\comma3\transit_research\collect_fingerprint.py
python .\nodes\comma3\transit_research\collect_firmware.py
python .\nodes\comma3\transit_research\summarize_transit_support.py
```

Reports:

```text
docs\openpilot-transit-support-check.md
docs\ford-transit-openpilot-gap-analysis.md
```

Remote update queue development server:

```powershell
cd E:\camper-agent
.\scripts\start-local-update-server.ps1
```

Stop it:

```powershell
cd E:\camper-agent
.\scripts\stop-local-update-server.ps1
```

The local update server keeps its queue, artifacts, and generated secrets under:

```text
E:\camper-agent\local-vps\
```

Cloudflare quick tunnel for remote access:

```powershell
cd E:\camper-agent
.\scripts\start-local-update-tunnel.ps1
```

Publish through the active tunnel:

```powershell
cd E:\camper-agent
.\scripts\publish-tunnel-update.ps1 -Target comma-3 -Version 0.1.1 -ArtifactPath .\comma-node\comma_node.py
```

Stop tunnel and local server:

```powershell
cd E:\camper-agent
.\scripts\stop-local-update-tunnel.ps1
```

Quick tunnel URLs change when restarted. Use a named Cloudflare Tunnel with a fixed domain for unattended remote updates.

VPS deploy scaffold:

```powershell
cd E:\camper-agent
$env:CAMPER_REMOTE_QUEUE_ADMIN_TOKEN="replace-with-long-random-admin-token"
$env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3="replace-with-long-random-comma-token"
$env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_HIKITY_ANDROID="replace-with-long-random-hikity-token"
.\scripts\deploy-remote-queue.ps1 -HostName <vps-ip-or-hostname> -User root -InstallNginx
```

The VPS service listens locally on `127.0.0.1:8787`; nginx proxies public HTTP traffic to it. Add TLS before exposing this as the long-term production endpoint.

Publish a signed update job:

```powershell
cd E:\camper-agent
.\scripts\start-local-update-server.ps1
.\scripts\publish-local-update.ps1 -Target comma-3 -Version 0.1.1 -ArtifactPath .\comma-node\comma_node.py
```

Vehicle nodes only stage verified artifacts in phase 1. They do not execute shell commands and do not accept CAN-write/control updates.

## Current Verification Target

1. Build or at least statically inspect the Android module.
2. Run the bridge tests.
3. Start the bridge locally and call `GET /health`.

## Build APK

This repo now includes a Gradle Wrapper. The current local setup uses a project-local Android SDK at `.android-sdk`, referenced by `local.properties`.

```powershell
cd E:\camper-agent
.\scripts\build-debug.ps1
```

Output:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Install APK

Connect the media unit with adb enabled, then run:

```powershell
cd E:\camper-agent
.\scripts\install-debug.ps1
```

## Collect First Device Inventory

```powershell
cd E:\camper-agent
.\scripts\collect-adb-inventory.ps1
```

Output is written under:

```text
logs\adb-inventory\TIMESTAMP\
```

## Android SDK Notes

If the project-local SDK is missing, install these packages with Android Studio SDK Manager:

- Android SDK Platform 35
- Android SDK Build-Tools 35.0.0
- Android SDK Platform-Tools
- Android SDK Command-line Tools

Then set `local.properties`:

```properties
sdk.dir=C\:\\Users\\jimmy\\AppData\\Local\\Android\\Sdk
```

Known limitation: the app currently inventories Android and USB surfaces only. It does not open vLinker as a serial port yet, comma node data is simulated, and there is no CAN write/control path.
