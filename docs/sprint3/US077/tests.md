## TESTS

### Implementation Tests

**PilotUser (domain)**

* `deactivateFromRosterSetsSystemUserInactive`
* `deactivateFromRosterThrowsWhenAlreadyInactive`
* `toDTOMapsPilotFields`

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

* `deactivatePilotEnsuresAuthorizationAndDelegatesToService` — returns `PilotUser`, not DTO
* `deactivatePilotFailsWhenUserIsUnauthorized`
* `listActivePilotsChecksAuthAndReturnsDTOs` — DTOs produced via `PilotCollaboratorUserService` + `PilotUser.toDTO()`

**InMemoryPilotUserRepository**

* `findPilotByCompanyAndActiveReturnsOnlyActivePilots`
* `findByEmailWithLockReturnsPilotWhenPresent`
* `findByEmailWithLockReturnsEmptyWhenNotFound`

**InMemoryFlightRepository**

* `existsActiveFlightForPilotReturnsTrueForDraftOrSubmittedPlans`
* `existsActiveFlightForPilotReturnsFalseForTerminalStatusesOrNoPlan`
