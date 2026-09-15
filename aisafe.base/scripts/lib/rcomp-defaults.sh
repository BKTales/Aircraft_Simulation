# RCOMP — DEI Cloud static port mappings (internal VS port : external vgate:gatePort)
#
# Cloud A — vs353 (10.9.21.97), gate suffix 353
#   2225 : vsgate-s2.dei.isep.ipp.pt:10353  — TCP (RCOMP server / remote client)
#
# Cloud B — vs387 (10.9.21.131), gate suffix 387
#   2227 : vsgate-s3.dei.isep.ipp.pt:10387  — UDP (logging)
#   2224 : vsgate-http.dei.isep.ipp.pt:10387 — HTTP (logging UI)
#
# Override in .env or env vars. Set AISAFE_RCOMP_LOCAL=1 for loopback on one machine.

_aisafe_rcomp_defaults() {
  if [[ "${AISAFE_RCOMP_LOCAL:-}" == "1" ]]; then
    : "${AISAFE_RCOMP_TCP_LISTEN_PORT:=2225}"
    : "${AISAFE_RCOMP_HOST:=127.0.0.1}"
    : "${AISAFE_RCOMP_TCP_PORT:=2225}"
    : "${AISAFE_RCOMP_UDP_LISTEN_PORT:=2227}"
    : "${AISAFE_RCOMP_HTTP_LISTEN_PORT:=2224}"
    : "${AISAFE_RCOMP_LOG_HOST:=127.0.0.1}"
    : "${AISAFE_RCOMP_LOG_UDP_PORT:=2227}"
    return
  fi

  : "${AISAFE_RCOMP_TCP_LISTEN_PORT:=2225}"
  : "${AISAFE_RCOMP_HOST:=vsgate-s2.dei.isep.ipp.pt}"
  : "${AISAFE_RCOMP_TCP_PORT:=10353}"

  : "${AISAFE_RCOMP_UDP_LISTEN_PORT:=2227}"
  : "${AISAFE_RCOMP_HTTP_LISTEN_PORT:=2224}"

  : "${AISAFE_RCOMP_LOG_HOST:=10.9.21.131}"
  : "${AISAFE_RCOMP_LOG_UDP_PORT:=2227}"

  : "${AISAFE_RCOMP_LOG_HTTP_HOST:=vsgate-http.dei.isep.ipp.pt}"
  : "${AISAFE_RCOMP_LOG_HTTP_PORT:=10387}"

  : "${AISAFE_RCOMP_LOG_UDP_GATE_HOST:=vsgate-s3.dei.isep.ipp.pt}"
  : "${AISAFE_RCOMP_LOG_UDP_GATE_PORT:=10387}"
}
