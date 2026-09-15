#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "[DEPRECATED] Use scripts/run/remote/local/logging-server.sh (local) or scripts/run/remote/cloud/logging-server.sh (cloud)" >&2
export AISAFE_RCOMP_LOCAL="${AISAFE_RCOMP_LOCAL:-1}"
exec "$ROOT/scripts/run/remote/local/logging-server.sh" "$@"
