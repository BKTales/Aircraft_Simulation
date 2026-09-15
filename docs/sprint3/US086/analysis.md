# US086 — Analysis

## Business goal

Pilots manage and validate flight plans **remotely** via TCP, using the same domain logic as the backoffice console, without direct database access from the client.

---

## Scope (US086)

| Area | Detail |
|------|--------|
| Client | `RemoteClientApp` — profile **Pilot** after login |
| Transport | TCP + `ProtocolFrame` (shared with US078) |
| Server | `ClientHandler` → `PilotCommandHandler` for opcodes **40–49** |
| Domain | Existing `aisafe.core` controllers; AuthZ via `AuthzRegistry` session |
| Features | US080 create, US121 import, US082 weather, US085 validate |

## Out of scope

- ATCC remote commands → [US078](../US078/analysis.md)
- Weather person remote → US044
- Backoffice-only UI details → respective feature US docs

---

## Assumptions

1. Pilot selects profile **PILOT** at login (`RemoteLoginUI`); credentials are validated against `AISafeRoles.PILOT`.
2. One TCP connection = one authenticated session; role is fixed for the connection lifetime.
3. Business rules for create/replace/validate are **identical** to local console; only transport differs.
4. DSL file content for US121 is sent over TCP (filename + bytes), parsed server-side into a temp file.
5. Server runs with PostgreSQL and bootstrap data (pilots, routes, aircraft) same as backoffice.

---

## Architecture

```text
RemoteClientApp (Pilot profile)
    → TcpSession / RemoteTcpGateway
    → ClientHandler (login, role, dispatch)
    → PilotCommandHandler
    → aisafe.core controllers
    → repositories / PostgreSQL
```

| Module | Responsibility |
|--------|----------------|
| `aisafe.rcomp.tcpclient` | Console UI, TCP session, Pilot menus |
| `aisafe.rcomp.protocol` | Opcodes, frames, payloads, response codes |
| `aisafe.rcomp.server` | `ClientHandler`, `PilotCommandHandler` |
| `aisafe.core` | Business logic (unchanged from local US) |

---

## Authentication and dispatch

1. **Login:** `ClientHandler` authenticates; Pilot profile uses `authenticate(..., PILOT)`.
2. **Session role:** `RemoteSessionRole.PILOT` stored for the connection.
3. **Dispatch:**
   - Opcodes **20–39** → `AtccCommandHandler` (ATCC only)
   - Opcodes **40–49** → `PilotCommandHandler` (Pilot only)
4. **Cross-role:** wrong block → `ResponseCodes.FORBIDDEN` (-21).

Detailed login/dispatch diagram: [us086-login-sd.puml](us086-login-sd.puml).

---

## Pilot opcode map (implemented)

| Opcode | Constant | US | Entry point |
|--------|----------|-----|-------------|
| 40 | `CREATE_FLIGHT_PLAN` | US080 | `CreateFlightPlanController` → `CreateFlightPlanService` |
| 41 | `PARSE_FLIGHT_PLAN_FILE` | US121 | `ImportFlightPlanFromFileController` |
| 42 | `IMPORT_FLIGHT_PLAN` | US121 | `ImportFlightPlanFromFileController` |
| 43 | `LIST_IMPORT_AIRCRAFT` | US121 | `ImportFlightPlanFromFileController` |
| 44 | `ATTACH_WEATHER` | US082 | `InsertWeatherInFlightController` |
| 45 | `VALIDATE_FLIGHT_PLAN` | US085 | `ValidateFlightPlanController` |
| 46 | `LIST_MY_FLIGHTS` | helper | `FlightRepository` query |
| 47 | `LIST_CREATE_ROUTES` | US080 | `CreateFlightPlanController` → `CreateFlightPlanService` |
| 48 | `LIST_COMPANY_PILOTS` | US080 | `CreateFlightPlanController` → `CreateFlightPlanService` |

---

## Business rules (remote create — US080)

Same as [US080 analysis](../US080/analysis.md):

- Silent replace for **DRAFT** / **SIM_REJECTED**
- **NEEDS_CONFIRMATION** (-24) for **SUBMITTED** / **SIM_APPROVED** until `confirmReplace=true`
- Success body: `OK|designator|DRAFT|CREATED` or `OK|designator|DRAFT|REPLACED|previousStatus`

See [create-flight-plan-tcp.md](create-flight-plan-tcp.md).

---

## Gap analysis

| Item | Status |
|------|--------|
| Multi-role login (ATCC + Pilot) | Done |
| Pilot opcode routing | Done |
| US080 remote create + helpers | Done |
| US121 import over TCP | Done |
| US082 attach weather | Done |
| US085 validate over TCP | Done |
| `RemoteClientApp` Pilot profile | Done (`run-remote-app.sh`) |
| US090 logging hook | Per US090 implementation |

No open gaps for US086 Pilot remote access in Sprint 3 scope.

---

## Implementation status

| Layer | Status | Notes |
|-------|--------|-------|
| Protocol | Done | `PilotOpcodes`, payloads, `NEEDS_CONFIRMATION` |
| Server | Done | `PilotCommandHandler` |
| Client | Done | Remote UIs per feature |
| Core parity | Done | Same controllers as backoffice |
| Tests | Done | See [tests.md](tests.md) |

---

## Traceability

| US | Relationship |
|----|--------------|
| US078 | Shared server and framing |
| US080 | Create flight plan rules and summary |
| US082 | Weather attach |
| US085 | Validation / simulation trigger |
| US121 | DSL file import |
| US090 | Remote access audit |
