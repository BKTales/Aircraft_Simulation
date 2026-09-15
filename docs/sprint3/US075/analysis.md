## ANALYSIS

The `PilotUser` acts as an **Aggregate Root**, containing and managing a collection of `PilotCertification` internal entities. The certifications are mapped directly to an `AircraftModel` aggregate, establishing a strict dependency where a certification cannot exist without a valid target aircraft model.

---

## BUSINESS RULES

* **Automatic Company Derivation:** The pilot's company is automatically derived from the authenticated ATCC (Air Transport Company Collaborator) profile via the `currentCompany()` method. No manual company selection is permitted in the UI.
* **System User Lifecycle (Branch 1):** First name, last name, username, and email must be non-empty and valid. Username and Email must be globally unique across the system framework.
* **Security Clearance & Phone:** The pilot's security data (assessment date) and telephone number are encapsulated as embedded value objects (`SecurityClearance` and `Phone`), with early format and presence validations.
* **Certification Invariance:** A pilot must have one or more certifications. Each `PilotCertification` mandates a valid `AircraftModel` reference and a valid `DueDate` period.
* **Date Constraints:** Inside `DueDate`, the start date must be chronologically before the end date (`startDate < endDate`).

---

## UNIT TESTS

* `ensureDatesAreRequired`
* `ensureEndDateIsNotBeforeStartDate`
* `ensureGettersExposeValues`
* `ensureProtectedConstructorForOrm`
* `ensureConstructorRequiresAllFields`
* `ensureGettersExposeValues`
* `addPilotCertificationRejectsWhenRoleIsNotPilot`
* `addPilotCertificationAddsWhenRoleIsPilot`
* `createPilotUserBeginsAndCommitsTransaction`
* `createPilotUserSavesSystemUserAndCollaborator`
* `createPilotUserCreatesCollaboratorWithCorrectCompany`
* `createPilotUserWithoutTransactionalContextSkipsTransactionCalls`
* `createPilotUserThrowsWhenRoleIsAtcc`
* `createPilotUserDoesNotCommitWhenSecurityDataDateIsInvalid`
* `createPilotUserThrowsWhenUserHasMoreThanOneRole`
* `createPilotOnlyCreatesCollaboratorForEligibleUser`
* `createPilotOnlyThrowsWhenEmailNotInEligibleList`
* `addUserEnsuresAuthorizationAndDelegatesToService`
* `addUserFailsWhenUserIsUnauthorized`
* `addUserPropagatesServiceExceptionAfterAuthorization`
