# GUI Safety Model

## Safety Position

The GUI is read-only for vehicle-facing surfaces. It may visualize state, logs, inventory, and simulated captures, but it must not create new paths that can affect a vehicle, adapter firmware, CAN bus, Android system settings, or the desktop host.

## Allowed

- Display Android device metadata and USB inventory.
- Display adapter/session state reported by existing read-only code.
- Display bridge health and upload results.
- Display allow-listed bridge command verbs as text.
- Run simulator-only fixture transitions.
- Start manual read-only collection already supported by the agent.

## Not Allowed

- CAN write frames.
- OBD control or actuator commands.
- vLinker firmware/configuration writes.
- Arbitrary shell commands through the bridge or app.
- Hidden background actions that alter adapter, Android, bridge, or vehicle state.
- Broad command dispatch based on raw strings from the bridge.

## Command Handling

The GUI must treat bridge commands as data to display unless a command is already implemented in the existing allow-list and has a narrow local handler. Unknown verbs stay visible as rejected/ignored records and must not be executed.

Simulator controls must be namespaced or otherwise isolated from live controls so a simulated action cannot accidentally call a live adapter or bridge path.

## UI Requirements

- Make simulator/live state visible on each operational screen.
- Prefer disabled controls over hidden controls when a feature is intentionally unavailable.
- Log operator-triggered actions with timestamp, mode, and outcome.
- Avoid confirmation dialogs as the primary safety barrier; unsafe actions should not exist in the GUI.

## Verification

Before any live GUI build is considered usable:

1. Build the debug APK.
2. Run simulator mode without hardware.
3. Confirm no UI element exposes CAN write, OBD control, or generic shell behavior.
4. Confirm bridge commands remain restricted to the existing allow-list.
