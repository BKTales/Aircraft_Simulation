## DESIGN

* Follow the standard layered application architecture
* **Two operational branches implemented at controller and service level:**
  - **Branch 1:** `AddPilotCollaboratorController.addUser()` → `PilotCollaboratorUserService.createPilotUser()`

**Domain Classes (with Inheritance Hierarchy):**

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
  - `securityClearance`
  - `phoneNumber`
  - `systemUser`

`PilotCertification` (Domain Entity)
* @Entity with @EmbeddedId PilotCertificationId, implementing `DomainEntity<PilotCertificationId>`
* Fields:
  - `aircraftModel` (@ManyToOne association to `AircraftModel` mapped via `AIRCRAFT_MODEL_ID`)
  - `dueDate` (@Embedded DueDate)

`PilotCertificationId` (Value Object)
* @Embeddable, UUID-backed identifier

`DueDate` (Value Object)
* @Embeddable
* Fields: `startDate` (LocalDate), `endDate` (LocalDate)
* Factory method: `valueOf(LocalDate startDate, LocalDate endDate)`

`SecurityClearance` (Value Object, inherited)
* @Embeddable
* Fields: `skillsAssessmentDate`, `expiryDate`

`Phone` (Value Object, inherited)
* @Embeddable, phone number format validation

`SystemUser` (framework class, OneToOne reference)
* NOT embedded - referenced via relationship

`PilotUserBuilder` (DDD Factory)
* Implements DomainFactory<PilotUser>
* Fluent interface with methods:
  - `withSystemUser(SystemUser)`
  - `withAirTransportCompany(AirTransportCompany)`
  - `withRole(Role)` (expects `CompanyCollaboratorRoles.PILOT`)
  - `withSecurityData(String date)` → converts to SecurityClearance
  - `withPhoneNumber(String phone)` → converts to Phone

**JPA Persistence Details:**

`PilotUser`:
* @Entity inheriting from AISafeUser via JOINED strategy
* @Id = inherited from parent (AISafeUserId)
* @Version for optimistic locking
* `pilotCertifications` mapped with `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` and `@JoinColumn(name = "PILOT_COLLABORATOR_ID")`

`PilotCertification`:
* @Entity with `@EmbeddedId` and @Version
* Managed as part of the `PilotUser` aggregate root (cascaded lifecycle)

`Repository`:
* `PilotUserRepository` extends DomainRepository<AISafeUserId, PilotUser>
* Query methods:
  - `findPilotByCompanyAndActive(AirTransportCompany company): Iterable<PilotUser>` - Returns active PILOT users for a specific company

**Controller:** `AddPilotCollaboratorController`

**Methods:**

* `currentCompany(): AirTransportCompany`
  - Retrieves: Authenticated user's company from their company collaborator profile
  - Implementation: `authz.session().authenticatedUser()` → `companyCollaboratorUserService.findATCC(collaboratorRepository, username)` → `airTransportCompany()`
  - Used by both branches to automatically determine pilot's company (no manual selection)

* `getAircraftModelIds(): String[]`
  - Returns: Available aircraft model identity strings for certification selection via `PilotCollaboratorUserService`

* `addUser(CreatePilotCollaboratorDTO dto)` - Branch 1 entry point
  - Authorization: Checks `authz.isAuthenticatedUserAuthorizedTo(AISafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)`
  - Derives company via `currentCompany()`
  - Delegates to: `PilotCollaboratorUserService.createPilotUser()` passing the extracted DTO parameters and `CurrentTimeCalendars.now()`


**Service:** `PilotCollaboratorUserService`

**Methods:**

**New User Creation:**
* `createPilotUser(username, password, firstName, lastName, email, roles, createdOn, company, securityData, phoneNumber, certifications, userRepository, collaboratorRepository, transactionalContext, aircraftModelRepository)`
  - Transaction: Explicit `txCtx.beginTransaction()`
  - Creates & Saves: new `SystemUser` via `UserBuilderHelper.builder()` saved into `UserRepository`
  - Creates: new `PilotUser` via `PilotUserBuilder` with the provided company and system user reference
  - Validates: ensuring that the roles set contains exactly one role and matches `CompanyCollaboratorRoles.PILOT`
  - Adds: Mapped `PilotCertification` instances to the entity via `aircraftModelRepository.ofIdentity()` and `LocalDate.parse()`
  - Saves: `PilotUser` to `PilotUserRepository`
  - Transaction End: `txCtx.commit()` → `txCtx.close()`

  

**Transactional Context Management:**
* The operation receives `TransactionalContext txCtx` as parameter
* `txCtx.beginTransaction()` signals explicit transaction start
* `txCtx.commit()` persists all changes within transaction scope
* `txCtx.close()` releases transaction resources
* **Exception Handling:** On any exception during transaction (validation, persistence, constraint violation), automatic rollback occurs at framework level; exception is propagated to controller
* **Null Context:** If `txCtx == null`, operations execute without explicit transaction control (framework-managed transactions)
* **Branch 1 Scope:** Transaction encompasses both SystemUser creation and PilotUser creation with certifications

**UI:** `AddPilotCollaboratorUserUI`

**UI Workflow:**
* Interacts with `AddPilotCollaboratorController` to fetch the current working company name.
* "Create User from scratch" → enter new system user information (username, password, name, email) → enter pilot security data and phone → dynamically enter certifications loop → confirm

