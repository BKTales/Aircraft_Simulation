## DESIGN

* Layered architecture: UI (console), Controller, Application Service, Domain, Persistence

**Domain Classes/Value Objects:**
- `SystemUser` (aggregate root — provided by the framework)
- `Username` (VO — provided by the framework)
- `Role` (VO — provided by the framework)

**Controller:**
- `ActivateDeactivateUserController`: Coordinates authorisation, user listing, and status changes

**Services:**
- `UserManagementService`: Activates/deactivates and persists the user (provided by the framework)

**Repositories:**
- `UserRepository`: Stores and retrieves users (provided by the framework)

**Flow (textual):**
1. Admin accesses the Disable/Enable User option in the UI
2. UI displays a menu: deactivate an active user or activate an inactive user
3. Admin chooses the operation
4. `ActivateDeactivateUserController` verifies authorisation (`ADMIN`)
5. Controller returns a list of active or inactive users depending on the operation
6. UI filters the authenticated user out of the deactivation list
7. Admin selects the user from the list
8. Controller invokes `deactivateUser` or `activateUser` on `UserManagementService`
9. `UserManagementService` persists the status change
10. UI displays a confirmation message or an error message

**Tests:**
- Unit: `ActivateDeactivateUserControllerTest` (with fakes)