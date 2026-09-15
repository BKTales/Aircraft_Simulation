#!/usr/bin/env bash
# Backoffice console with in-memory database (no PostgreSQL).
set -euo pipefail

SCRIPT_PATH="${BASH_SOURCE[0]}"
# shellcheck source=../../lib/common.sh
source "$(cd "$(dirname "$SCRIPT_PATH")" && pwd)/../../lib/common.sh"
_aisafe_init_run_script "$SCRIPT_PATH"

export AISAFE_CONFIG="$AISAFE_BASE/application-inmemory.properties"
if [[ ! -f "$AISAFE_CONFIG" ]]; then
  echo "Missing $AISAFE_CONFIG" >&2
  exit 1
fi

_aisafe_build

echo "[run] AISafe console in-memory (config: $AISAFE_CONFIG)..."
mvn -f "$AISAFE_BASE/pom.xml" -B -pl aisafe.app.backoffice.console dependency:build-classpath -Dmdep.outputFile=target/cp.txt -q
exec java -cp "$(cat "$AISAFE_BASE/aisafe.app.backoffice.console/target/cp.txt"):$AISAFE_BASE/aisafe.app.backoffice.console/target/aisafe.app.backoffice.console-1.0.0-SNAPSHOT.jar" \
     eapli.aisafe.app.backoffice.console.AISafeConsoleApp
