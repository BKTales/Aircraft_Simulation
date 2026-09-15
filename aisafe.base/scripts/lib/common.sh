#!/usr/bin/env bash
# Shared helpers for AISafe run scripts.

_aisafe_resolve_base() {
  local start="${1:?start directory required}"
  local d="$start"
  while [[ "$d" != "/" ]]; do
    if [[ -f "$d/pom.xml" && -d "$d/aisafe.app.backoffice.console" ]]; then
      echo "$d"
      return 0
    fi
    d="$(dirname "$d")"
  done
  echo "Could not locate aisafe.base (no pom.xml) from $start" >&2
  return 1
}

_aisafe_init_run_script() {
  local script_path="${1:?script path required}"
  export AISAFE_LIB="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
  # shellcheck source=load-env.sh
  source "$AISAFE_LIB/load-env.sh"
  # shellcheck source=rcomp-defaults.sh
  source "$AISAFE_LIB/rcomp-defaults.sh"
  AISAFE_BASE="$(_aisafe_resolve_base "$(cd "$(dirname "$script_path")" && pwd)")"
  export AISAFE_BASE
  _aisafe_load_env "$AISAFE_BASE"
}

_aisafe_require_postgres_config() {
  export AISAFE_CONFIG="${AISAFE_CONFIG:-$AISAFE_BASE/application-postgres.properties}"
  export AISAFE_ENV="${AISAFE_ENV:-$AISAFE_BASE/.env}"
  if [[ ! -f "$AISAFE_ENV" ]]; then
    echo "Missing $AISAFE_ENV — create it (see AISAFE-config-fora-do-GitHub.md)." >&2
    exit 1
  fi
  if [[ ! -f "$AISAFE_CONFIG" ]]; then
    echo "Missing $AISAFE_CONFIG — pull from git or see AISAFE-config-fora-do-GitHub.md." >&2
    echo "For in-memory only: scripts/run/backoffice/console-inmemory.sh" >&2
    exit 1
  fi
}

_aisafe_build() {
  echo "[build] Maven clean install (skip tests)..."
  mvn -f "$AISAFE_BASE/pom.xml" -B clean install -DskipTests
}

_aisafe_build_quiet() {
  if ! mvn -f "$AISAFE_BASE/pom.xml" -B -q clean install -DskipTests >/dev/null 2>&1; then
    echo "Build failed. Retrying with Maven output:" >&2
    mvn -f "$AISAFE_BASE/pom.xml" -B clean install -DskipTests
    exit 1
  fi
}

_aisafe_bootstrap_db() {
  echo "[bootstrap] Seeding database (config: $AISAFE_CONFIG)..."
  mvn -f "$AISAFE_BASE/pom.xml" -B -pl aisafe.bootstrap dependency:build-classpath -Dmdep.outputFile=target/cp.txt -q
  java -cp "$(cat "$AISAFE_BASE/aisafe.bootstrap/target/cp.txt"):$AISAFE_BASE/aisafe.bootstrap/target/aisafe.bootstrap-1.0.0-SNAPSHOT.jar" \
       eapli.aisafe.infrastructure.bootstrapers.Bootstrapper
}

_aisafe_rcomp_local() {
  export AISAFE_RCOMP_LOCAL=1
  _aisafe_rcomp_defaults
}

_aisafe_rcomp_cloud() {
  unset AISAFE_RCOMP_LOCAL
  _aisafe_rcomp_defaults
}

_aisafe_dashboard_url_local() {
  local host="${AISAFE_RCOMP_LOG_HTTP_HOST:-localhost}"
  local port="${AISAFE_RCOMP_HTTP_LISTEN_PORT:-2224}"
  echo "http://${host}:${port}"
}

_aisafe_dashboard_url_cloud() {
  local host="${AISAFE_RCOMP_LOG_HTTP_HOST:-vsgate-http.dei.isep.ipp.pt}"
  local port="${AISAFE_RCOMP_LOG_HTTP_PORT:-10387}"
  echo "http://${host}:${port}"
}
