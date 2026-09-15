#!/usr/bin/env bash
# Bootstrap PostgreSQL schema and seed users (one-time / reset helper).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_require_postgres_config

_aisafe_build
_aisafe_bootstrap_db
echo "[done] Database bootstrapped. Run scripts/run/backoffice/console.sh"
