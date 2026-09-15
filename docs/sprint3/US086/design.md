# US086 — Design

Pilot remote access over TCP. Business rules for create/replace: [US080](../US080/design.md). TCP payload details: [create-flight-plan-tcp.md](create-flight-plan-tcp.md).

---

## Architectural approach

Same pattern as [US078](../US078/design.md):

- **Client** — `RemoteClientApp` + feature `*RemoteUI` classes
- **Transport** — `ProtocolFrame` over TCP (`TcpSession`)
- **Server** — `ClientHandler` (session) + `PilotCommandHandler` (opcodes 40–49)
- **Application** — existing `aisafe.core` controllers delegate to services (US080: `CreateFlightPlanController` → `CreateFlightPlanService`; no duplicated business logic)

```text
Pilot → RemoteClientApp → TcpSession → ClientHandler → PilotCommandHandler
                                                      → CreateFlightPlanController / …
                                                      → PostgreSQL (server only)
```

---

## Sequence diagrams

| Diagram | Scope |
|---------|--------|
| [us086-login-sd.puml](us086-login-sd.puml) | Login, role selection, opcode dispatch, FORBIDDEN paths |
| [us086-sd.puml](us086-sd.puml) | **Simplified** — remote create flight plan (US080 over TCP) |

Other Pilot flows (import, weather, validate) follow the same handler → controller pattern; see opcode table below.

---

## Network

| Environment | Client target | Notes |
|-------------|---------------|--------|
| Local | `127.0.0.1:2225` (default) | `run-rcomp-server.sh` + `run-remote-app.sh` |
| DEI gateway | `vsgate-s2.dei.isep.ipp.pt:10353` | `-DAISAFE_RCOMP_HOST` / `-DAISAFE_RCOMP_TCP_PORT` |

Launch client:

```bash
cd aisafe.base && ./run-remote-app.sh
```

Choose **Pilot** profile at login (`pilot1` / `password123` after bootstrap).

---

## Authentication

LOGIN payload (opcode 10):

| Form | Role |
|------|------|
| `username;password;PILOT` | Pilot (US086) |
| `username;password;ATCC` | ATCC (US078) |
| `username;password` | ATCC (backward compatible) |

Parser: `LoginCredentialsParser` in `aisafe.rcomp.server`.

Session routing in `ClientHandler`:

- Opcodes **20–39** → ATCC session only
- Opcodes **40–49** → Pilot session only
- Wrong block → `FORBIDDEN` (-21)

---

## Pilot opcodes (40–49)

| Code | Name | US | Request (summary) | Success response (summary) |
|------|------|-----|-------------------|----------------------------|
| 40 | CREATE_FLIGHT_PLAN | US080 | 12-field semicolon payload | `OK\|designator\|DRAFT\|CREATED\|…` |
| 41 | PARSE_FLIGHT_PLAN_FILE | US121 | file name + content | `OK` or `BAD_REQUEST` + DSL errors |
| 42 | IMPORT_FLIGHT_PLAN | US121 | file + aircraft reg | `OK` + designator message |
| 43 | LIST_IMPORT_AIRCRAFT | US121 | empty | registration lines |
| 44 | ATTACH_WEATHER | US082 | `designator;weatherDataId` | `OK\|flight summary` |
| 45 | VALIDATE_FLIGHT_PLAN | US085 | `designator` | `OK\|PASS\|…` or `OK\|FAIL\|…` |
| 46 | LIST_MY_FLIGHTS | aux | empty | flight lines for logged-in pilot |
| 47 | LIST_CREATE_ROUTES | US080 | `yyyy-MM-dd` | route lines |
| 48 | LIST_COMPANY_PILOTS | US080 | empty | pilot roster lines |

Response codes: `ResponseCodes` — notably `NEEDS_CONFIRMATION` (-24) for US080 replace confirm.

Full CREATE_FLIGHT_PLAN contract: [create-flight-plan-tcp.md](create-flight-plan-tcp.md).

---

## Remote create flow (US080) — summary

1. `CreateFlightPlanRemoteUI` collects wizard input (same fields as backoffice).
2. Helper calls: opcode **47** (routes), **43** (aircraft), **48** (pilots).
3. Opcode **40** with `PilotCreateFlightPlanPayload.encode(...)`.
4. If response **NEEDS_CONFIRMATION** → show message → re-send with `confirmReplace=true`.
5. On **OK** → `RemoteFlightPlanCreationSummary.printSuccess(...)`.

See [us086-sd.puml](us086-sd.puml).

---

## Components

### Client (`aisafe.rcomp.tcpclient`)

| Component | Role |
|-----------|------|
| `RemoteClientApp` | Main entry; TCP connect, login loop, menu |
| `RemoteLoginUI` | Profile + credentials |
| `RemoteCollaboratorMenuUI` | Pilot / ATCC / Weather root menu |
| `RemoteTcpGateway` | Send/receive `ProtocolFrame` |
| `CreateFlightPlanRemoteUI` | US080 wizard over TCP |
| `RemoteFlightPlanCreationSummary` | Post-create summary |
| `ImportFlightPlanRemoteUI` | US121 import flow |
| `ValidateFlightPlanRemoteUI` | US085 validation flow |
| `AttachWeatherRemoteUI` | US082 weather attach |

### Server (`aisafe.rcomp.server`)

| Component | Role |
|-----------|------|
| `ClientHandler` | Login, logout, role, opcode dispatch |
| `PilotCommandHandler` | Map opcode → controller call → frame response |
| `PilotResponseFormatter` | Format route/pilot/flight lines |
| `TempFlightPlanFile` | US121 — write uploaded bytes for parse/import |

### Protocol (`aisafe.rcomp.protocol`)

| Component | Role |
|-----------|------|
| `PilotOpcodes` | Opcode constants 40–48 |
| `PilotCreateFlightPlanPayload` | Encode/decode CREATE_FLIGHT_PLAN |
| `PilotFlightPlanPayload` | Encode/decode file upload for US121 |
| `ProtocolFrame` | Binary framing |
| `ResponseCodes` | OK, UNAUTHORIZED, FORBIDDEN, BAD_REQUEST, NEEDS_CONFIRMATION, … |

---

## Error flows

| Condition | Response |
|-----------|----------|
| Not logged in | `UNAUTHORIZED` (-20) |
| ATCC session + Pilot opcode | `FORBIDDEN` (-21) |
| Invalid payload / business rule | `BAD_REQUEST` (-22) |
| Replace needs confirmation | `NEEDS_CONFIRMATION` (-24) |
| Unknown opcode | `BAD_REQUEST` |

---

## Test users (bootstrap)

| Profile | User | Password |
|---------|------|----------|
| Pilot | `pilot1` | `password123` |
| ATCC (negative test) | `atcc1` | `password123` |

---

## Regression

- US078 `RemoteClientApp` ATCC profile and opcodes 20–39 unchanged.
- `ProtocolFrameTest` remains green.
- Client JAR must not depend on JDBC / JPA.
