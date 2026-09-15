# US078 — Design

Formal sequence diagram: [`us078-sd.puml`](us078-sd.puml) (handshake, auth, ATCC loop 20–39, logout, disconnect, US090 UDP).

## Network

| Environment | Client target | Server listen |
|-------------|---------------|---------------|
| Local | `127.0.0.1:2225` | `RcompTcpServerApp` (`AISAFE_RCOMP_TCP_PORT`, default `2225`) |
| DEI gateway | `vsgate-s2.dei.isep.ipp.pt:10353` | forwarded to app port `2225` |

```mermaid
sequenceDiagram
    participant Client as AtccRemoteClient
    participant Gate as vsgate_s2_10353
    participant Server as RcompTcpServer
    participant Handler as ClientHandler
    participant Atcc as AtccCommandHandler
    participant Core as aisafe_core_controllers
    participant DB as PostgreSQL

    Client->>Gate: TCP
    Gate->>Server: port 2225
    Server->>Handler: thread per connection
    Client->>Handler: LOGIN (10) username;password;ATCC
    Handler->>Core: authenticate (AuthzRegistry session)
    Handler-->>Client: SUCCESS_LOGIN (11) / FAILED_LOGIN (-11)
    loop session
        Client->>Handler: ATCC opcode 20-39
        Handler->>Atcc: handle
        Atcc->>Core: same controllers as backoffice
        Core->>DB: JDBC
        Atcc-->>Client: OK + text / error code
    end
    Client->>Handler: LOGOUT (12)
    Handler-->>Client: SUCCESS_LOGOUT (13)
```

## Single TCP server, multiple remote-access stories

All remote roles share **`aisafe.rcomp.server.TcpServer`** → **`ClientHandler`** (one thread per connection). Opcode range selects the handler:

| Opcode range | User story | Handler | Session role |
|--------------|------------|---------|--------------|
| **10–13, -11, -12** | Common (login/logout) | `ClientHandler` | — |
| **20–39** | **US078** ATCC collaborator | `AtccCommandHandler` | `AIR_TRANSPORT_COMPANY_COLLABORATOR` |
| **40–49** | **US086** Pilot remote access | `PilotCommandHandler` | `PILOT` |
| **50–59** | **US044** Weather person | `WeatherCommandHandler` | `WEATHER_PERSON` |

**US078 scope:** fleet, routes, and pilot roster management for the ATCC (opcodes 20–39).

**Not US078 (same server, different block):**

| Feature US | Remote US | Opcodes |
|------------|-----------|---------|
| US080 Create flight plan | US086 | 40, 47, 48 |
| US081 Import flight plan | US086 | 41–43 |
| US082 Attach weather | US086 | 44 |
| US085 Validate flight plan | US086 | 45 |

Login payload must declare the intended role: `username;password;ATCC` (default role is ATCC when the third field is omitted). A PILOT session sending opcode 29 receives `FORBIDDEN (-21)`.

## Frame format

`[version:byte=1][opcode:byte][length:int][payload:utf8]`

### Common opcodes

| Code | Name |
|------|------|
| 10 | LOGIN (`username;password` or `username;password;ATCC\|PILOT\|WEATHER`) |
| 11 | SUCCESS_LOGIN |
| 12 | LOGOUT |
| 13 | SUCCESS_LOGOUT |
| -11 | FAILED_LOGIN |
| -12 | INVALID_CREDENTIALS |

### Response codes (business)

| Code | Meaning |
|------|---------|
| 0 | OK |
| -20 | UNAUTHORIZED |
| -21 | FORBIDDEN |
| -22 | BAD_REQUEST |
| -23 | NOT_IMPLEMENTED |
| -25 | NOT_FOUND |
| -26 | CONFLICT |

### ATCC opcodes (20–39)

See `eapli.aisafe.rcomp.protocol.AtccOpcodes`.

| Op | Constant | Backoffice / domain US |
|----|----------|------------------------|
| 20 | LIST_AIRCRAFT_MODELS | US055 |
| 21 | REGISTER_AIRCRAFT | US070 |
| 22 | DECOMMISSION_AIRCRAFT | US071 |
| 23 | LIST_ACTIVE_AIRCRAFT | US071 |
| 24–26 | LIST_FLEET / MODELS / MANUFACTURERS | US072 |
| 27 | CREATE_ROUTE | US073 |
| 28 | DEACTIVATE_ROUTE | US074 |
| 29 | REMOVE_PILOT | US077 |
| 30–31 | ADD_PILOT_NEW / EXISTING | US075 |
| 32 | LIST_PILOT_ROSTER | US076 |
| 33 | LIST_ELIGIBLE_USERS | US075 |
| 34–35 | LIST_AIRCRAFT_MODEL_IDS / CERTIFIED_ENGINES | US075 |
| 36–39 | Route helpers (context, validate name, airports, list active) | US073 / US074 |

## US090 logging (UDP)

`ClientHandler` and `AtccCommandHandler` call `RemoteAccessLogger` → `UdpClient` (default port `2227`). Events: `LOGIN_OK`, `LOGIN_FAIL`, `LOGOUT`, `DISCONNECT`, plus per-operation names (e.g. `REMOVE_PILOT`). Service id for ATCC sessions: `US78`.

## Payload examples

- Register aircraft: `CS-TST;A320;ENG01;150;20;8;PT;6;2015`
- List fleet: `UNFILTERED` | `MODEL;A320` | `PASSENGERS;180;GT`
- Add pilot (new user, op 30): `user;pwd;First;Last;email@x.com;2026-01-01;+351...;CERT:A320,2026-01-01,2028-01-01`
- Add pilot (existing user, op 31): `email@x.com;2026-01-01;+351...;CERT:A320,2026-01-01,2028-01-01`
- List eligible users (op 33): empty payload → newline-separated emails
- Remove pilot (op 29): `pilot@tap.com` → `OK` or `NOT_FOUND` / `CONFLICT` / `BAD_REQUEST`
- List active routes (op 39): empty payload → `name|company|origin|dest|type|schedule|recurring|deactivation`
- Deactivate route (op 28): `routeName;2026-06-15`

## Notes

- `AtccCommandHandler` delegates to the same `aisafe.core` controllers as the backoffice UI; package-private constructor for tests (`AtccCommandHandlerTest`).
- Remote pilot-collaborator flow mirrors `AddPilotCollaboratorUserUI`: list eligible users (33) then add existing (31), or add new user (30).
- `SessionManager` enforces one active TCP session per username across ATCC/Pilot/Weather clients.
