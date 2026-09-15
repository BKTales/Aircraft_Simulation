#!/usr/bin/env bash
# Convenience entrypoint from aisafe.base/
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$ROOT/scripts/run/help.sh" "$@"
