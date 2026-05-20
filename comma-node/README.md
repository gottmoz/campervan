# Camper Agent Comma Node

Read-only companion agent for a comma 3 / 3X device.

This does not modify openpilot and does not send vehicle-control or CAN-write commands. It only collects local status metadata and can upload signed status to the desktop bridge.

## Install Location

```text
/data/camper-agent/comma-node/
```

## Configure

Copy `config.example.json` to `config.json` on the comma and set:

- `bridge_url`: desktop bridge base URL, for example `http://192.168.50.10:8765`
- `shared_secret`: same HMAC secret as `CAMPER_BRIDGE_SECRET`

## Run

```bash
cd /data/camper-agent/comma-node
python3 comma_node.py --once
```

Periodic upload:

```bash
cd /data/camper-agent/comma-node
./start_periodic.sh
tail -f comma_node.log
```

Stop it:

```bash
cd /data/camper-agent/comma-node
./stop_periodic.sh
```

The CAN check uses openpilot's read-only `cereal.messaging` subscription to the `can` service. It does not open panda directly and does not publish `sendcan`.

## Remote Updates

Remote update polling is disabled by default. When enabled, the node polls a queue URL, verifies signed manifests, downloads artifacts, verifies SHA-256, and stages them under:

```text
/data/camper-agent/staged-updates/
```

It does not execute shell commands and does not apply CAN-write/control updates.
