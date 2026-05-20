# GUI Plan

## Goal

Add a simulator-first GUI for the Android head-unit agent without changing the vehicle safety posture. The GUI is an operator surface for inventory, adapter state, logs, bridge status, and simulated capture flows. It must not introduce vehicle write/control behavior.

## Scope

- Show local Android and USB inventory already collected by the agent.
- Show adapter/session state as read-only telemetry.
- Show bridge connectivity, last upload status, and queued allow-listed command names.
- Provide simulator controls for fake adapter data, fake bridge responses, and capture-state transitions.
- Keep real hardware paths read-only: no CAN writes, no OBD control commands, no arbitrary shell execution.

## Simulator-First Workflow

1. Build UI screens against simulator data providers.
2. Validate state transitions with static fixtures before connecting the bridge.
3. Connect read-only live data after the simulator surface is stable.
4. Treat every live integration as display-only unless the safety model explicitly allows it.

Simulator mode should be the default for GUI development. It should be possible to run the GUI with no vLinker, no vehicle, and no desktop bridge by using local fixtures.

## First Screens

- Dashboard: agent status, adapter state, bridge health, latest inventory summary.
- Inventory: Android build/device fields and USB device list.
- Adapter: read-only connection/session state and capture counters.
- Logs: local log segments and upload state.
- Simulator: fixture selection and simulated state changes.

## Build Requirements

- Android Gradle Plugin 8.7.3.
- Kotlin 2.0.21.
- Compile/target SDK 35, build-tools 35.0.0.
- Jetpack Compose with the existing Compose BOM.
- Build from `E:\camper-agent` with `.\scripts\build-debug.ps1`.

Do not add GUI build steps that require a vehicle, bridge, network access, or elevated Windows permissions.
