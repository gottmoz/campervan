# Comma 3 Sidecar

Read-only sidecar scaffold for comma 3 / openpilot devices.

It reports status and route metadata to the Camper Agent bridge. It does not send CAN, does not run remote shell commands, and does not touch openpilot controls.

First dry-run on a PC:

```powershell
cd E:\camper-agent
python .\nodes\comma3\camper_sidecar.py --config .\nodes\comma3\config.example.json --dry-run
```

On a comma device, copy this directory, create `config.json`, and set:

- `bridge_url`
- `shared_secret`
- `node_id`

Keep `mode` set to `read_only`.
