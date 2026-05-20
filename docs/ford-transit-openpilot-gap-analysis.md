# Ford Transit Openpilot Gap Analysis

Upstream Transit support found: no

## Supported Ford Models In Upstream Docs

- Bronco Sport 2021-24
- Escape 2020-22
- Escape 2023-24
- Escape Hybrid 2020-22
- Escape Hybrid 2023-24
- Escape Plug-in Hybrid 2020-22
- Escape Plug-in Hybrid 2023-24
- Expedition 2022-24
- Explorer 2020-24
- Explorer Hybrid 2020-24
- F-150 2021-23
- F-150 Hybrid 2021-23
- Focus 2018
- Focus Hybrid 2018
- Kuga 2020-23
- Kuga Hybrid 2020-23
- Kuga Hybrid 2024
- Kuga Plug-in Hybrid 2020-23
- Kuga Plug-in Hybrid 2024
- Maverick 2022
- Maverick 2023-24
- Maverick Hybrid 2022
- Maverick Hybrid 2023-24
- Mustang Mach-E 2021-24
- Ranger 2024

## Ford Port Files

- `external/opendbc/opendbc/car/ford/values.py`
- `external/opendbc/opendbc/car/ford/interface.py`
- `external/opendbc/opendbc/car/ford/carstate.py`
- `external/opendbc/opendbc/car/ford/fingerprints.py`
- `external/opendbc/opendbc/car/ford/fordcan.py`

## Required Data For A Transit Port

- CAN fingerprint per visible bus
- Firmware responses for PCM, PSCM/EPS, IPMA/camera, ABS and related modules
- Confirmation of ACC/LKA/Lane Centering equipment
- DBC signal mapping for wheel speeds, steering angle, cruise state, brake/gas state and LKAS messages
- Safety assessment before any non-dashcam behavior

## Recommended Next Phase

1. Keep comma 3 in dashcam/read-only mode.
2. Collect fingerprint and CAN summaries from the real vehicle.
3. Collect firmware summaries only with read-only diagnostics.
4. Compare against existing Ford platforms in opendbc.
5. Consider a dashcam-only Transit candidate only after data confirms a stable fingerprint.

No steering, brake, throttle, CAN-write or panda safety changes are part of this phase.
