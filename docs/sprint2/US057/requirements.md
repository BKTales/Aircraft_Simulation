## REQUIREMENTS

### Functional

1. **R1 — Add certified engine model to existing aircraft model**  
   An authorised user can select an aircraft model and add an existing engine model to its certified configurations.

2. **R2 — Existing references required**  
   The target aircraft model must exist and the engine model to add must exist.

3. **R3 — No duplicate certification**  
   The same engine model cannot be certified twice for the same aircraft model.

4. **R4 — Authorisation**  
   Only **Admin** or **Backoffice Operator** may invoke `AddEngineModelToAircraftModelController.addEngineModelToAircraftModel`.

### Non-functional

5. **R5 — Persistence**  
   The updated aggregate is persisted through `AircraftModelRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given an existing aircraft model and existing engine model not yet certified for it, when the operation is executed, then the engine model is added and the aircraft model is saved. |
| AC2 | Given a non-existent aircraft model id, when the operation is executed, then `AircraftModelNotFoundException` is raised and nothing is saved. |
| AC3 | Given a non-existent engine model id, when the operation is executed, then `EngineModelNotFoundException` is raised and nothing is saved. |
| AC4 | Given an engine model already certified for that aircraft model, when the operation is executed, then validation fails and no duplicate entry is created. |
| AC5 | Given a user who is neither Admin nor Backoffice Operator, when the controller method is invoked, then authorisation fails. |
