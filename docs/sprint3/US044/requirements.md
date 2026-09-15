# US044 — Weather Person Remote Access

## User Story

As a **Weather Person**, I want to remotely access the system using a **TCP client application**, so that I can register, import, and consult weather data without using the backoffice console.

---

## Requirements

### Functional

1. **R1 — TCP client application**
   A dedicated TCP-based network client application is required to communicate with the server application embedded in the system.

2. **R2 — No direct database access**
   The client application interaction with the system must be limited to the TCP connection; any direct interaction with the database is unacceptable.

3. **R3 — All Weather Person user stories remotely available**
   All Weather Person user stories must be remotely available through this client application:
   - **US041** — Register weather data
   - **US042** — Bulk import weather data from CSV
   - **US043** — Consult weather data by day and air control area

4. **R4 — Authentication and authorization**
   Authentication and authorization must be enforced. Only users with role `WEATHER_PERSON` may use Weather remote commands.

5. **R5 — Shared TCP server**
   Weather Person, ATCC (US078), and Pilot (US086) remote access share the same embedded TCP server (`aisafe.rcomp.server`). Each TCP connection maintains an isolated session.

6. **R6 — Protocol framing**
   Remote commands use the same frame format as US078: `[version:byte][opcode:byte][length:int][payload:utf8]` (see `ProtocolFrame`).

7. **R7 — Role isolation**
   A logged-in ATCC or Pilot user must not execute Weather opcodes (and vice versa). Cross-role access returns `FORBIDDEN`.

8. **R8 — Remote access logging (US090)**
   Login, logout, disconnect, and business operations for Weather sessions must be logged to the Remote Accesses Logging Server with service identifier `US44`.

### Non-functional

9. **R9 — Module reuse**
   Reuse existing Maven modules: `aisafe.rcomp.protocol`, `aisafe.rcomp.server`, `aisafe.rcomp.tcpclient`, and `aisafe.core` controllers (`RegisterWeatherDataController`, `BulkWeatherDataController`).

10. **R10 — Persistence boundary (NFR08)**
    The TCP server accesses PostgreSQL via JDBC (or in-memory for local demos). The remote client never connects to the database.

11. **R11 — Domain logic reuse**
    Remote handlers delegate to existing US041–US043 controllers; no duplicate weather business rules in the RCOMP layer.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | A specific TCP-based network client application communicates with the embedded server |
| AC2 | The client interacts only via TCP — no direct database access |
| AC3 | US041, US042, and US043 are reachable through Weather remote commands |
| AC4 | Authentication and authorization are enforced for every Weather command |
| AC5 | Invalid credentials or wrong role produce `FAILED_LOGIN` or `FORBIDDEN` |
| AC6 | Weather sessions are logged for US090 with service id `US44` |

---

## Relationship to Other User Stories

| US | Relationship |
|----|----------------|
| **US041** | Register weather data — remote opcode 51 (+ helper 50) |
| **US042** | Bulk CSV import — remote opcode 52 |
| **US043** | Consult by day — remote opcode 53 |
| **US078** | Same TCP server and framing; ATCC actor uses opcodes 20–39 |
| **US086** | Same TCP server; Pilot actor uses opcodes 40–49 |
| **US090** | UDP logging of remote access events (service `US44`) |
| **US091** | HTTP dashboard for remote access logs |
| **NFR08** | Server-side JDBC; client has no persistence dependencies |

---

## References

- [Project_Requirements_V3b.pdf](../../../Project_Requirements_V3b.pdf) — US044 acceptance criteria
- [US078/design.md](../US078/design.md) — TCP protocol baseline
- [US041/requirements.md](../../sprint2/US041/requirements.md) — Register weather (domain)
- [US042/requirements.md](../US042/requirements.md) — Bulk import (domain)
- [US043/requirements.md](../US043/requirements.md) — Consult by day (domain)
