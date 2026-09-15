# US075 — Add a Pilot to Company

## User Story
As an Air Transport Company Collaborator, I want to add a pilot to my company.
A pilot is a system user and must be certified to pilot one or more aircraft models.

---

## Requirements

### Functional

1. **R1 — Register pilot collaborator**
   An authorised ATCC can register a pilot with username, password, first name, last name,
   email, phone number, and security clearance start date. The pilot is automatically associated with the operator's company.

2. **R2 — Company association**
   The associated company is derived from the authenticated ATCC profile via `AddPilotCollaboratorController.currentCompany()`.
   The pilot is linked to exactly one air transport company (the operator's company, no manual selection required).

3. **R3 — System user**
   Each pilot is a distinct system user. No two users may share the same username or email.

4. **R4 — Role assignment**
   The collaborator is assigned the `CompanyCollaboratorRoles.PILOT` role only. Each pilot collaborator can have exactly one role.

5. **R5 — Pilot certifications**
   A pilot has one or more certifications. Each certification (`PilotCertification`) maps to an existing aircraft model (`AircraftModel`) and a validity period (`DueDate` with start date and end date).
6. **R6 — Authorisation**
   Only **AIR_TRANSPORT_COMPANY_COLLABORATOR** may register a pilot collaborator.

### Non-functional

7. **R7 — Persistence**
   The pilot collaborator aggregate (`PilotUser` which extends `AISafeUser`) and its certifications are persisted through the `PilotUserRepository`. All changes are controlled within an explicit `TransactionalContext`.

---

## Acceptance Criteria

| ID  | Criterion                                                                                                                                                                                                                 |
|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AC1 | Given a valid company, a unique username, and a unique email, when all fields are valid, then the pilot is persisted and retrievable as a system user and `PilotUser`.                                                    |
| AC2 | Given a non-existent company or session error, when registration is attempted, then an exception is raised and nothing is persisted.                                                                                      |
| AC3 | Given a username already in use, when registration is attempted, then an `IntegrityViolationException` or `ConcurrencyException` is caught and handled.                                                                   |
| AC4 | Given an email already in use, when registration is attempted, then an exception is raised and nothing is persisted.                                                                                                      |
| AC5 | Given a blank or null required field (name, email, username, phone number, certification dates), when registration is attempted, then validation fails early via `Preconditions` or `DateTimeParseException`.             |
| AC6 | Given a user who is not an ATCC, when registration is attempted, then authorisation fails. Authorization check: `authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)` must return true. |
| AC7 | Given valid certification data, when registration is completed, then each `PilotCertification` is persisted inside the `PilotUser` collection via cascade lifecycle.                                                      |

---