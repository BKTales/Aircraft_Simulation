# US086 — Tests

Requirements: [requirements.md](requirements.md). Design: [design.md](design.md).

US080 business-rule tests (local): [US080/tests.md](../US080/tests.md).

---

## Status

Pilot remote access is **implemented**. Automated tests cover protocol, login/dispatch, and `PilotCommandHandler` delegation. End-to-end TCP flows are validated manually with `run-remote-app.sh`.

Run focused suite:

```bash
cd aisafe.base
mvn -q -pl aisafe.rcomp.protocol,aisafe.rcomp.server test \
  -Dtest=ProtocolFrameTest,ClientHandlerLoginTest,PilotCommandHandlerTest,PilotCreateFlightPlanPayloadTest
```

Broader regression:

```bash
cd aisafe.base
mvn -q -pl aisafe.rcomp.protocol,aisafe.rcomp.server,aisafe.core test
```

---

## Acceptance criteria traceability

| AC | Criterion | Automated | Manual |
|----|-----------|-----------|--------|
| AC1 | TCP client + Pilot login | `ClientHandlerLoginTest.ensurePilotLoginSucceeds` | M1 |
| AC2 | No client DB access | — (build/classpath check) | M6 |
| AC3 | US080/121/082/085 opcodes | `PilotCommandHandlerTest` per opcode | M2–M5 |
| AC4 | Auth on every command | `ensureRequiresSession`, `ensureRequiresPilotRole` | M1 |
| AC5 | Failed login / FORBIDDEN | Login tests + `ensureRequiresPilotRole` | M1 |
| AC6 | CREATE replace + confirm | `ensureCreatePlanNeedsConfirmation`, create success tests | M3 |
| AC7 | VALIDATE PASS/FAIL | `ensureValidateFlightPlanPass/Fail/DslFailure` | M5 |
| AC8 | US090 logging | — | M7 (when US090 enabled) |
| AC9 | US078 regression | `ClientHandlerLoginTest` ATCC path | M6 |

---

## Automated tests

### Protocol — `ProtocolFrameTest`

Frame encode/decode round-trip (regression for all remote clients).

### Login — `ClientHandlerLoginTest`

| Test | Expected |
|------|----------|
| `ensureAtccLoginSucceeds` | `SUCCESS_LOGIN`, role ATCC |
| `ensurePilotLoginSucceeds` | `SUCCESS_LOGIN`, role PILOT |
| `ensureAdminLoginFails` | `FAILED_LOGIN` |
| `ensureInvalidPasswordFails` | `FAILED_LOGIN` |
| `ensureMalformedCredentialsRejected` | `INVALID_CREDENTIALS` |

### Pilot handler — `PilotCommandHandlerTest`

| Test | Expected |
|------|----------|
| `ensureRequiresSession` | `UNAUTHORIZED` |
| `ensureRequiresPilotRole` | ATCC session + opcode 40 → `FORBIDDEN` |
| `ensureCreatePlanSucceeds` | `OK` + `DRAFT\|CREATED` |
| `ensureCreatePlanNeedsConfirmation` | `NEEDS_CONFIRMATION` |
| `ensureCreatePlanFailsWhenArrivalBeforeDeparture` | `BAD_REQUEST` |
| `ensureListCreateRoutesReturnsActiveRoutes` | `OK` with route lines |
| `ensureListCompanyPilotsReturnsRoster` | `OK` with pilot lines |
| `ensureImportDelegatesToController` | Import opcode → `OK` |
| `ensureAttachWeatherDelegates` | Attach opcode → `OK` |
| `ensureValidateFlightPlanPass` | `OK\|PASS\|TP123` |
| `ensureValidateFlightPlanFail` | `OK\|FAIL\|…` |
| `ensureValidateFlightPlanDslFailure` | `BAD_REQUEST` |

### Payload — `PilotCreateFlightPlanPayloadTest`

Encode/decode round-trip; wire datetime strictness; 12-field validation.

### Core (shared business rules)

| Class | US | Notes |
|-------|-----|-------|
| `CreateFlightPlanServiceTest` | US080 | Same rules as local — see US080 tests |
| `ImportFlightPlanFromFileControllerTest` | US121 | Import business rules |
| `InsertWeatherInFlightControllerTest` | US082 | Weather attach rules |
| `ValidateFlightPlanServiceTest` | US085 | Validation logic |

Remote handler tests mock controllers; domain coverage lives in `aisafe.core`.

---

## Manual scenarios

### Setup

1. **Server:** `cd aisafe.base && ./run-rcomp-server.sh` (PostgreSQL + bootstrap).
2. **Client:** `cd aisafe.base && ./run-remote-app.sh`.
3. Login profile **Pilot**, user `pilot1` / `password123`.

### M1 — Login and authorization

| Step | Expected |
|------|----------|
| Pilot login on remote app | Success; menu "AISafe Remote Pilot" |
| ATCC user on Pilot profile | Login failed |
| Pilot opcode without login | `UNAUTHORIZED` (raw client) |
| ATCC client + send opcode 40 | `FORBIDDEN` |

### M2 — Import flight plan (US121)

| Step | Expected |
|------|----------|
| Parse valid DSL file (41) | `OK` or validation errors with line/col |
| List aircraft (43) | Registration lines |
| Import with valid aircraft (42) | `OK`, designator, DRAFT |
| Duplicate designator | `BAD_REQUEST` |

### M3 — Create flight plan (US080)

| Step | Expected |
|------|----------|
| Helpers 47/48/43 return data | Routes, pilots, aircraft |
| Create with valid payload (40) | `OK\|…\|CREATED`; summary in UI |
| Same designator, DRAFT exists | `OK\|…\|REPLACED\|DRAFT` (silent) |
| SIM_APPROVED exists, no confirm | `NEEDS_CONFIRMATION`; retry with confirm |

See [create-flight-plan-tcp.md](create-flight-plan-tcp.md).

### M4 — Attach weather (US082)

| Step | Expected |
|------|----------|
| Attach weather (44) | `OK` + flight summary |
| Unknown designator | `BAD_REQUEST` |
| Plan status after attach | `DRAFT` |

### M5 — Validate flight plan (US085)

| Step | Expected |
|------|----------|
| Validate bootstrap `TP085OK` | `OK\|PASS\|TP085OK` |
| Invalid / rejected plan | `OK\|FAIL\|…` |
| DSL failure case | `BAD_REQUEST` with errors |

### M6 — Regression (US078)

| Step | Expected |
|------|----------|
| ATCC login + opcodes 20–39 | Unchanged behaviour |
| Client JAR | No JDBC / persistence deps |

### M7 — US090 logging (when enabled)

| Step | Expected |
|------|----------|
| Pilot login / logout / disconnect | UDP event, service `US86` |

---

## Test data

| Item | Value |
|------|--------|
| Pilot | `pilot1` / `password123` |
| ATCC (negative) | `atcc1` / `password123` |
| DSL fixture | `aisafe.core/src/test/resources/dsl/valid.txt` |
| Validate demo | Bootstrap flight `TP085OK` |
| Create demo | Route `TP1001`, CS-TP02, modest load |

---

## Logout

Option **0** (logout) → `SUCCESS_LOGOUT` (opcode 13).
