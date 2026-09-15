# US076 — List Pilot Roster

## User Story
As an Air Transport Company Collaborator, I want to list all active pilots in my company so that I can manage pilot certifications and assignments effectively.

---

## Requirements

### Functional

1. **R1 — Retrieve pilots by company**
   An authorised user can retrieve a list of all active pilots associated with their air transport company. The company context is implicitly derived from the authenticated operator's company collaborator profile (no manual company selection allowed).

2. **R2 — Display pilot information**
   For each pilot, the system exposes via a decoupled DTO: email, first name, last name, phone number, security clearance skills assessment date, security clearance expiry date, and the total count of valid aircraft model certifications.

3. **R3 — Filter active pilots only**
   Only active pilots are included in the result set. The filter constraint is applied at the repository query level.

4. **R4 — Company isolation**
   Air Transport Company Collaborators see exclusively pilots matching their own company's IATA code. Cross-company data leakage is strictly prevented through session-anchored lookup mechanisms.

5. **R5 — Authorisation**
   Only **AIR_TRANSPORT_COMPANY_COLLABORATOR** users may list pilots.
   Authorization check: `authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)`.

### Non-functional

6. **R6 — Persistence & Architecture**
   Pilot data is retrieved via the `PilotUserRepository.findPilotByCompanyAndActive(company)` infrastructure query method.

7. **R7 — Data Decoupling**
   Domain instances of `PilotUser` must never be exposed directly to the console UI. Transformation into `ResponsePilotCollaboratorDTO` via the built-in `toDTO()` method is enforced at the application service layer.

---

## Acceptance Criteria

| ID  | Criterion                                                                                                                                                         |
|-----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AC1 | Given an authenticated Air Transport Company Collaborator, when listing pilots, then only active pilots for their specific company are returned.                  |
| AC2 | Given an authenticated user without the `AIR_TRANSPORT_COMPANY_COLLABORATOR` role, when listing pilots, then authorisation fails immediately before database hit. |
| AC3 | Given a company with no active pilots, when listing pilots, then an empty iterable collection is returned.                                                        |

---