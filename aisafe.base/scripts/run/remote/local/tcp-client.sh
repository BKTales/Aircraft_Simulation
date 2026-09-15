#!/usr/bin/env bash
# RCOMP TCP client — connect to local TCP server (Weather / Pilot / ATCC remote menus).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_local

HOST="${AISAFE_RCOMP_HOST:-127.0.0.1}"
PORT="${AISAFE_RCOMP_TCP_PORT:-2225}"

_aisafe_build_quiet
mvn -f "$AISAFE_BASE/pom.xml" -B -q -pl aisafe.rcomp.tcpclient dependency:build-classpath -Dmdep.outputFile=target/cp.txt

CLIENT_JAR="$AISAFE_BASE/aisafe.rcomp.tcpclient/target/aisafe.rcomp.tcpclient-1.0.0-SNAPSHOT.jar"
DEPS_CP="$(cat "$AISAFE_BASE/aisafe.rcomp.tcpclient/target/cp.txt")"

echo "[run] Remote client → ${HOST}:${PORT}"
printf '\033[H\033[2J'
exec java -DAISAFE_RCOMP_HOST="$HOST" -DAISAFE_RCOMP_TCP_PORT="$PORT" \
  -cp "$DEPS_CP:$CLIENT_JAR" \
  eapli.aisafe.rcomp.tcpclient.RemoteClientApp
