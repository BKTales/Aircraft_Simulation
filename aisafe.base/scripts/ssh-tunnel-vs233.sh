#!/usr/bin/env bash
# Forward local 5432 -> PostgreSQL on vs233. Requires DEI SSH user (see deployment.md).
set -euo pipefail

# Panel: vs233.dei.isep.ipp.pt:2222 <- vsgate-ssh.dei.isep.ipp.pt:10233
# Alternative: VS_HOST=vsgate-ssh.dei.isep.ipp.pt VS_SSH_PORT=10233
HOST="${VS_HOST:-vs233.dei.isep.ipp.pt}"
PORT="${VS_SSH_PORT:-2222}"
PG_HOST="${VS_PG_HOST:-10.9.20.233}"
PG_PORT="${VS_PG_PORT:-5432}"
SSH_USER="${VS_SSH_USER:?Set VS_SSH_USER to your DEI VS login}"

echo "Tunnel: localhost:5432 -> ${PG_HOST}:${PG_PORT} via ${HOST}:${PORT} (user ${SSH_USER})"
exec ssh -N -p "${PORT}" -L "5432:${PG_HOST}:${PG_PORT}" "${SSH_USER}@${HOST}"
