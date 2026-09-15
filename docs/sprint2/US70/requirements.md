## REQUIREMENTS

### Functional

1. **R1 — Register aircraft**  
   An air transport company collaborator can register an aircraft with tail number, aircraft model, certified engine model, cabin seats per class, registration country, flight crew count, and year of manufacture.

2. **R2 — Uniqueness and references**  
   Registration must be unique worldwide. Model must exist. Engine must be certified for the model. Total seats must not exceed model capacity.

3. **R3 — Company from session**  
   Owner company is the collaborator’s company IATA from the session; it is not entered in the form.

4. **R4 — Default status**  
   New aircraft are persisted with `OperationalStatus.ACTIVE` without user input.

5. **R5 — Year of manufacture**  
   Year of manufacture is mandatory at registration and is used later to compute aircraft age (US072d).

6. **R6 — Authorisation**  
   Only users with role **Air Transport Company Collaborator** registered in `CompanyCollaboratorUser` may register aircraft.

7. **R7 — Bootstrap**  
   Demo aircraft can be seeded via `AircraftBootstrapper` after models and certifications exist.

### Non-functional

8. **R8 — Persistence**  
   The aggregate is persisted through `AircraftRepository` (JPA in production, in-memory in tests).

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given valid data and a unique registration, when registration runs, then the aircraft is saved as ACTIVE for the collaborator’s company. |
| AC2 | Given a registration already in use, when registration runs, then `DuplicateAircraftRegistrationException` is raised. |
| AC3 | Given an unknown model id, when registration runs, then `AircraftModelNotFoundException` is raised. |
| AC4 | Given an engine not certified for the model, when registration runs, then validation fails before save. |
| AC5 | Given total seats above the model maximum, when registration runs, then validation fails before save. |
| AC6 | Given a user who is not a registered company collaborator, when the controller is invoked, then the operation fails. |
| AC7 | Given a valid year of manufacture, when the aircraft is listed later, then displayed age equals current year minus that year. |
