# Camper Agent Desktop Bridge

Small Windows-friendly bridge for the head-unit agent. It accepts signed agent reports, stores them under `data/`, and only returns allow-listed command verbs.

## Start

```powershell
cd E:\camper-agent\bridge
$env:CAMPER_BRIDGE_SECRET = "replace-with-shared-secret"
$env:CAMPER_BRIDGE_HOST = "127.0.0.1"
$env:CAMPER_BRIDGE_PORT = "8765"
python -m bridge_agent.server
```

FastAPI/uvicorn is used if installed. Without those packages, the same command falls back to Python's standard-library HTTP server.

Optional install:

```powershell
python -m pip install -r requirements.txt
```

## Environment

- `CAMPER_BRIDGE_SECRET`: shared HMAC secret. Change this from the dev default.
- `CAMPER_BRIDGE_HOST`: bind host, default `127.0.0.1`.
- `CAMPER_BRIDGE_PORT`: bind port, default `8765`.
- `CAMPER_BRIDGE_DATA`: storage directory, default `E:\camper-agent\bridge\data`.

## Signed Requests

Every `POST` requires:

- `X-Agent-Id`
- `X-Timestamp`: Unix seconds, accepted within 5 minutes.
- `X-Nonce`
- `X-Signature`: `hex(HMAC-SHA256(secret, timestamp + "." + nonce + "." + raw_body))`

Endpoints:

- `GET /health`
- `POST /api/agent/report-inventory`
- `POST /api/agent/upload-log`
- `POST /api/agent/pull-commands`
- `POST /api/agent/self-update/status`
- `POST /api/comma/report-status`
- `POST /api/comma/upload-route-log`
- `POST /api/comma/upload-can-summary`
- `GET /api/comma/config`

Comma endpoints use the same HMAC headers. They are read-only upload/config endpoints for a future comma 3 node and do not accept shell, CAN-write, or vehicle-control commands.

Allowed command verbs are:

```text
upload.logs
rotate.capture
set.scan.profile
update.agent
restart.service
export.inventory
```

To queue commands, create `data\commands.json`:

```json
{
  "commands": [
    {"verb": "rotate.capture"},
    {"verb": "export.inventory"}
  ]
}
```

Unknown verbs are filtered out before the bridge responds.
