## DESIGN

* Layered architecture: UI (console), Controller, Application Service, Domain, Persistence

**Domain Classes/Value Objects:**

- `SystemUser` (aggregate root — provided by the framework)
- `Username` (VO — provided by the framework)
- `Password` (VO — provided by the framework)
- `Role` (VO — provided by the framework)
- `EmailDomain` (aggregate root — own domain)

**Main Relationships:**

- `SystemUser` holds a username, email, password, name, and set of roles
- `EmailDomain` is an independent entity representing a valid email domain for internal roles

**Controller:**

- `AddUserController`: Responsible for the manual registration and bootstrap flow; coordinates authorisation, domain validation, and persistence

**Services:**

- `AddUserService`: Validates role and email domain for internal roles by querying the `EmailDomainRepository`; returns `AddUserValidationResult`
- `UserManagementService`: Registers and persists the user (provided by the framework)

**Result types:**

- `AddUserValidationResult`: Outcome of business-rule validation in the application service
- `AddUserResult`: Rich return type for the full register-user use case (US031)

**Flow (textual):**

1. Admin initiates registration via UI or bootstrap
2. Enters data: username, password, name, email, role
3. `AddUserController` verifies authorisation (`ADMIN`)
4. `AddUserController` invokes `AddUserService.validateRegistration()`
5. If the role is internal, `AddUserService` queries the `EmailDomainRepository`
6. If validation fails, returns `AddUserResult` with the corresponding outcome and message
7. If valid, `UserManagementService` persists the user and returns `AddUserResult.success`
8. UI displays a confirmation message or the result message

**Tests:**

- Unit: `EmailDomain`, `AddUserService`
- Integration: `AddUserController`, `AddUserControllerTest` (with fakes)