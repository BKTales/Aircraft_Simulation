# US061 — Add a Customer's Collaborator

## User Story
As a Backoffice Operator, I want to register a customer's collaborator so that they become a
user of the system. A customer may be an air transport company or an air control area. Each
collaborator is a distinct system user. There is no need to verify that the collaborator's email
belongs to the customer's domain. This must also be achievable by a bootstrap process.

---

## Requirements

### Functional

1. **R1 — Register collaborator**
   An authorised user can register a collaborator with username, password, first name, last name,
   email, phone number, security data, and an associated customer (air transport company or air
   control area).

2. **R2 — Customer association**
   The associated customer must exist in the system. The collaborator must be linked to exactly
   one customer — either an air transport company (via IATA code) or an air control area
   (via area code).

3. **R3 — System user**
   Each collaborator is a distinct system user. No two collaborators may share the same username
   or email.

4. **R4 — Email domain**
   There is no requirement to verify that the collaborator's email belongs to the customer's domain.

5. **R5 — Role assignment**
   The collaborator is assigned the appropriate role based on the customer type: Pilot or
   Air Transport Company Collaborator for air transport companies; Flight Control Operator
   for air control areas.

6. **R6 — Existing user**
   If the user already exists in the system, it must be possible to associate them with a customer
   without creating a new system user.

7. **R7 — Authorisation**
   Only **Backoffice Operator** may register a collaborator.

8. **R8 — Bootstrap**
   Collaborators can be seeded via a bootstrapper using the same service rules.

### Non-functional

9. **R9 — Persistence**
   The collaborator aggregate is persisted through the appropriate repository.

---

## Acceptance Criteria

| ID  | Criterion                                                                                                                                                       |
|-----|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AC1 | Given a valid customer, a unique username, and a unique email, when all fields are valid, then the collaborator is persisted as a system user and retrievable.  |
| AC2 | Given a non-existent customer ID, when registration is attempted, then an exception is raised and nothing is persisted.                                         |
| AC3 | Given a username already in use, when registration is attempted, then an exception is raised and nothing is persisted.                                          |
| AC4 | Given an email already in use, when registration is attempted, then an exception is raised and nothing is persisted.                                            |
| AC5 | Given an existing system user, when association with a customer is requested, then the collaborator record is created without creating a duplicate system user. |
| AC6 | Given a blank or null required field (name, email, username, phone number), when registration is attempted, then validation fails before save.                  |
| AC7 | Given a user who is not a Backoffice Operator, when registration is attempted, then authorisation fails.                                                        |
| AC8 | Given bootstrap data, when the bootstrapper runs, then duplicate collaborators are skipped or handled without corrupting existing rows.                         |