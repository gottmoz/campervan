# Remote Update Plan

## Recommendation

Use a pull-based signed update queue on a small rendezvous VPS.

Both the desktop bridge and the vehicle can be behind NAT. The VPS is only a queue and artifact rendezvous point; it is not a remote shell and it is not allowed to issue CAN-write/control commands.

```text
desktop bridge -> VPS queue/artifacts
vehicle nodes  -> poll queue -> download artifact -> verify signature/hash -> stage -> apply -> report status
```

## Why This First

- Works when the camper is far away and not on the same network.
- Keeps inbound ports closed on the vehicle.
- Matches the existing outbound HTTP model from comma-node.
- Supports per-node update jobs and audit logs.
- Avoids exposing the local bridge as a public control surface.

## Update Manifest

Each job should include:

- target node: `hikity-android` or `comma-3`
- version/build id
- artifact URL
- artifact SHA-256
- allow-listed verb: `update.agent` or `restart.service`
- expiry
- nonce
- monotonic sequence number
- admin signature

## Node Rules

- Verify manifest signature.
- Verify artifact checksum before staging.
- Apply only allow-listed update verbs.
- Report success/failure back to the queue.
- Continue local operation if the VPS is unreachable.

## Alternatives

Tailscale/WireGuard is useful for admin/debug access, but it broadens direct network reachability and should be optional.

Cloudflare Tunnel can expose services without port forwarding, but it risks making the bridge a public surface unless protected by Access/mTLS.

GitHub Releases polling is good for artifact hosting, but weak for per-node queues, status, and rollback policy.

## Risks

- Node secrets can leak; use per-node secrets and revocation.
- Bad updates can break a node; use staging and rollback.
- Clock skew can break short HMAC windows; signed manifests should have explicit expiry.
- The VPS becomes a dependency only for updates, not runtime telemetry.
