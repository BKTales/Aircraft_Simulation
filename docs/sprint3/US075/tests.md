## TESTS

### Unit Tests

**DueDate**
* `ensureDatesAreRequired`
* `ensureEndDateIsNotBeforeStartDate`
* `ensureGettersExposeValues`
* `ensureProtectedConstructorForOrm`

**PilotCertification**
* `ensureConstructorRequiresAllFields`
* `ensureGettersExposeValues`

**CompanyCollaboratorUser (Pilot certifications)**
* `addPilotCertificationRejectsWhenRoleIsNotPilot`
* `addPilotCertificationAddsWhenRoleIsPilot`


### Integration / Implementation Tests

* `createPilotUserBeginsAndCommitsTransaction`
* `createPilotUserSavesSystemUserAndCollaborator`
* `createPilotUserCreatesCollaboratorWithCorrectCompany`
* `createPilotUserWithoutTransactionalContextSkipsTransactionCalls`
* `createPilotUserThrowsWhenRoleIsAtcc`
* `createPilotUserThrowsWhenUserHasMoreThanOneRole`
* `createPilotUserDoesNotCommitWhenCertificationDateIsInvalid`
* `createPilotUserReturnsDuplicateUsernameWhenIntegrityViolationOccurs`
* `createPilotUserReturnsInvalidPhoneFormatWhenExceptionIsThrown`
* `createPilotUserReturnsInvalidInputWhenPreconditionFails`
* `createPilotUserReturnsAircraftNotFoundWhenEntityDoesNotExist`
* `createPilotUserReturnsNoCertificationsErrorWhenListIsEmpty`
* `createPilotUserRollsBackTransactionWhenRepositoryFails`
* `findATCCReturnsCollaboratorWhenExists`
* `findATCCThrowsExceptionWhenCollaboratorDoesNotExist`

**AddPilotCollaboratorController**
* `addUserEnsuresAuthorizationAndDelegatesToService`
* `addUserFailsWhenUserIsUnauthorized`
* `addUserPropagatesServiceExceptionAfterAuthorization`


**InMemoryPilotUserRepository**
* `findByUsername`
* `findPilotByCompanyAndActive`

