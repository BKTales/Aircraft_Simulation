## ANALYSIS

**Client Clarification / Product Owner Session**

* N/A in current implementation

## BUSINESS RULES

* Pilots are filtered by company association: only pilots for the authenticated user's company are displayed
* Company is derived from the authenticated user's `CompanyCollaboratorUser` profile (no manual company selection)
* Only active pilots (`status == ACTIVE`) are included in the list; inactive pilots are excluded
* Authorization is dual-role: `AIR_TRANSPORT_COMPANY_COLLABORATOR`  role required

---

## UNIT TESTS

* `shouldReturnAllCompaniesWhenAuthorized`
* `shouldReturnActiveCollaboratorsForCompanyWhenAuthorized`
* `shouldThrowWhenUnauthorizedUserTriesToListPilots`
* `shouldReturnEmptyIterableWhenNoActivePilotsForCompany`
* `shouldFilterInactivePilotsFromResults`
* `shouldEnforceCompanyIsolationForNonAdminUsers`
* `shouldDisplayAllPilotsColumnsCorrectly`

---