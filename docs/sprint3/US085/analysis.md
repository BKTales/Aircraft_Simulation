## ANALYSIS

### Clarifications

* In US085 the Pilot does **not** upload a DSL file. The DSL is already stored in the flight plan (from US080 or US121).
* US080 creates the plan in **DRAFT** without running validation. US085 is the first validation gate.
* If US082 updates weather data after a test, the result is void and the Pilot must run US085 again.
* **PO clarification:** US085 simulates **only the selected flight plan**. No concurrent `SIM_APPROVED` peers. Validation scope is **fuel** and **altitude** via the C simulator. Collision detection is **out of scope** (US100).

### Business rules

* The stored DSL is the only input.
* DSL must pass US120 validation before the C simulator runs.
* Simulation parameters are derived from the plan:
  - **Area:** air control area(s) crossed by the route (used to invoke `FlightSimulationService`).
  - **Interval:** from earliest departure to latest arrival across all legs.
  - **Eligible flights:** only the target flight (`List.of(target)`).
  - **Simulation window:** departure → arrival of the target leg; C stops when target lands (`FS_TARGET_FLIGHT_ID`).
* The C simulator is shared with US100; US085 adds the DSL gate and single-flight invocation.
* US085 approval requires aggregate CSV `PASS` **and** target flight `execution_status = SUCCESS` (fuel/altitude failures surface as `OUT_OF_FUEL` / `LOW_ALTITUDE`).
* Only the Pilot who owns the plan may run the test.
* After a successful test, weather changes (US082) reset the plan to **DRAFT**.

### Plan status lifecycle

```
DRAFT → SUBMITTED_FOR_SIMULATION → SIM_APPROVED  (test passed)
                                 → SIM_REJECTED  (test failed)
```

Weather update (US082) after test → back to **DRAFT**.
