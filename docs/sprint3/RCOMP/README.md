# RCOMP — Sprint 3 Overview

> **This document is the entry point for all RCOMP work in Sprint 3.**  
> For SCOMP (C flight simulator), see [`docs/scomp/README.md`](../../scomp/README.md) (Sprint 2) and [`docs/sprint3/SCOMP/shm-sync-overview.md`](../SCOMP/shm-sync-overview.md) (Sprint 3).

---

## 1. What was built

AISafe remote access layer, exposing domain features (flight management, weather, fleet management) to remote clients over the network. Three distinct transports were implemented:

| Transport | Purpose | User stories |
|-----------|---------|--------------|
| **TCP** | Remote backoffice access (three profiles) | US044, US078, US086 |
| **UDP** | Structured event logging to remote server | US090 |
| **HTTP + AJAX** | Log visualisation dashboard | US091 |
| **JDBC (JPA)** | TCP server backed by remote PostgreSQL | NFR08 |

---

## 2. Sprint 3 RCOMP scope

From [`sprint_planning.md §5.3`](../sprint_planning.md):

| US | Title | Transport | Domain exposed | Responsible | Status |
|----|-------|-----------|----------------|-------------|--------|
| **TCP skeleton** | Protocol, framing, session auth | TCP | — (infrastructure) | Bernardo | Done |
| **US044** | Weather Person remote access | TCP opcodes 50–59 | US041 register, US042 bulk import, US043 consult | Henrique | Done |
| **US078** | ATCC remote access | TCP opcodes 20–39 | Fleet (US070–072), Routes (US073–074), Pilots (US075–077) | Vitor | Done |
| **US086** | Pilot remote access | TCP opcodes 40–49 | Create flight plan (US080), DSL import (US121), Attach weather (US082), Validate (US085) | João | Done |
| **US090** | External logging | UDP | Session events (login, logout, disconnect, per-operation) | Alexandre | Done |
| **US091** | Log visualisation | HTTP + AJAX | Events list, active users (React SPA) | Bernardo | Done |
| **NFR08** | Remote RDBMS | JDBC | PostgreSQL on vs233 | Vitor | Done |

### Out of scope (optional bonus — no dedicated doc)

- **US113** — flight step UDP logging (mentioned in sprint planning as optional)
- **US114** — HTTP flight dashboard (mentioned as bonus)

---

## 3. Maven modules

| Module | Artifact | Purpose |
|--------|----------|---------|
| `aisafe.rcomp.protocol` | protocol | Frame format, opcode constants, payload codecs, response codes |
| `aisafe.rcomp.server` | server | TCP server, `ClientHandler`, role handlers, UDP logger |
| `aisafe.rcomp.tcpclient` | tcpclient | Remote client app, all remote UIs |
| `aisafe.rcomp.loggingserver` | loggingserver | UDP receiver + HTTP dashboard (React, built by Maven) |

All are declared in [`aisafe.base/pom.xml`](../../../aisafe.base/pom.xml).

---

## 4. Architecture summary

One TCP server (Cloud A, vs353) handles all three remote roles. A separate logging server (Cloud B, vs387) receives UDP events and exposes an HTTP dashboard.

See **[architecture.md](architecture.md)** for the full module diagram, session lifecycle, and role-dispatch design.

---

## 5. Protocol summary

Binary framing: `[version:1][opcode:byte][length:int][payload:utf8]`  
Auth on every command. Three opcode blocks (20–39 ATCC, 40–49 Pilot, 50–59 Weather).

See **[protocol.md](protocol.md)** for the full opcode table, payload formats, response codes, and known gaps.

---

## 6. Running the system

See **[runbook.md](runbook.md)** for scripts, port table, local vs cloud config, and test commands.

Quick start (local, in-memory):

```bash
cd aisafe.base
AISAFE_RCOMP_LOCAL=1 AISAFE_CONFIG=./application-inmemory.properties ./run-rcomp-server.sh
./run-remote-app.sh        # in a second terminal
```

---

## 7. Detailed docs per US

| US | Analysis | Design | Tests | Contracts |
|----|----------|--------|-------|-----------|
| US044 | [analysis](../US044/analysis.md) | [design](../US044/design.md) | [tests](../US044/tests.md) | [weather-tcp.md](../US044/weather-tcp.md) |
| US078 | [analysis](../US073/analysis.md) | [design](../US078/design.md) | [tests](../US078/tests.md) | — |
| US086 | [analysis](../US086/analysis.md) | [design](../US086/design.md) | [tests](../US086/tests.md) | [create-flight-plan-tcp.md](../US086/create-flight-plan-tcp.md) |
| US090 | [analysis](../US90/analysis.md) | [design](../US90/design.md) | [tests](../US90/tests.md) | — |
| US091 | [analysis](../US091/analysis.md) | [design](../US091/design.md) | [tests](../US091/tests.md) | — |
| NFR08 | — | [design](../NFR08/design.md) | — | [deployment.md](../NFR08/deployment.md) |

---

## 8. Test users (bootstrap)

| Profile (TCP login field) | Username | Password | Role constant |
|---------------------------|----------|----------|---------------|
| `ATCC` (or omit) | `atcc1` | `password123` | `AIR_TRANSPORT_COMPANY_COLLABORATOR` |
| `PILOT` | `pilot1` | `password123` | `PILOT` |
| `WEATHER` | `weather` | `password123` | `WEATHER_PERSON` |

After bootstrap, the same credentials work on the TCP server. Admin / other roles receive `FAILED_LOGIN`.

---
