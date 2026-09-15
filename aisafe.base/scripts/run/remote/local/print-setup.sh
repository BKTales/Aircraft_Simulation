#!/usr/bin/env bash
# Print which terminals to open for a full local remote-access demo.
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_local

BASE="$AISAFE_BASE/scripts/run"
DASH="$(_aisafe_dashboard_url_local)"

cat <<EOF
AISafe — local remote access (4 terminals)
==========================================

Terminal 1 — Logging server (US091 HTTP + UDP):
  cd $AISAFE_BASE
  $BASE/remote/local/logging-server.sh

Terminal 2 — RCOMP TCP server (needs PostgreSQL + .env):
  cd $AISAFE_BASE
  $BASE/remote/local/tcp-server.sh

Terminal 3 — Backoffice console (admin/backoffice login):
  cd $AISAFE_BASE
  ./run-aisafe.sh
  # or first time: ./run-aisafe.sh --bootstrap
  # Open US091 dashboard in browser: $DASH

Terminal 4 — Remote TCP client (weather / pilot / ATCC user):
  cd $AISAFE_BASE
  $BASE/remote/local/tcp-client.sh

Optional — send test UDP log:
  $BASE/remote/local/udp-test-log.sh

Dashboard URL: $DASH
EOF
