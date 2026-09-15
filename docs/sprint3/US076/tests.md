## TESTS

### Unit Tests

**ListPilotUsersController**
* `allAirTransportCompaniesReturnsAllCompaniesWhenAuthorized`
* `allAirTransportCompaniesFailsWhenUnauthorized`
* `activePilotUsersForCompanyReturnsOnlyActiveUsers`
* `activePilotUsersForCompanyReturnsOnlyForAuthenticatedUserCompany`
* `activePilotUsersForCompanyFiltersInactivePilots`
* `activePilotUsersForCompanyThrowsWhenUnauthorized`
* `activePilotUsersForCompanyReturnsEmptyWhenNoActivePilots`

### Integration / Implementation Tests

**InMemoryCompanyCollaboratorUserRepository**
* `findPilotByCompanyAndActiveReturnsPilotsOnly`
* `findPilotByCompanyAndActiveFiltersInactivePilots`
* `findPilotByCompanyAndActiveReturnsEmptyWhenNoPilotsForCompany`

**UI Integration (Console)**
* `ListPilotUsersUIDisplaysCorrectColumnsForEachPilot`
* `ListPilotUsersUIFormatsSecurityClearanceDatesCorrectly`
