# US055 — Create an Aircraft Model

## User Story
As a Backoffice Operator, I want to register a new aircraft model so that it can be used in flight
operations. Any model must have at least one engine model (all engines are of the same type).
An aircraft model has a name and manufacturer, and their combination must be unique. At least
one certified engine model must be associated. An aircraft model will have a type, maximum range,
and other flight characteristics. This must also be achieved by a bootstrap process.

---

## Requirements

### Functional

1. **R1 — Register aircraft model**
   An authorised user can register an aircraft model with model ID, name, manufacturer, aircraft
   type, MTOW, MZFW, empty weight, wing area, wingspan, CD0, CL, service ceiling, cruise speed,
   fuel capacity, maximum range, maximum passenger seats, number of engines, and at least one
   associated engine model.

2. **R2 — Manufacturer and uniqueness**
   The manufacturer must exist in the system. The pair (model name, manufacturer) must be unique.

3. **R3 — Flight characteristics invariants**
   MTOW, MZFW, empty weight, wing area, wingspan, fuel capacity, maximum range, service ceiling,
   and cruise speed must be positive. MTOW must be greater than MZFW, and MZFW must be greater
   than empty weight. CD0 and CL must satisfy the invariants defined in `analysis.md`.

4. **R4 — Authorisation**
   Only **Admin** or **Backoffice Operator** may invoke `CreateAircraftModelController.createAircraftModel`.

5. **R5 — Bootstrap**
   Aircraft models can be seeded via `AircraftModelsBootstrapper` using the same service rules.

### Non-functional

6. **R6 — Persistence**
   The aggregate is persisted through `AircraftModelRepository`.

---

## Acceptance Criteria

| ID   | Criterion                                                                                                                                                                                    |
|------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AC1  | Given a valid manufacturer, a unique model name for that manufacturer, and at least one existing engine model, when all fields are valid, then `AircraftModel` is persisted and retrievable. |
| AC2  | Given a non-existent manufacturer ID, when registration is attempted, then `ManufacturerNotFoundException` is raised and nothing is persisted.                                               |
| AC3  | Given a model name already registered for the same manufacturer, when registration is attempted, then `AircraftModelAlreadyExistsException` is raised.                                       |
| AC4  | Given an engine model ID that does not exist in the system, when registration is attempted, then `EngineModelNotFoundException` is raised and nothing is persisted.                          |
| AC5  | Given an empty engine model list or zero engines declared, when registration is attempted, then validation fails before save.                                                                |
| AC6  | Given MTOW ≤ 0, or MZFW ≥ MTOW, or empty weight ≥ MZFW, when registration is attempted, then domain validation fails before save.                                                            |
| AC7  | Given wing area, wingspan, fuel capacity, maximum range, service ceiling, or cruise speed ≤ 0, when registration is attempted, then domain validation fails before save.                     |
| AC8  | Given an invalid aircraft type label, when registration is attempted, then validation fails with a message listing allowed types.                                                            |
| AC9  | Given a user who is neither Admin nor Backoffice Operator, when the controller method is invoked, then authorisation fails.                                                                  |
| AC10 | Given bootstrap data, when the bootstrapper runs, then duplicate aircraft models are skipped or handled without corrupting existing rows.                                                    |