## DESIGN

* Follow the standard layered application architecture

**Domain Classes:**

`CompanyCollaboratorUser` (shared aggregate with US61)
* Extends AISafeUser (same hierarchy as US61)
* Contains: role, companyIataCode, and inherited fields
* Active filtering relies on SystemUser.active (from AISafeUser)

**Repository Specifications:**

`CompanyCollaboratorUserRepository.findByCompanyIataCodeAndActive(String iataCode)`
* WHERE clause: companyIataCode = ? AND systemUser.active = true
* Returns: Iterable<CompanyCollaboratorUser> of active collaborators only
* Used by: ListCompanyCollaboratorUsersController
* Database efficiency: query executed at repository level
* JPA: JPQL query with named parameters
* In-Memory: Stream filter with .filter(c -> c.companyIataCode().equals(iataCode) && c.systemUser().isActive())

**Application Layer:**

`ListCompanyCollaboratorUsersController`
* Authorization: @Restricted({"BACKOFFICE_OPERATOR", "ADMIN"})
* Methods:
  - `allAirTransportCompanies()`: Returns all companies for user selection
  - `activeCompanyCollaboratorsUsersForCompany(String iataCode)`: Delegates to service for filtered active collaborators

`CompanyCollaboratorUserService`
* `findActiveCollaboratorsByCompany(repository, iataCode)`: Delegates to repository.findByCompanyIataCodeAndActive(iataCode)
* Returns only active collaborators efficiently

**Controller:** `ListCompanyCollaboratorUsersController`

**Service:** `CompanyCollaboratorUserService`

**Repository:** `CompanyCollaboratorUserRepository`, `AirTransportCompanyRepository`

**UI:** `ListCompanyCollaboratorUsersUI`
