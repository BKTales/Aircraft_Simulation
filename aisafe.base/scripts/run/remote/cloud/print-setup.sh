#!/usr/bin/env bash
# Print cloud remote-access setup (DEI VS353 + VS387).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"
_aisafe_rcomp_cloud

BASE="$AISAFE_BASE/scripts/run"
TCP_HOST="${AISAFE_RCOMP_HOST:-vsgate-s2.dei.isep.ipp.pt}"
TCP_PORT="${AISAFE_RCOMP_TCP_PORT:-10353}"
DASH="$(_aisafe_dashboard_url_cloud)"

cat <<EOF
AISafe — cloud remote access (DEI)
==================================

ON vs387 — logging server:
  $BASE/remote/cloud/logging-server.sh

ON vs353 — TCP server (PostgreSQL):
  $BASE/remote/cloud/tcp-server.sh

ON your laptop — remote client:
  $BASE/remote/cloud/tcp-client.sh
  (connects to ${TCP_HOST}:${TCP_PORT})

ON your laptop — open US091 dashboard in browser:
  $DASH

Dashboard URL: $DASH

Optional UDP test from laptop:
  $BASE/remote/cloud/udp-test-log.sh
EOF
