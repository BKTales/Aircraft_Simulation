#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "[DEPRECATED] Use scripts/run/remote/local/udp-test-log.sh or scripts/run/remote/cloud/udp-test-log.sh" >&2
exec "$ROOT/scripts/run/remote/local/udp-test-log.sh" "$@"
