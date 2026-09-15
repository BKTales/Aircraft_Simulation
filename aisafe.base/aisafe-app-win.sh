#!/usr/bin/env bash
set -euo pipefail

# Windows helper (Git Bash / WSL). Uses the unified entrypoint.
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Use:"
echo "  $SCRIPT_DIR/run-aisafe.sh --bootstrap   # first run (DB seed)"
echo "  $SCRIPT_DIR/run-aisafe.sh              # normal run"
exit 1