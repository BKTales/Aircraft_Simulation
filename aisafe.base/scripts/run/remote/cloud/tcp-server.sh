#!/usr/bin/env bash
# RCOMP TCP server on cloud VS (vs353). Run ON the server VM with PostgreSQL.
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_require_postgres_config
_aisafe_rcomp_cloud

PORT="${AISAFE_RCOMP_TCP_LISTEN_PORT:-2225}"
HOST="${AISAFE_RCOMP_HOST:-vsgate-s2.dei.isep.ipp.pt}"
GATE_PORT="${AISAFE_RCOMP_TCP_PORT:-10353}"

_aisafe_build

echo "[run] RCOMP TCP server (cloud) listen=$PORT"
echo "      External clients: ${HOST}:${GATE_PORT}"

mvn -f "$AISAFE_BASE/pom.xml" -B -q -pl aisafe.rcomp.server dependency:build-classpath -Dmdep.outputFile=target/cp.txt

exec java -DAISAFE_RCOMP_TCP_PORT="$PORT" \
  -cp "$(cat "$AISAFE_BASE/aisafe.rcomp.server/target/cp.txt"):$AISAFE_BASE/aisafe.rcomp.server/target/aisafe.rcomp.server-1.0.0-SNAPSHOT.jar" \
  eapli.aisafe.rcomp.server.RcompTcpServerApp
