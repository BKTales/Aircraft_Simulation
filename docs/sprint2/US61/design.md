## DESIGN

* Follow the standard layered application architecture
* **Two operational branches implemented at controller and service level:**
  - **Branch 1:** `AddCompanyCollaboratorController.addUser()` → `CompanyCollaboratorUserService.createCollaboratorUser()`
  - **Branch 2:** `AddCompanyCollaboratorController.addATCCToExistingUser()` → `CompanyCollaboratorUserService.createATCCOnly()`

**Domain Classes (with Inheritance Hierarchy):**

`AISafeUser` (abstract parent class)
* JPA @Entity with @Inheritance(strategy = InheritanceType.JOINED)
* Fields: 
  - `id` (@EmbeddedId AISafeUserId)
  - `systemUser` (@OneToOne SystemUser)
  - `securityClearance` (@Embedded SecurityClearance)
  - `phoneNumber` (@Embedded Phone)

`CompanyCollaboratorUser` extends AISafeUser, implements AggregateRoot<AISafeUserId>
* Direct embedded fields:
  - `role` (CollaboratorRole): String-based role identifier
  - `companyIataCode` (IATACode): Column override "COMPANY_IATA_CODE"
* Inherited fields (from AISafeUser):
  - `securityClearance`
  - `phoneNumber`
  - `systemUser`

`CollaboratorRole` (Value Object)
* @Embeddable, string-based
* Factory method: `valueOf(String role)`

`IATACode` (Value Object, cross-aggregate reference)
* @Embeddable, identifies AirTransportCompany
* Column name: "COMPANY_IATA_CODE"

`SecurityClearance` (Value Object, inherited)
* @Embeddable
* Fields: `skillsAssessmentDate`, `expiryDate` (auto +5 years)
* Column overrides: "CLEARANCE_ASSESSMENT_DATE", "CLEARANCE_EXPIRY_DATE"

`Phone` (Value Object, inherited)
* @Embeddable, phone number format validation

`SystemUser` (framework class, OneToOne reference)
* NOT embedded - referenced via relationship
* Contains: email, username, firstName, lastName
* Accessed via: `systemUser().email()`, `systemUser().username()`

`CompanyCollaboratorUserBuilder` (DDD Factory)
* Implements DomainFactory<CompanyCollaboratorUser>
* Fluent interface with methods:
  - `withSystemUser(SystemUser)`
  - `withIataCode(String iataCode)` → converts to IATACode
  - `withRole(String roleType)` → converts to CollaboratorRole
  - `withSecurityData(String date)` → converts to SecurityClearance via LocalDate.parse()
  - `withPhoneNumber(String phone)` → converts to Phone

**JPA Persistence Details:**

`CompanyCollaboratorUser`:
* @Entity inheriting from AISafeUser via JOINED strategy
* @Id = inherited from parent (AISafeUserId)
* @Version for optimistic locking

`Repository`:
* `CompanyCollaboratorUserRepository` extends DomainRepository<AISafeUserId, CompanyCollaboratorUser>
* Query methods:
  - `findByCompanyIataCodeAndActive(String iataCode): Iterable<CompanyCollaboratorUser>`
  - `findByUsername(String username): Optional<CompanyCollaboratorUser>`
* Implementations: JPA (JPQL) and In-Memory (stream filtering)

**Controller:** `AddCompanyCollaboratorController`

**Methods:**
* `getCompanyIds(): String[]` - Returns available company IATA codes for Branch 1
* `getRoleTypes(): Role[]` - Returns available roles (PILOT, AIR_TRANSPORT_COMPANY_COLLABORATOR)
* `getEligibleUsers(): List<String>` - Returns emails of users eligible for ATCC assignment (Branch 2)
* `addUser(username, password, firstName, lastName, email, roles, companyId, securityData, phoneNumber)` - Branch 1 entry point
* `addATCCToExistingUser(email, areaCode, securityData, phoneNumber)` - Branch 2 entry point

**Service:** `CompanyCollaboratorUserService`

**Methods:**

**Branch 1 - New User Creation:**
* `createCollaboratorUser(username, password, firstName, lastName, email, roles, companyId, securityData, phoneNumber, userRepository, collaboratorRepository, transactionalContext)`
  - Validates: username uniqueness (via `UserRepository.existsByUsername()`)
  - Creates: new `SystemUser` via `UserBuilderHelper.builder()`
  - Saves: `SystemUser` to `UserRepository`
  - Creates: new `CompanyCollaboratorUser` via builder
  - Saves: `CompanyCollaboratorUser` to `CompanyCollaboratorUserRepository`
  - Transaction: Explicit `txCtx.beginTransaction()` → operations → `txCtx.commit()` → `txCtx.close()`

**Branch 2 - Assign ATCC to Existing User:**
* `createATCCOnly(email, companyId, securityData, phoneNumber, collaboratorRepository, transactionalContext)`
  - Looks up: `SystemUser` from cached `usersNoATCC` list (populated by `findEligibleUsersForATCC()`)
  - Throws: `EntityNotFoundException` if user not found or already has ATCC assignment
  - Creates: new `CompanyCollaboratorUser` linked to existing `SystemUser`
  - Saves: `CompanyCollaboratorUser` to `CompanyCollaboratorUserRepository`
  - Transaction: Explicit `txCtx.beginTransaction()` → operations → `txCtx.commit()` → `txCtx.close()`

* `findEligibleUsersForATCC(userRepository, collaboratorRepository): List<SystemUser>`
  - Fetches: all active users from `UserRepository.findByActive(true)`
  - Filters: users with PILOT or AIR_TRANSPORT_COMPANY_COLLABORATOR role
  - Excludes: users who already have ATCC assignment (cross-reference with `CompanyCollaboratorUserRepository.findAll()`)
  - Caches result in: `usersNoATCC` for later use in `createATCCOnly()`

* `findActiveCollaboratorsByCompany(collaboratorRepository, iataCode): Iterable<CompanyCollaboratorUser>`
  - Queries: collaborators by company IATA code and active status

**Transactional Context Management:**
* Both branches receive `TransactionalContext txCtx` as parameter
* `txCtx.beginTransaction()` signals explicit transaction start
* `txCtx.commit()` persists all changes within transaction scope
* `txCtx.close()` releases transaction resources
* On any exception: automatic rollback (framework-level guarantee)
* If `txCtx == null`: operations execute without explicit transaction control

**Repository:** `CompanyCollaboratorUserRepository`, `UserRepository`, `AirTransportCompanyRepository`

**Key Query Methods:**
* `CompanyCollaboratorUserRepository`:
  - `findByCompanyIataCodeAndActive(String iataCode): Iterable<CompanyCollaboratorUser>` - Branch 1 & 2
  - `findByUsername(String username): Optional<CompanyCollaboratorUser>` - Potential lookup
  - `findAll(): Iterable<CompanyCollaboratorUser>` - Branch 2: retrieve all existing ATCC assignments
* `UserRepository`:
  - `existsByUsername(String username): boolean` - Branch 1: validate new username uniqueness
  - `findByActive(boolean active): Iterable<SystemUser>` - Branch 2: get eligible base users
* `AirTransportCompanyRepository`:
  - `findAll()` - Retrieve available companies for UI selection

**UI:** `AddCompanyCollaboratorUserUI`

**UI Workflow:**
* Branch 1 path: "Add New Collaborator" → enter new credentials → enter company/security/phone → confirm
* Branch 2 path: "Add ATCC to Existing User" → select user from eligible list → enter company/security/phone → confirm

**Summary of Sequence Diagrams:**
* See `us61_branch1.puml` for detailed Branch 1 flow (new user creation)
* See `us61_branch2.puml` for detailed Branch 2 flow (add ATCC to existing)
