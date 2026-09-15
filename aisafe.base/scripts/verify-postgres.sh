#!/usr/bin/env bash
set -euo pipefail

HOST="${PGHOST:-127.0.0.1}"
PORT="${PGPORT:-5432}"
DB="${PGDATABASE:-aisafe}"
USER="${PGUSER:-postgres}"

if command -v pg_isready >/dev/null 2>&1; then
  pg_isready -h "${HOST}" -p "${PORT}" -U "${USER}" -d "${DB}"
  echo "PostgreSQL reachable at ${HOST}:${PORT}/${DB}"
  exit 0
fi

if command -v nc >/dev/null 2>&1; then
  nc -z -w 5 "${HOST}" "${PORT}"
  echo "TCP port ${PORT} open on ${HOST} (install postgresql client for full check)"
  exit 0
fi

echo "Install postgresql client (pg_isready) or netcat (nc)" >&2
exit 1
