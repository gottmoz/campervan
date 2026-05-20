# Ford Transit Research

Read-only research tools for Ford Transit / Transit Custom support investigation.

Goals:

- collect CAN ID summaries
- collect fingerprint artifacts
- collect firmware summaries only where safe
- compare against upstream Ford support

Non-goals:

- no CAN write
- no UDS write
- no steering, brake, throttle, controls or panda safety changes
- no remote shell

Dry-run:

```powershell
cd E:\camper-agent
python .\nodes\comma3\transit_research\collect_can_summary.py
python .\nodes\comma3\transit_research\collect_fingerprint.py
python .\nodes\comma3\transit_research\collect_firmware.py
python .\nodes\comma3\transit_research\summarize_transit_support.py
```

CAN log input format for `collect_can_summary.py` is JSONL with fields like:

```json
{"bus": 0, "id": "0x123", "data": "0011223344556677"}
```
