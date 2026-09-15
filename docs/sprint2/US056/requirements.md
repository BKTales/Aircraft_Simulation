## REQUIREMENTS

### Functional

1. **R1 — Register engine model**  
   An authorised user can register an engine model with name, manufacturer, motorization type, thrust at static (kN), thrust at cruise (kN), fuel type, and TSFC.

2. **R2 — Manufacturer and uniqueness**  
   The manufacturer must exist. The pair (engine name, manufacturer) must be unique in the system.

3. **R3 — Thrust and TSFC**  
   Thrust values and TSFC must satisfy the invariants in `analysis.md`.

4. **R4 — Authorisation**  
   Only **Admin** or **Backoffice Operator** may invoke `CreateEngineModelController.createEngineModel`.

5. **R5 — Bootstrap**  
   Engine models can be seeded via `EngineModelsBootstrapper` using the same service rules.

### Non-functional

6. **R6 — Persistence**  
   The aggregate is persisted through `EngineModelRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given a valid manufacturer and a unique engine name for that manufacturer, when all fields are valid, then `EngineModel` is persisted and retrievable. |
| AC2 | Given a non-existent manufacturer id, when registration is attempted, then `ManufacturerNotFoundException` is raised and nothing is persisted. |
| AC3 | Given an engine name already registered for the same manufacturer, when registration is attempted, then `EngineModelAlreadyExistsException` is raised. |
| AC4 | Given thrust at static or cruise ≤ 0, or static < cruise, when registration is attempted, then domain validation fails before save. |
| AC5 | Given an invalid motorization label, when registration is attempted, then validation fails with a message listing allowed types. |
| AC6 | Given TSFC ≤ 0, when registration is attempted, then validation fails before save. |
| AC7 | Given a user who is neither Admin nor Backoffice Operator, when the controller method is invoked, then authorisation fails. |
| AC8 | Given bootstrap data, when the bootstrapper runs, then duplicate engine models are skipped or handled without corrupting existing rows. |
