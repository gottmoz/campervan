# GUI Module Architecture

## Architecture

Keep the GUI inside the existing Android app module. Do not create new Gradle modules until there is repeated code or build pressure that justifies it.

Use clear package boundaries:

```text
ui/              Compose screens and navigation
ui/simulator/    Fixture-backed simulator controls
domain/          Read-only state models used by UI and agent code
data/            Inventory, adapter, log, and bridge data sources
```

The package names can follow existing app conventions, but the dependency direction should stay simple:

```text
ui -> domain -> data interfaces
data implementations -> platform/bridge APIs
```

UI code should not call adapter, USB, bridge, or file APIs directly. Route those through small data-source classes or view models so simulator and live providers can share the same UI models.

## Providers

Use two provider families:

- Simulator providers: deterministic fixtures, no hardware, no bridge.
- Live read-only providers: existing inventory/session/log/bridge reads.

The GUI should select simulator providers by default for development builds until live wiring is explicitly enabled. Provider selection must not change the underlying safety model: live providers remain read-only.

## State Models

Prefer compact immutable state models for screens:

- `GuiDashboardState`
- `GuiInventoryState`
- `GuiAdapterState`
- `GuiBridgeState`
- `GuiLogState`

State models should represent unavailable data explicitly instead of forcing UI code to infer it from exceptions or null platform objects.

## Build Notes

The current build is a single Android application module using Compose. Required inputs are already captured by the repository build:

- `settings.gradle.kts` includes only `:app`.
- Root build uses Android Gradle Plugin 8.7.3 and Kotlin 2.0.21.
- App build enables Compose and targets SDK 35.
- `scripts\build-debug.ps1` sets project-local Android SDK variables when `.android-sdk` exists, then runs `:app:assembleDebug`.

GUI work should keep those build assumptions intact unless a separate task changes build ownership.
