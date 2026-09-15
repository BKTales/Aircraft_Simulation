# US077 — Remove a Pilot

## User Story
As an Air Transport Company Collaborator, I want to make a pilot inactive in my company's roster.
Cannot deactivate a pilot with flight plans assigned.

---

## Requirements

### Functional

1. **R1 — Deactivate pilot collaborator**
   An authorised ATCC can deactivate a pilot from their company's roster. The pilot's `SystemUser` is deactivated (`active = false`); they no longer appear in the active roster (US076).

2. **R2 — Company scope**
   The ATCC can only deactivate pilots belonging to their own company. The company is derived from the authenticated ATCC profile via `currentCompany()`.

3. **R3 — Flight plan constraint**
   A pilot cannot be deactivated if they have a **pending** flight (scheduled departure at or after the reference time) with a flight plan in `DRAFT`, `SUBMITTED_FOR_SIMULATION`, or `SIM_APPROVED`. Flights in the past do not block deactivation. `SIM_REJECTED` does not block.

4. **R4 — Authorisation**
   Only **ATCC** may deactivate a pilot collaborator.

5. **R5 — Persistence**
   The pilot's updated status is persisted through the appropriate repository.

---

## Acceptance Criteria

| ID  | Criterion                                                                                                                                                                             |
|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AC1 | Given an active pilot with no active flight plans, when deactivation is requested, then `SystemUser.active` is false and persisted.                                                    |
| AC2 | Given a pilot with a pending flight whose plan is in `DRAFT`, `SUBMITTED_FOR_SIMULATION`, or `SIM_APPROVED`, when deactivation is attempted, then an exception is raised and the pilot remains active. |
| AC3 | Given a pilot that does not belong to the authenticated ATCC's company, when deactivation is attempted, then an exception is raised.                                                  |
| AC4 | Given a user who is not an ATCC, when deactivation is attempted, then authorisation fails.                                                                                            |
| AC5 | Given a pilot email that does not exist in the roster, when deactivation is attempted, then an exception is raised and nothing is persisted.                                          |
| AC6 | Given an already inactive pilot, when deactivation is attempted, then an exception is raised.                                                                                         |