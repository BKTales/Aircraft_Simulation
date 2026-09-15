## REQUIREMENTS

### Functional

1. **R1 — Simulate flights in area**  
   As a Flight Control Operator, I want to simulate flights in a given air control area from the backoffice console.

2. **R2 — Simulation parameters**  
   The operator provides simulation parameters (time range, geographic area). Weather conditions, safety thresholds, and performance settings use C simulator defaults unless extended later. All required parameters must be validated before the simulation starts.

3. **R3 — Geographic area**  
   The geographic area is an existing **Air Control Area** (US050), selected by the operator.

4. **R4 — Time range**  
   The operator specifies a start and end date-time. Only flights whose schedule overlaps this interval are eligible.

5. **R5 — Included flights from database**  
   Included flights are flight plans persisted in the system through **US080** (create flight plan) or **US081 / US121** (create flight plan from file). They are not read directly from `flight_simulator` test environment folders.

6. **R6 — Partial area simulation**  
   When a flight route **crosses** the selected area polygon but does not lie entirely inside it, only the portion of the leg inside the area is exported and simulated. When the full leg lies inside the polygon, the complete plan is simulated.

7. **R7 — Invoke C simulator**  
   Eligible plans (full or clipped) are exported to JSON files compatible with the C reader, then the existing `flight_simulator` binary is executed (processes, pipes, and signals — see `docs/scomp/`).

8. **R8 — Simulation result**  
   After execution, the operator is informed whether the simulation passed or failed and where the report files were written.

9. **R9 — Eligible-flight preview**  
   Before invoking C, the operator sees how many flights are eligible, their designators, and whether each will run as **FULL** (entire leg inside area) or **CLIPPED** (partial crossing).

10. **R10 — Authorisation**  
    Only **Flight Control Operator** users may start the simulation.

### Non-functional

11. **R11 — Layered architecture**  
    Console UI, application controller/service, repositories, geography helpers, and C subprocess integration follow the project layered architecture.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given a valid air control area, time range, and at least one eligible flight plan whose route crosses the area polygon, when the FCO runs the simulation, then `flight_simulator` executes and a pass/fail outcome is shown. |
| AC2 | Given no eligible flight plans for the selected area and interval, when the FCO attempts to run the simulation, then the operation fails before invoking C. |
| AC3 | Given an invalid date-time interval (`end` before `start`), when parameters are submitted, then validation fails before invoking C. |
| AC4 | Given a user who is not a Flight Control Operator, when the controller is invoked, then authorisation fails. |
| AC5 | Given invalid or incomplete required parameters, when the FCO submits the form, then validation fails with a clear message. |
| AC6 | Given the C simulator produces a report, when the simulation ends, then the operator can access the generated report output. |
| AC7 | Given a flight that departs outside the area but crosses it (e.g. OPO→LIS for TMA Lisboa), when the FCO previews and runs US100, then the flight is eligible, shown as **CLIPPED**, and only the in-area segment is exported to JSON. |
| AC8 | Given a flight whose leg lies entirely inside the area polygon, when US100 runs, then the flight is shown as **FULL** and the complete leg JSON is exported. |
