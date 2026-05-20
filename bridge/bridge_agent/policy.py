from __future__ import annotations

ALLOWED_COMMANDS = {
    "upload.logs",
    "rotate.capture",
    "set.scan.profile",
    "update.agent",
    "restart.service",
    "export.inventory",
}


def filter_allowed_commands(commands: list[dict]) -> list[dict]:
    allowed: list[dict] = []
    for command in commands:
        if not isinstance(command, dict):
            continue
        verb = command.get("verb")
        if verb in ALLOWED_COMMANDS:
            allowed.append(command)
    return allowed

