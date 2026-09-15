## TESTS

### Unit tests

**DSL validation (US120)**
* Valid plan passes parsing
* Lexical, syntax, and semantic errors report line and column
* Simulator is not invoked when DSL is invalid

**ValidateFlightPlanService** (`ValidateFlightPlanServiceTest`, 32 tests — service-level invariants)

_Preflight gate:_
* Blocked when the plan has no stored DSL
* Blocked when the flight has no pilot assigned
* Only the plan owner can run the test
* Only `DRAFT` plans can be validated

_DSL gate (real parser):_
* Syntax error returns parser errors + DSL content; simulator not called
* Semantic error does not invoke the simulator

_DSL consistency (`validateDslConsistency`):_
* DSL flight id must match the flight
* DSL departure / arrival airport must match the route
* DSL departure must match the flight schedule
* Multi-leg airport gap is rejected

_Business rules (`validateBusinessRules`):_
* Blocked when the flight has no schedule
* Blocked when the route is inactive
* Blocked when the aircraft is decommissioned
* Blocked when the pilot belongs to another company
* Blocked when the pilot is not certified for the aircraft model

_Area / interval derivation & outcomes:_
* Area and time interval correctly derived from the plan (verified via `ArgumentCaptor`)
* Blocked when no air control area is crossed
* Status persisted on pass (`SIM_APPROVED`) and fail (`SIM_REJECTED`)
* Rejected when aggregate `PASS` but target flight `execution_status != SUCCESS` (e.g. `OUT_OF_FUEL`)
* Rejected when the target flight is missing from the simulation report
* `SimulatorExecutionException` ⇒ `SIM_REJECTED` persisted, with simulator log
* `onSimulationStart` callback runs before the simulation
* Simulator receives a single-flight list (target only)

_`listValidatableForPilot`:_
* Empty for a null pilot
* Returns only the owner's `DRAFT` flights
* Excludes non-`DRAFT` plans
* Sorted by scheduled departure
* Weather update (US082) voids the test result (`DRAFT`)

**Weather influence** (`FlightSimulationServiceTest`)
* Same route / aircraft / weight + different attached weather ⇒ **different `weather_snapshot.json`** while the exported `flight_plan_*.json` is **identical**; the mocked gateway reads `windSpeedMs` from `FS_WEATHER_FILE` and yields `SIM_APPROVED` (calm) vs `SIM_REJECTED` (strong wind)

**ValidateFlightPlanResult** (`ValidateFlightPlanResultTest`)
* Factory methods (`approved` / `rejected` / `blocked` / `dslFailure`) map to the correct status, message, and `dslFailure()` flag; null collections normalised to empty

**ValidateFlightPlanController** (`ValidateFlightPlanControllerTest`)
* Requires `PILOT` role; delegates `listValidatableFlights` / `validateFlightPlan` to the service
* Forwards the `onSimulationStart` callback
* Fails when there is no authenticated session; unauthorized user cannot list

**SimulationReportParser**
* Parses `validation_result` and per-flight `execution_status`
* Maps `LOW ALTITUDE` → `LOW_ALTITUDE`

### Integration scenarios

| Scenario | Expected outcome |
|----------|-----------------|
| Valid DSL, flight completes safely | `SIM_APPROVED` |
| Valid DSL, fuel exhaustion | `SIM_REJECTED` (`OUT_OF_FUEL`) |
| Valid DSL, low altitude | `SIM_REJECTED` (`LOW_ALTITUDE`) |
| Aggregate `PASS` but target flight fails | `SIM_REJECTED` (US085 gate) |
| Invalid DSL | Parser errors; simulator not called |
| Weather updated after test | Status `DRAFT`; re-test required |
| Non-owner Pilot | Authorisation failure |

### Remote (US086)

| Scenario | Expected |
|----------|----------|
| Valid designator, test passes | `OK\|PASS\|<designator>` |
| Valid designator, test fails | `OK\|FAIL\|<designator>\|<reason>` |
| Invalid DSL | `BAD_REQUEST` with parser errors |
| Pilot does not own the plan | `FORBIDDEN` or `BAD_REQUEST` |

### Manual demo (bootstrap)

After `./run-aisafe.sh --bootstrap`, login as `pilot1` / `password123`:

| Designator | Scenario | Expected |
|------------|----------|----------|
| `TP085OK` | Valid DSL + valid simulator JSON (OPO→LIS, A320) | `SIM_APPROVED` |
| `TP085DSL` | Valid JSON, invalid DSL (missing `;`) | DSL errors; simulator not called |
| `TP085SIM` | Valid DSL, self-contained JSON with 500 kg leg fuel (runs out mid-route) | `SIM_REJECTED` |

Re-bootstrap after schema reset (`./reset-db.sh`) so demo flights get updated JSON.
