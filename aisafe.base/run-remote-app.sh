#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "[DEPRECATED] Use scripts/run/remote/local/tcp-client.sh (local) or scripts/run/remote/cloud/tcp-client.sh (cloud)" >&2
if [[ "${AISAFE_RCOMP_LOCAL:-0}" == "1" ]]; then
  exec "$ROOT/scripts/run/remote/local/tcp-client.sh" "$@"
else
  exec "$ROOT/scripts/run/remote/cloud/tcp-client.sh" "$@"
fi
