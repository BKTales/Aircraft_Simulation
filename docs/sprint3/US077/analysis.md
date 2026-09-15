## ANALYSIS

**Client Clarification / Product Owner Session**

---

## BUSINESS RULES

* Only pilots belonging to the authenticated ATCC's company may be deactivated (R2)
* A pilot cannot be deactivated if any **pending** `Flight` (departure at or after reference time) assigned to their `SystemUser` has a `FlightPlan` in `DRAFT`, `SUBMITTED_FOR_SIMULATION`, or `SIM_APPROVED` (R3). Past flights do not block.
* Deactivation sets `SystemUser.active` to false via `SystemUser.deactivate()` — the pilot no longer appears in `findPilotByCompanyAndActive` (R1)
* An already inactive pilot (`systemUser.active == false`) cannot be deactivated again (AC6)
* The flight-plan check runs inside the same transaction as deactivation, with a **pessimistic lock** on the `PilotUser` row (`findByEmailWithLock`) to reduce race conditions in multi-client use (RCOMP + backoffice)

---

## Presentation boundary (DTOs)

* **List:** `RemovePilotCollaboratorController` → `PilotCollaboratorUserService.findActivePilotsByCompany` → each `PilotUser.toDTO()` → `ResponsePilotCollaboratorDTO` (US076 pattern; no mapper class).
* **Deactivate:** input `String` email; output `PilotUser` domain entity — **no** response DTO.

---

## UNIT TESTS

**PilotUser (domain)**

* `deactivateFromRosterSetsSystemUserInactive`
* `deactivateFromRosterThrowsWhenAlreadyInactive`

**RemovePilotCollaboratorService**

* `deactivatePilotBeginsAndCommitsTransaction`
* `deactivatePilotSucceedsWhenNoActiveFlightPlans`
* `deactivatePilotThrowsWhenHasActiveFlightPlans`
* `deactivatePilotSucceedsWhenFlightPlansAreInTerminalState`
* `deactivatePilotThrowsWhenPilotNotFound`
* `deactivatePilotThrowsWhenPilotDoesNotBelongToCompany`
* `deactivatePilotDoesNotCommitWhenConstraintViolated`
* `deactivatePilotWithoutTransactionalContextSkipsTransactionCalls`

**RemovePilotCollaboratorController**

* `deactivatePilotEnsuresAuthorizationAndDelegatesToService`
* `deactivatePilotFailsWhenUserIsUnauthorized`
* `deactivatePilotPropagatesServiceExceptionAfterAuthorization`
* `listActivePilotsChecksAuthAndReturnsDTOs`
