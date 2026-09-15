## REQUIREMENTS

### Functional

1. **R1 — Decommission aircraft**  
   A company collaborator can retire an active aircraft from service by selecting it from the company’s active fleet.

2. **R2 — Company scope**  
   Only aircraft owned by the collaborator’s company may be decommissioned.

3. **R3 — Pending flights**  
   Decommission is rejected if the aircraft has at least one flight whose scheduled departure is after the reference time.

4. **R4 — Terminal state**  
   After decommission, status is `DECOMMISSIONED` and cannot return to `ACTIVE`.

5. **R5 — New flight assignment**  
   A decommissioned aircraft must not be assignable to new flights (`assertAssignableToNewFlight`).

6. **R6 — Authorisation**  
   Only **Air Transport Company Collaborator** users registered for a company may invoke the use case.

### Non-functional

7. **R7 — Persistence**  
   Status change is persisted through `AircraftRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given an active company aircraft with no pending flights, when decommission runs, then status becomes DECOMMISSIONED and is persisted. |
| AC2 | Given an aircraft with a future scheduled flight, when decommission runs, then `AircraftHasPendingFlightsException` is raised and status stays ACTIVE. |
| AC3 | Given an aircraft owned by another company, when decommission runs, then `AircraftNotInCompanyFleetException` is raised. |
| AC4 | Given an unknown registration, when decommission runs, then `AircraftNotFoundException` is raised. |
| AC5 | Given an already decommissioned aircraft, when decommission runs again, then `AircraftAlreadyDecommissionedException` is raised. |
| AC6 | Given only past flights for the aircraft, when decommission runs, then it succeeds. |
