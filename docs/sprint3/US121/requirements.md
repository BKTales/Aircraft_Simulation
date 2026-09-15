# US121 — Create a Flight Plan from a File

## User Story

As a Pilot, I want to create a valid flight plan from a file, so that I can register operational flight data in the system without manual re-entry.

There may be multiple file formats in the future; the file format must be validated before use.

---

## Requirements

### Functional

1. **R1 — Flight DSL format**
   The flight plan file must conform to the Core Flight DSL.

2. **R2 — US120 validation**
   The file must be validated according to **US120** (lexical, syntactic, and semantic analysis) before any domain object is created.

3. **R3 — Meaningful errors**
   Invalid files produce clear, informative error messages (including line/column when provided by US120). No partial or invalid flight is persisted.

4. **R4 — Import only valid plans**
   Only flight plans that pass US120 validation may be imported and used by the system (persisted `Flight` aggregate with embedded `FlightPlan`).

5. **R5 — Domain integration**
   A valid import creates or updates the relationship between:
   - **`Flight`** (aggregate root): designator, type, schedule, route name, aircraft, pilot
   - **`FlightPlan`** (embedded): status `DRAFT`, fuel load, `dslContent` (source text), `jsonContent` (simulator JSON derived from DSL)

6. **R6 — JSON representation**
   `FlightPlan.jsonContent` stores the simulator-compatible JSON produced from the validated descriptor (see [jsonMapping.md](jsonMapping.md)). This is **not** an optional export to the filesystem.

7. **R7 — Pilot context**
   The importing user must be a Pilot; `pilotId` on `Flight` is set from the authenticated session identity.

8. **R8 — Aircraft selection**
   The Pilot selects an existing aircraft registration from the system; the aircraft must exist in `AircraftRepository`.

9. **R9 — Route name**
   `routeName` is derived from the flight plan (e.g. `{firstDepartureAirport}-{lastArrivalAirport}`).

### Non-functional

10. **R10 — No duplicate designator**
    Import fails if a `Flight` with the same designator already exists.

11. **R11 — Dependency on US120**
    US121 must not duplicate DSL grammar or semantic rules; it delegates to `FlightDslParser`.

12. **R12 — Legacy US081 UI**
    Remove optional JSON file export to `flight_simulator/` from the import UI; persistence replaces that workflow.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Any regular file whose content conforms to Flight DSL is accepted |
| AC2 | Validation uses US120 (`FlightDslParser`) |
| AC3 | Invalid files show errors and are not saved |
| AC4 | Valid import persists `Flight` + `FlightPlan` via `FlightRepository` |
| AC5 | `FlightPlan.jsonContent` contains simulator JSON per `jsonMapping.md` |
| AC6 | `FlightPlan.dslContent` stores original file text |
| AC7 | `pilotId` reflects authenticated Pilot |
| AC8 | `aircraftRegistration` matches a registered aircraft |
| AC9 | Imported plan status is `DRAFT` |

---

## Relationship to Other User Stories

| US | Relationship |
|----|----------------|
| **US120** | Mandatory validation gate before import |
| **US081** (Sprint 2) | Prototype file picker + validate-only UI; superseded for persistence by US121 |
| **US080** | Manual plan creation (alternative entry path) |
| **US085** | Re-validates `dslContent` via US120 before C simulator |
| **US100** | Area simulation reads persisted flights with `jsonContent` from DB |

---

## References

- [Project_Requirements_V3.md](../../../../Project_Requirements_V3.md) — US121 acceptance criteria
- [US120/requirements.md](../US120/requirements.md) — DSL validation
- [DomainModel.puml](../global_artifacts/DomainModel.puml) — Flight aggregate
- [flight_plan0.json](../../../flight_simulator/src/data/flight_plans/flight_plan0.json) — JSON contract (SCOMP)
