# First Prototype Plan

Source: `C:\Users\jimmy\Downloads\deep-research-report.md`.

## Scope

Build the smallest useful read-only prototype:

1. Android app starts manually and can be bootstrapped after boot/update.
2. It inventories Android device data and USB devices.
3. It models adapter state without vehicle write/control operations.
4. It can later upload inventory/log segments to the local bridge.
5. Bridge stores agent reports and exposes only allow-listed commands.

## Safety Rules

- No generic remote shell.
- No CAN write commands in phase one.
- Treat OBD, raw CAN, vendor MCU surfaces, and Android internals as separate domains.
- Keep command verbs explicit and logged.
