#!/usr/bin/env bash
# Send UDP test log to cloud logging server (via gateway).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_cloud

HOST="${AISAFE_RCOMP_LOG_UDP_GATE_HOST:-vsgate-s3.dei.isep.ipp.pt}"
PORT="${AISAFE_RCOMP_LOG_UDP_GATE_PORT:-10387}"

echo "[run] UDP test log (cloud) → ${HOST}:${PORT}"
exec mvn -f "$AISAFE_BASE/pom.xml" -q -pl aisafe.rcomp.loggingserver exec:java \
  -Dexec.mainClass=eapli.aisafe.rcomp.loggingserver.test.UdpTestClient \
  "-DAISAFE_RCOMP_LOG_UDP_GATE_HOST=${HOST}" \
  "-DAISAFE_RCOMP_LOG_UDP_GATE_PORT=${PORT}"
