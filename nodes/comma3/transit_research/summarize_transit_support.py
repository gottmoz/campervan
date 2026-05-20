from __future__ import annotations

import argparse
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


def ford_models(cars_md: Path) -> list[str]:
    models: list[str] = []
    if not cars_md.exists():
        return models
    for line in cars_md.read_text(encoding="utf-8", errors="ignore").splitlines():
        if line.startswith("|Ford|"):
            parts = line.strip("|").split("|")
            if len(parts) >= 2:
                models.append(parts[1])
    return models


def car_enum_names(values_py: Path) -> list[str]:
    if not values_py.exists():
        return []
    text = values_py.read_text(encoding="utf-8", errors="ignore")
    return re.findall(r'\bFORD_[A-Z0-9_]+\s*=\s*FordCarDocs\("([^"]+)"', text)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--opendbc", default=str(ROOT / "external" / "opendbc"))
    parser.add_argument("--output", default=str(ROOT / "docs" / "ford-transit-openpilot-gap-analysis.md"))
    args = parser.parse_args()

    opendbc = Path(args.opendbc)
    cars = ford_models(opendbc / "docs" / "CARS.md")
    enum_models = car_enum_names(opendbc / "opendbc" / "car" / "ford" / "values.py")
    transit_found = any("transit" in item.lower() or "tourneo" in item.lower() for item in cars + enum_models)

    lines = [
        "# Ford Transit Openpilot Gap Analysis",
        "",
        f"Upstream Transit support found: {'yes' if transit_found else 'no'}",
        "",
        "## Supported Ford Models In Upstream Docs",
        "",
        *[f"- {model}" for model in cars],
        "",
        "## Ford Port Files",
        "",
        "- `external/opendbc/opendbc/car/ford/values.py`",
        "- `external/opendbc/opendbc/car/ford/interface.py`",
        "- `external/opendbc/opendbc/car/ford/carstate.py`",
        "- `external/opendbc/opendbc/car/ford/fingerprints.py`",
        "- `external/opendbc/opendbc/car/ford/fordcan.py`",
        "",
        "## Required Data For A Transit Port",
        "",
        "- CAN fingerprint per visible bus",
        "- Firmware responses for PCM, PSCM/EPS, IPMA/camera, ABS and related modules",
        "- Confirmation of ACC/LKA/Lane Centering equipment",
        "- DBC signal mapping for wheel speeds, steering angle, cruise state, brake/gas state and LKAS messages",
        "- Safety assessment before any non-dashcam behavior",
        "",
        "## Recommended Next Phase",
        "",
        "1. Keep comma 3 in dashcam/read-only mode.",
        "2. Collect fingerprint and CAN summaries from the real vehicle.",
        "3. Collect firmware summaries only with read-only diagnostics.",
        "4. Compare against existing Ford platforms in opendbc.",
        "5. Consider a dashcam-only Transit candidate only after data confirms a stable fingerprint.",
        "",
        "No steering, brake, throttle, CAN-write or panda safety changes are part of this phase.",
    ]
    output = Path(args.output)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(output)


if __name__ == "__main__":
    main()
