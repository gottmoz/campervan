#!/usr/bin/env bash
set -euo pipefail

cd /data/camper-agent/comma-node

if [ ! -f comma_node.pid ]; then
  echo "no pid file"
  exit 0
fi

pid="$(cat comma_node.pid)"
if [ -n "$pid" ] && kill -0 "$pid" 2>/dev/null; then
  kill "$pid"
  echo "stopped comma_node: $pid"
else
  echo "comma_node not running: $pid"
fi
rm -f comma_node.pid
