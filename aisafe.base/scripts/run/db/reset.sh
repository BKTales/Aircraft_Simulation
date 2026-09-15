#!/usr/bin/env bash
# Reset PostgreSQL schema via bootstrap (drop-and-create, then restore none).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_require_postgres_config

sedi() {
  if [[ "$OSTYPE" == "darwin"* ]]; then
    sed -i '' "$@"
  else
    sed -i "$@"
  fi
}

echo "[reset] Setting schema generation to drop-and-create..."
sedi 's/schema-generation.database.action=none/schema-generation.database.action=drop-and-create/' "$AISAFE_CONFIG"
sedi 's/schema-generation.database.action=create/schema-generation.database.action=drop-and-create/' "$AISAFE_CONFIG"

_aisafe_build
_aisafe_bootstrap_db

echo "[reset] Restoring schema generation to none..."
sedi 's/schema-generation.database.action=drop-and-create/schema-generation.database.action=none/' "$AISAFE_CONFIG"

echo "[done] Database reset. Run scripts/run/backoffice/console.sh"
