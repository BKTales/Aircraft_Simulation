## DESIGN

US085 follows the standard layered architecture. It adds a DSL validation gate on top of the US100 simulation engine.

### Responsibilities

| Layer | Responsibility |
|-------|----------------|
| **UI** | Collect flight designator; show result or errors |
| **Controller** | Authorisation (`PILOT`); delegate to service — no business logic |
| **Service** | Load plan, DSL gate, business rules, simulation, persistence |
| **FlightSimulationService** | Shared C simulator invocation (US100) |

### Flow

1. Pilot selects a flight plan to test.
2. `ValidateFlightPlanController` checks role and calls `ValidateFlightPlanService.validate(designator)`.
3. **Service** loads the `Flight`, verifies ownership, reads stored DSL.
4. **Service** calls `FlightDslParser` — if invalid, returns errors (line/col); simulator not called.
5. **Service** derives simulation parameters (area, interval) and passes **only the target flight**.
6. **Service** calls `FlightSimulationService` → C simulator → US109 CSV report.
7. **Service** parses report, updates `FlightPlanStatus`, saves the `Flight` aggregate.
8. Controller returns pass/fail and failure reason to UI / TCP client.

### Simulation approval (US085)

`SIM_APPROVED` requires **both**:

| Check | Source |
|-------|--------|
| Aggregate `validation_result = PASS` | CSV metrics section |
| Target flight `execution_status = SUCCESS` | CSV flights section |

The target flight is matched by simulator ID: stable hash of the flight designator (same algorithm as `FlightPlanJsonExporter.stableId`).

Reject examples (even when aggregate `validation_result = PASS`):

- `OUT_OF_FUEL`
- `LOW_ALTITUDE`

Only `FlightPlanStatus` is persisted (`SIM_APPROVED` / `SIM_REJECTED`). Report details are returned in the validation response; the CSV is not stored in the database.

### Flight plan status flow

```
DRAFT ──[US085 start]──► SUBMITTED_FOR_SIMULATION  (in-memory only, never persisted)
                                  │
                    ┌─────────────┴─────────────┐
                    ▼                           ▼
             SIM_APPROVED                 SIM_REJECTED
             (saved)                      (saved)

DRAFT ──[DSL invalid / preflight fail]──► DRAFT  (no save)
```

**Re-validation constraints:**

- `preflightChecks` requires `status == DRAFT` — `SIM_REJECTED` and `SIM_APPROVED` plans **cannot be re-validated directly**.
- To re-run US085 after a rejection or approval, the plan must return to `DRAFT`:
  - **Option A:** Create a replacement plan via US080 (`replaceFlightPlan` resets status to `DRAFT`).
  - **Option B:** Attach weather via US082 after a test (AC8) — see note below.
- `SUBMITTED_FOR_SIMULATION` is never written to the database; a crash during C process execution leaves the plan in `DRAFT`.

**AC8 — Weather voids previous test:**  
Attaching weather data via US082 after a successful or failed test (`SIM_APPROVED` / `SIM_REJECTED`) **must** reset `FlightPlanStatus` to `DRAFT`, requiring re-validation. See `FlightPlan.resetTestOnWeatherChange()`.

### Simulator invocation

`FlightSimulationService` writes self-contained JSON per eligible flight (embedded `Aircraft`, `DepartureAirport`, `ArrivalAirport`) and starts `flight_simulator` with:

| Variable | Set by |
|----------|--------|
| `FS_FLIGHT_PLANS_DIR` | Java (temp dir) |
| `FS_REPORTS_DIR` | Java (temp dir) |
| `FS_NON_INTERACTIVE` | `1` |
| `FS_TARGET_FLIGHT_ID` | stable hash of flight designator (single-flight stop) |
| `FS_WEATHER_FILE` | `weather_snapshot.json` in plans dir (when weather is attached) |

No `FS_AIRCRAFT_XML` or `FS_NETWORK_XML` — aircraft and airport data come from the flight plan JSON.

US085 passes **only the target flight** (`List.of(targetFlight)`) — no concurrent peer flights. Multi-flight area simulation with collision detection belongs to US100.

### Main components

| Layer | Component |
|-------|-----------|
| UI | `ValidateFlightPlanUI` |
| Application | `ValidateFlightPlanController`, `ValidateFlightPlanService` |
| DSL | `FlightDslParser` (US120) — used by **service** |
| Simulation | `FlightSimulationService`, `SimulationReportParser` |
| C process | `flight_simulator` (US109 report) |
| Domain | `Flight`, `FlightPlan`, `FlightPlanStatus`, `FlightSimulationReport` |

### US109 report (CSV excerpt)

```
metric,value
validation_result,PASS
flights_reported,1
collision_events,0
...

flight_id,departure,arrival,execution_status,...
```

### Remote access (US086)

| Opcode | Payload | Response |
|--------|---------|----------|
| 45 `VALIDATE_FLIGHT_PLAN` | `flightDesignator` | `OK\|PASS\|<designator>` or `OK\|FAIL\|<designator>\|<reason>` |

**Diagrams:** [us085-sd.puml](us085-sd.puml) · [us085-sd.svg](us085-sd.svg) · [us085-components.puml](us085-components.puml) · [us085-components.svg](us085-components.svg)
