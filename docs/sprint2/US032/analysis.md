## ANALYSIS

**Client Clarification / Product Owner Session**
- Only administrators can activate or deactivate backoffice users.
- The authenticated user cannot deactivate their own account.
- The target user must be previously registered in the system.
- The operation is persisted immediately.
- The UI displays separately the list of active users (to deactivate) and inactive users (to activate).

---

## BUSINESS RULES

* Only administrators can activate/deactivate users.
* The authenticated administrator cannot deactivate their own account.
* Deactivating an already inactive user or activating an already active user is not allowed.
* The target user must exist in the system.
* The state change is persisted immediately.

---

## UNIT TESTS

* `activeUsersReturnsUsersFromService` — active user list is returned correctly when authorized
* `nonActiveUsersReturnsUsersFromService` — inactive user list is returned correctly after deactivation
* `deactivateUserSucceeds` — user is deactivated and state is persisted
* `activateUserSucceeds` — user is activated and state is persisted
* `unauthorizedUserThrowsExceptionOnActiveUsers` — user without permission cannot access the active users list
* `unauthorizedUserThrowsExceptionOnDeactivateUser` — user without permission cannot deactivate a user
* `unauthorizedUserThrowsExceptionOnActivateUser` — user without permission cannot activate a user
* `currentUserReturnsAuthenticatedUser` — authenticated user is returned correctly
* `currentUserReturnsEmptyWhenNoSession` — returns empty Optional when there is no active session
* `constructorThrowsExceptionWhenAuthzIsNull` — null AuthorizationService throws IllegalArgumentException
* `constructorThrowsExceptionWhenUserSvcIsNull` — null UserManagementService throws IllegalArgumentException
* `controllerRequiresInitialization` — empty constructor without configured AuthzRegistry throws an exception