## ANALYSIS

**Client Clarification / Product Owner Session**

* N/A in current implementation

## BUSINESS RULES

* The company is selected from the existing Air Transport Company repository (IATA code)
* Collaborator email must have a valid format and is stored in lowercase
* First name and last name must be non-empty after trimming (Branch 1 only; Branch 2 uses existing user data)
* Role must be either `AIR_TRANSPORT_COMPANY_COLLABORATOR` (ATCC) or `PILOT`. Each collaborator can have exactly one role
* Security clearance assessment date is required; expiry date is auto-calculated as assessment date plus 5 years
* A newly registered collaborator is active by default with status `CollaboratorStatusType.ACTIVE`
* Username must be unique in the system
* Collaborator is uniquely identified by `AISafeUserId` (inherited from AISafeUser parent)

---

## UNIT TESTS

* `EnsureThatConstructorRequiresAllFields`
* `EnsureThatIsActiveReflectsStatus`
* `EnsureThatDeactivateChangesStatus`
* `EnsureThatIdentityIsEmail`
* `EnsureThatGettersExposeValues`
* `EnsureProtectedConstructorForORM`
* `EnsureThatBuildCreatesCollaboratorFromSystemUserData`
* `EnsureThatMissingSystemUserThrows`
* `EnsureThatAllWithMethodsReturnSameBuilder`
* `EnsureThatNonUserValuesReturnsPilotAndATTC`
