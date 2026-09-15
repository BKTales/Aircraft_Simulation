#!/usr/bin/env bash
# Send one UDP test log to the local logging server.
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_local

HOST="${AISAFE_RCOMP_LOG_HOST:-127.0.0.1}"
PORT="${AISAFE_RCOMP_LOG_UDP_PORT:-2227}"

echo "[run] UDP test log → ${HOST}:${PORT}"
exec mvn -f "$AISAFE_BASE/pom.xml" -q -pl aisafe.rcomp.loggingserver exec:java \
  -Dexec.mainClass=eapli.aisafe.rcomp.loggingserver.test.UdpTestClient \
  "-DAISAFE_RCOMP_LOG_UDP_GATE_HOST=${HOST}" \
  "-DAISAFE_RCOMP_LOG_UDP_GATE_PORT=${PORT}"
