## ANALYSIS

**Client Clarification / Product Owner Session**

---

## BUSINESS RULES

* Access is restricted to users with roles `BACKOFFICE_OPERATOR` or `ADMIN`. Authorization is enforced at controller level via @Restricted annotation
* User must select an Air Transport Company from existing ones
* Only active collaborators of the selected company are returned via repository query `findByCompanyIataCodeAndActive()`
* A company must exist in the system to show collaborators
* The query filters by two criteria: IATA code (company identification) AND system user active flag (systemUser.active = true)
* Filtering happens at repository level for database efficiency (JPQL WHERE clause on SystemUser.active)

---

## UNIT TESTS

* `shouldReturnAllCompaniesWhenAuthorized`
* `shouldReturnActiveCollaboratorsForCompanyWhenAuthorized`
* `shouldThrowExceptionWhenUserIsNotAuthorizedForCompanies`
* `shouldThrowExceptionWhenUserIsNotAuthorizedForCollaborators`
* `shouldReturnActiveCollaboratorsByCompany`
