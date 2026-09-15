## DESIGN

* Follow the standard layered application architecture
* **Single operational flow:** List active pilots for the authenticated user's company (no manual company selection allowed).

**Domain Classes (Reuse from US075):**

`AISafeUser` (abstract parent class)
* JPA @Entity with @Inheritance(strategy = InheritanceType.JOINED)
* Fields:
  - `id` (@EmbeddedId AISafeUserId)
  - `systemUser` (@OneToOne SystemUser)
  - `securityClearance` (@Embedded SecurityClearance)
  - `phoneNumber` (@Embedded Phone)

`PilotUser` extends AISafeUser, implements AggregateRoot<AISafeUserId>
* Direct fields:
  - `airTransportCompany` (AirTransportCompany): @ManyToOne association mapped via `AIR_TRANSPORT_COMPANY_IATA`
  - `pilotCertifications` (List<PilotCertification>): @OneToMany with `CascadeType.ALL` and `orphanRemoval = true`, mapped with `@JoinColumn(name = "PILOT_COLLABORATOR_ID")`
* Inherited fields (from AISafeUser):
  - `securityClearance` (with `skillsAssessmentDate` and `expiryDate`)
  - `phoneNumber`
  - `systemUser`

`PilotCertification` (Domain Entity)
* @Entity with @EmbeddedId PilotCertificationId, implementing `DomainEntity<PilotCertificationId>`
* Fields:
  - `aircraftModel` (@ManyToOne association to `AircraftModel` mapped via `AIRCRAFT_MODEL_ID`)
  - `dueDate` (@Embedded DueDate)

**Data Transfer Objects (DTOs):**

`ResponsePilotCollaboratorDTO` (Data Transfer Object)
* Pure data carrier class used to transfer information from the domain/service layer safely to the presentation layer.
* Contains flattened properties: `email`, `firstName`, `lastName`, `phoneNumber`, `skillsAssessmentDate`, `expiryDate`, and `certificationCount`.

**JPA Persistence Details:**

`PilotUser`:
* @Entity inheriting from AISafeUser via JOINED strategy
* @Id = inherited from parent (AISafeUserId)
* @Version for optimistic locking
* Mapped with `PilotUserRepository`

`Repository`: `PilotUserRepository` extends DomainRepository<AISafeUserId, PilotUser>
* Query methods:
  - `findPilotByCompanyAndActive(AirTransportCompany company): Iterable<PilotUser>` - Handles target company lookup for active pilots.

**Controller:** `ListPilotUsersController`

**Methods:**

* `allAirTransportCompanies(): Iterable<AirTransportCompany>`
  - Authorization: Checks `authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)`
  - Returns: All air transport companies from `AirTransportCompanyRepository` (used for standalone information retrieval).

* `activePilotUsersForCompany(): Iterable<ResponsePilotCollaboratorDTO>`
  - Authorization: Checks `authz.ensureAuthenticatedUserHasAnyOf(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)`
  - Company context: Derived from the authenticated operator's company collaborator profile via:
    - `authz.session().authenticatedUser()` → retrieves the logged-in `SystemUser`
    - `collaboratorUserService.findATCC(collaboratorUserRepository, user.username())` → retrieves the `CompanyCollaboratorUser` profile
    - `userCollab.airTransportCompany()` → extracts the target company aggregate reference
  - Delegates to: `pilotUserService.findActivePilotsByCompany(pilotUserRepository, company)`
  - Returns: An iterable collection of decoupled `ResponsePilotCollaboratorDTO` objects.
  - Data isolation: The working company is strictly anchored to the user's session context; no parameter override or cross-company data visibility is allowed.

**Service:** `PilotCollaboratorUserService` (existing, method reuse)

**Methods:**

* `findActivePilotsByCompany(PilotUserRepository collaboratorRepository, AirTransportCompany airTransportCompany): Iterable<ResponsePilotCollaboratorDTO>`
  - Retrieves: `collaboratorRepository.findPilotByCompanyAndActive(airTransportCompany)`
  - Mapping: Iterates over the matching active `PilotUser` entities, invokes their inner `pilot.toDTO()` logic, and builds an array/list of `ResponsePilotCollaboratorDTO`.
  - Returns: Mapped DTO list ready for presentation output.

---