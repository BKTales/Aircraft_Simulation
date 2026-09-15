# US090 — Requirements

## User Story

As an **Administrator**, I want to have logs for every remote access to the system, so that I can audit and monitor all remote connection activities.

## Requirements

### Functional

- **R1 — UDP datagram transmission**
  Remote access events must be transmitted to a remote application (the Remote Accesses Logging Server) using UDP datagrams.

- **R2 — Event types**
  Both successful logins and failed logins should be registered. Logouts and disconnects must also be logged.

- **R3 — Event data structure**
  Logged data must include:
  - Timestamp (ISO 8601 format)
  - Username
  - Client's IP address
  - Client's port number
  - Service identifier (US44, US78, or US86)
  - Event type (LOGIN_OK, LOGIN_FAIL, LOGOUT, DISCONNECT)

- **R4 — Logging server deployment**
  The Remote Accesses Logging Server application should run at a dedicated network node in a cloud environment.

### Non-functional

- **R5 — Fire-and-forget delivery**
  UDP transmission shall be non-blocking; failures in sending logs must not interrupt remote access operations.

- **R6 — Message format**
  Log events shall be transmitted as UTF-8 encoded pipe-delimited strings.

## Acceptance Criteria

| ID   | Criterion                                                                                           |
|------|-----------------------------------------------------------------------------------------------------|
| AC1  | Remote access events are transmitted via UDP datagrams to the configured logging server             |
| AC2  | Both successful and failed login events are captured and logged                                     |
| AC3  | Logout and disconnect events are captured and logged                                                |
| AC4  | Log entries include timestamp, username, client IP, client port, service identifier, and event type |
| AC5  | UDP transmission failures do not prevent remote access operations from continuing                   |
| AC6  | The logging server receives and stores the UDP log messages                                         |
