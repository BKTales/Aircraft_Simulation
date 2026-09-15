## ANALYSIS

**Client Clarification / Product Owner Session**
- Only administrators can access the backoffice user list.
- Listed information: username, name, status (active/inactive), assigned roles.
- Passwords and sensitive data must not be displayed.
- The presentation must ensure clarity and confidentiality of data.

---

## BUSINESS RULES

* Only administrators can access this feature.
* Sensitive information must never be exposed (e.g. passwords).
* The list displays all users registered in the system, regardless of their status.

---

## UNIT TESTS

* `allUsersReturnsUsersFromService` — user list is returned correctly when authorized
* `authorizedUserCanListUsers` — authorized user receives a non-null user list
* `unauthorizedUserThrowsException` — user without permission cannot access the list
* `findUserByUsername` — searching by an existing username returns the correct user
* `findUserByUsernameNotFound` — searching by a non-existent username returns an empty Optional
* `constructorThrowsExceptionWhenAuthzIsNull` — null AuthorizationService throws IllegalArgumentException
* `constructorThrowsExceptionWhenUserSvcIsNull` — null UserManagementService throws IllegalArgumentException
* `controllerRequiresInitialization` — empty constructor without configured AuthzRegistry throws an exception