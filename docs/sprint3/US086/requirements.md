# US086 — Pilot Remote Access

## User story

As **Pilot**, I want to remotely access the system using the **Air Transport Company Remote App**, so that I can create, import, enrich, and validate my flight plans without using the backoffice console.

---

## Requirements

### Functional

1. **R1 — TCP client application**
   A dedicated TCP-based remote client (`RemoteClientApp`) communicates with the embedded RCOMP server.

2. **R2 — No direct database access**
   The client interacts **only** via TCP. Direct JDBC or persistence access from the client is unacceptable.

3. **R3 — Pilot user stories over TCP**
   Authenticated Pilots can execute remotely:

   | Feature | US | Pilot opcodes |
   |---------|-----|---------------|
   | Create flight plan (wizard) | US080 | 40, 47, 48 |
   | Import flight plan from file | US081 / US121 | 41, 42, 43 |
   | Attach weather to flight | US082 | 44 |
   | Validate / test flight plan | US085 | 45 |
   | List my flights (helper) | — | 46 |

4. **R4 — Authentication and authorization**
   Only users with role **PILOT** may execute opcodes **40–49**. Unauthenticated requests return `UNAUTHORIZED`; wrong role returns `FORBIDDEN`.

5. **R5 — Shared TCP server**
   Pilot, ATCC (US078), and Weather (US044) share `aisafe.rcomp.server`. Each TCP connection has an isolated session and role.

6. **R6 — Protocol framing**
   Frame format: `[version:byte][opcode:byte][length:int][payload:utf8]` (`ProtocolFrame`). Common opcodes: LOGIN (10), LOGOUT (12).

7. **R7 — Role isolation**
   ATCC users cannot execute Pilot opcodes (40–49) and Pilots cannot execute ATCC opcodes (20–39).

8. **R8 — Business rule parity**
   Remote Pilot commands delegate to the **same** `aisafe.core` controllers as the backoffice console (US080 create rules including plan replacement — see [US080](../US080/requirements.md)).

9. **R9 — Remote access logging (US090)**
   Login, logout, and disconnect events for Pilot sessions are logged with service identifier **US86**.

10. **R10 — CREATE_FLIGHT_PLAN contract**
    Opcode 40 payload, helper opcodes, and confirmation flow: [create-flight-plan-tcp.md](create-flight-plan-tcp.md).

### Non-functional

11. **R11 — Module reuse**
    Maven modules: `aisafe.rcomp.protocol`, `aisafe.rcomp.server`, `aisafe.rcomp.tcpclient`, `aisafe.core`.

12. **R12 — Persistence boundary**
    Server uses PostgreSQL via JDBC; client JAR has no persistence dependencies.

13. **R13 — Wire datetime format**
    CREATE_FLIGHT_PLAN dates use strict `uuuu-MM-dd HH:mm` on the wire (client may accept flexible input locally).

### Implementation mapping (code)

| Concern | Class / artifact |
|---------|------------------|
| Client entry | `RemoteClientApp`, `run-remote-app.sh` |
| Login / profile | `RemoteLoginUI`, `RemoteProfile.PILOT` |
| Pilot menu | `RemoteCollaboratorMenuUI`, `RemoteCollaboratorMenuActions` |
| US080 remote UI | `CreateFlightPlanRemoteUI`, `RemoteFlightPlanCreationSummary` |
| US121 remote UI | `ImportFlightPlanRemoteUI` (and related) |
| US082 / US085 remote UI | respective `*RemoteUI` in `aisafe.rcomp.tcpclient` |
| TCP gateway | `RemoteTcpGateway`, `TcpSession` |
| Server dispatch | `ClientHandler`, `PilotCommandHandler` |
| Payload codecs | `PilotCreateFlightPlanPayload`, `PilotFlightPlanPayload` |
| Opcodes | `PilotOpcodes`, `ResponseCodes` |
| Core controllers | `CreateFlightPlanController`, `ImportFlightPlanFromFileController`, `InsertWeatherInFlightController`, `ValidateFlightPlanController` |

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | TCP client connects to embedded server and completes LOGIN as Pilot |
| AC2 | Client has no direct database access |
| AC3 | US080, US121, US082, and US085 flows are reachable via Pilot opcodes 40–45 |
| AC4 | Every Pilot opcode requires authenticated PILOT session |
| AC5 | Invalid credentials → `FAILED_LOGIN`; ATCC session + Pilot opcode → `FORBIDDEN` |
| AC6 | CREATE_FLIGHT_PLAN supports silent replace and `NEEDS_CONFIRMATION` (same as US080) |
| AC7 | VALIDATE_FLIGHT_PLAN returns `OK\|PASS\|…` or `OK\|FAIL\|…` when US085 succeeds/fails |
| AC8 | Pilot remote sessions logged for US090 with service `US86` |
| AC9 | US078 ATCC client behaviour unchanged (regression) |

---

## Out of scope

| Item | Covered by |
|------|------------|
| Backoffice console flows | Respective US (080, 082, 085, 121) |
| ATCC remote opcodes | [US078](../US078/design.md) |
| Weather person remote | US044 |
| HTTP log dashboard | US091 |

---

## Related documentation

| Document | Content |
|----------|---------|
| [analysis.md](analysis.md) | Architecture, auth, gap analysis |
| [design.md](design.md) | Opcodes, components, sequence diagrams |
| [tests.md](tests.md) | Automated and manual tests |
| [create-flight-plan-tcp.md](create-flight-plan-tcp.md) | US080 TCP payload contract |

## Related user stories

| US | Relationship |
|----|--------------|
| [US078](../US078/design.md) | Shared TCP server; ATCC opcodes 20–39 |
| [US080](../US080/requirements.md) | Create flight plan business rules |
| [US082](../US082/analysis.md) | Attach weather |
| [US085](../US085/requirements.md) | Validate flight plan |
| [US121](../US121/requirements.md) | Import from DSL file |
| [US090](../US090/requirements.md) | Remote access logging |
| [US091](../US091/requirements.md) | Log dashboard |
