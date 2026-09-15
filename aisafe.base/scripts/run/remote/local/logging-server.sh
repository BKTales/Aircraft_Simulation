#!/usr/bin/env bash
# RCOMP logging server — HTTP dashboard (US091) + UDP ingestion. Local loopback.
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_local

UDP_PORT="${AISAFE_RCOMP_UDP_LISTEN_PORT:-2227}"
TCP_PORT="${AISAFE_RCOMP_TCP_LISTEN_PORT:-2228}"
HTTP_PORT="${AISAFE_RCOMP_HTTP_LISTEN_PORT:-2224}"
DASHBOARD="$(_aisafe_dashboard_url_local)"

echo "[build] Logging server..."
mvn -f "$AISAFE_BASE/pom.xml" -B -q clean install -DskipTests

echo "[run] RCOMP logging server (local)"
echo "      UDP listen : $UDP_PORT"
echo "      HTTP listen: $HTTP_PORT  →  $DASHBOARD"
echo "      Open US091 in browser at the URL above."

exec java -DAISAFE_RCOMP_UDP_PORT="$UDP_PORT" -DAISAFE_RCOMP_TCP_PORT="$TCP_PORT" -DAISAFE_RCOMP_HTTP_PORT="$HTTP_PORT" \
  -cp "$AISAFE_BASE/aisafe.rcomp.loggingserver/target/aisafe.rcomp.loggingserver-1.0.0-SNAPSHOT.jar" \
  eapli.aisafe.rcomp.loggingserver.LoggingServerApp
