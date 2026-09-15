#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
echo "[DEPRECATED] Use scripts/run/backoffice/console.sh" >&2
exec "$ROOT/scripts/run/backoffice/console.sh" "$@"
