## FlightSimulationReport: Run Simulation and Generate Report

### Overview

This sequence diagram illustrates the simulation of a flight and the creation of a `FlightSimulationReport`, initiated by a **Flight Control Operator**. It demonstrates the key invariant of the **FlightSimulationReport** aggregate: a report is always tied to a specific `Flight` (by `FlightDesignator`) and must contain at least one `FlightStatus`.

### Flow Description

The Flight Control Operator interacts with the **SimulationUI**, which delegates the request to the **SimulationController**. The controller retrieves the `Flight` from the **FlightRepository** using the `FlightDesignator`, then accesses the embedded `FlightPlan` to check its current status.

The simulation can only proceed if the `FlightPlan` status is **submitted_for_simulation**. If this condition is met, the controller invokes the **C Simulator**, an external component responsible for the actual flight simulation logic. The simulator returns a set of results:

- **FlightStatuses** — the execution status of each flight (at least one required).
- **SafetyViolations** — any detected safety violations, including timestamp and position (may be empty).
- **ScheduleFlightStatuses** — the validation status of each scheduled flight (at least one required).

The controller then creates a new `FlightSimulationReport`, which enforces its own invariants before accepting the data:

- At least one `FlightStatus` must be present (`1..*`).
- `SafetyViolation` entries are optional (`0..*`).
- At least one `ScheduleFlightStatus` must be present (`1..*`).

After the report is created, the `FlightPlan` status is updated to either **sim_approved** or **sim_rejected** based on the simulation results. Both the updated `Flight` and the new `FlightSimulationReport` are then persisted to their respective repositories.

Finally, the report is returned to the Flight Control Operator via the UI.

### Key Design Decisions

| Decision | Justification |
|---|---|
| `FlightSimulationReport` references `Flight` by `FlightDesignator` | Since `FlightPlan` is now a Value Object inside `Flight`, the natural identifier for cross-aggregate reference is the `FlightDesignator`. |
| The C Simulator is treated as an external component | Simulation mechanics are explicitly out of scope for the Java domain model (per the project specification). The domain only handles the report creation and status update. |
| `FlightPlan` status is updated after the report is created | The report creation must succeed before the plan status changes, ensuring consistency — if report creation fails, the plan remains in `submitted_for_simulation`. |
| `SafetyViolation` has `0..*` multiplicity | A simulation may complete successfully with no safety violations, so this collection can legitimately be empty. |