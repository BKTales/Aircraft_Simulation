# US062 — List Customer's Collaborators

## User Story
As a Backoffice Operator, I want to list all collaborators of a given customer so that I can
manage and monitor who is associated with each customer. Disabled collaborators must not appear
in the list.

---

## Requirements

### Functional

1. **R1 — List collaborators by customer**
   An authorised user can request the list of collaborators for a given customer, identified by
   IATA code (air transport company) or area code (air control area).

2. **R2 — Active only**
   Only enabled collaborators are returned. Disabled collaborators must be excluded from the list.

3. **R3 — Authorisation**
   Only **Admin** or **Backoffice Operator** may list collaborators.

### Non-functional
4. **R4 — Persistence**
   The list is retrieved through the collaborator repository.

---

## Acceptance Criteria

| ID  | Criterion |
|-----|-----------|
| AC1 | Given a customer with active collaborators, when the list is requested, then all active collaborators are returned. |
| AC2 | Given a customer with disabled collaborators, when the list is requested, then disabled collaborators are not included in the result. |
| AC3 | Given a customer with no collaborators, when the list is requested, then an empty list is returned. |
| AC4 | Given a user who is neither Admin nor Backoffice Operator, when the list is requested, then authorisation fails. |