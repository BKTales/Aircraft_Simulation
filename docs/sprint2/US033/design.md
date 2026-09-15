## DESIGN

* Layered architecture: UI (console), Controller, Application Service, Domain, Persistence

**Domain Classes/Value Objects:**
- `SystemUser` (aggregate root — provided by the framework)
- `Username` (VO — provided by the framework)
- `Role` (VO — provided by the framework)

**Controller:**
- `ListUsersController`: Coordinates authorisation, listing of all users

**Services:**
- `UserManagementService`: Queries and returns users (provided by the framework)

**Repositories:**
- `UserRepository`: Stores and retrieves users (provided by the framework)

**Flow (textual):**
1. Admin accesses the List Users option in the UI
2. `ListUsersController` verifies authorisation (`ADMIN`)
3. Controller invokes `allUsers()` on `UserManagementService`
4. `UserManagementService` queries the `UserRepository`
5. User list is returned to the UI without sensitive information
6. UI displays each user's username, name, status, and roles

**Tests:**
- Unit: `ListUsersControllerTest` (with fakes)