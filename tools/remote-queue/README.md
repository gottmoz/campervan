# Remote Queue

Minimal rendezvous queue for signed update manifests.

This is a development server, not a hardened VPS deployment. Put it behind TLS and authentication before internet exposure.

```powershell
cd E:\camper-agent
$env:CAMPER_REMOTE_QUEUE_ADMIN_TOKEN="replace-with-admin-token"
$env:CAMPER_REMOTE_QUEUE_NODE_TOKEN_COMMA_3="replace-with-comma-node-token"
python .\tools\remote-queue\queue_server.py --host 0.0.0.0 --port 8787
```

Endpoints:

- `POST /jobs`
- `GET /nodes/{nodeId}/jobs`
- `POST /jobs/{jobId}/status`
