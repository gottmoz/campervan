#!/usr/bin/env bash
set -euo pipefail

cd /data/camper-agent/comma-node

if [ -f comma_node.pid ]; then
  old_pid="$(cat comma_node.pid)"
  if [ -n "$old_pid" ] && kill -0 "$old_pid" 2>/dev/null; then
    echo "comma_node already running: $old_pid"
    exit 0
  fi
fi

nohup python3 comma_node.py --loop >> comma_node.log 2>&1 &
echo "$!" > comma_node.pid
echo "started comma_node: $(cat comma_node.pid)"
