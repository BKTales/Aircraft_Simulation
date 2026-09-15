#!/usr/bin/env bash
# List all AISafe run scripts and common scenarios.
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"

R="$AISAFE_BASE/scripts/run"

cat <<EOF
AISafe run scripts
==================

Backoffice:
  $R/backoffice/console.sh [--bootstrap]     Console (PostgreSQL)
  $R/backoffice/console-inmemory.sh          Console (in-memory)
  $R/backoffice/bootstrap-db.sh              Seed DB only

Remote — local (all on one machine, AISAFE_RCOMP_LOCAL=1):
  $R/remote/local/print-setup.sh             Show 4-terminal guide
  $R/remote/local/logging-server.sh          US091 HTTP + UDP (open dashboard in browser)
  $R/remote/local/tcp-server.sh              RCOMP TCP server
  $R/remote/local/tcp-client.sh              Remote client
  $R/remote/local/udp-test-log.sh            Send test UDP log

Remote — cloud (DEI vs353 / vs387):
  $R/remote/cloud/print-setup.sh             Cloud guide
  $R/remote/cloud/logging-server.sh          Run on vs387
  $R/remote/cloud/tcp-server.sh              Run on vs353
  $R/remote/cloud/tcp-client.sh              From laptop
  $R/remote/cloud/udp-test-log.sh            UDP test via gateway

Database:
  $R/db/reset.sh                             Drop-and-recreate schema

Legacy wrappers in aisafe.base/ still work (run-aisafe.sh, run-backoffice.sh, etc.).
EOF
