# US091 — Analysis

## Goal

Provide remote access log visualization via HTTP + AJAX, including event stream and active users.

## Scope (Sprint 3)

- Target module: `aisafe.rcomp.loggingserver`
- HTTP endpoints:
  - `/` dashboard page
  - `/api/events` JSON
  - `/api/active-users` JSON
- UDP receiver integrates with the same event store.

## Functional decisions

- Introduced in-memory event store:
  - bounded events buffer
  - derived active users set from login/logout messages
- HTTP server routes requests by path and returns either HTML dashboard or JSON arrays.
- Dashboard uses periodic AJAX polling to refresh events and active users.

## Traceability to implementation

- `aisafe.rcomp.loggingserver/src/main/java/eapli/aisafe/rcomp/loggingserver/HttpServer.java`
- `aisafe.rcomp.loggingserver/src/main/java/eapli/aisafe/rcomp/loggingserver/UdpServer.java`
- `aisafe.rcomp.loggingserver/src/main/java/eapli/aisafe/rcomp/loggingserver/LogEventStore.java`

