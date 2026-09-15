## ANALYSIS

**Client Clarification / Product Owner Session**
- Only administrators can register backoffice users.
- The following data must be collected: name, username, email, password, and role.
- The process must be available via UI and bootstrap.
- Duplicate usernames or emails are not allowed.
- Available roles: Admin, Backoffice Operator, Weather Person.
- Air Transport Company Collaborator, Pilot, and Flight Control Operator users must be registered through their dedicated user stories (US061, US075, and flight control operator management), not via US031.
- Users with internal roles (Admin, Backoffice Operator, Weather Person) must have an email whose domain belongs to the list of valid domains registered in the system.
- Users with external roles (Pilot, Air Transport Company Collaborator, Flight Control Operator) are not subject to email domain validation.

---

## BUSINESS RULES

* Username and email must be unique.
* At least one valid role must be assigned.
* Registering a user with missing data is not allowed.
* The registration must persist the user and assign the role.
* The bootstrap must create users according to the configuration.
* For internal roles, the email domain must exist in the `Email_Domain` table.
* Valid domains are registered during bootstrap (`aisafe.admin.com`, `aisafe.backoffice.com`, `aisafe.weather.com`).

---

## UNIT TESTS

### AddUserControllerTest
* `getRoleTypesReturnsCorrectRoles` — returns the three backoffice-registerable roles
* `addUserWithValidDataSucceeds` — user is registered and persisted when all data is valid and domain is valid
* `addUserWithInvalidDomainReturnsFailure` — invalid domain returns `AddUserResult.INVALID_EMAIL_DOMAIN`
* `addUserWithPilotRoleReturnsFailure` — registering a pilot via US031 returns `AddUserResult.ROLE_NOT_REGISTERABLE`
* `addUserWithAirTransportCompanyCollaboratorRoleReturnsFailure` — registering an ATCC via US031 returns `AddUserResult.ROLE_NOT_REGISTERABLE`
* `addUserWithFlightControlOperatorRoleReturnsFailure` — registering an FCO via US031 returns `AddUserResult.ROLE_NOT_REGISTERABLE`
* `unauthorizedUserReturnsFailure` — user without ADMIN permission returns `AddUserResult.UNAUTHORIZED`
* `constructorThrowsExceptionWhenAuthzIsNull` — null AuthorizationService throws `IllegalArgumentException`
* `constructorThrowsExceptionWhenUserSvcIsNull` — null UserManagementService throws `IllegalArgumentException`
* `constructorThrowsExceptionWhenAddUserSvcIsNull` — null AddUserService throws `IllegalArgumentException`
* `controllerRequiresInitialization` — empty constructor without configured AuthzRegistry throws an exception

### AddUserServiceTest
* `extractDomainFromEmailReturnsCorrectDomain` — domain is correctly extracted from a standard email address
* `extractDomainFromEmailWithSubdomain` — domain is correctly extracted from an email with a subdomain
* `validateEmailDomainForAdminWithValidDomainReturnsValid` — valid domain for ADMIN role returns valid result
* `validateEmailDomainForBackofficeOperatorWithValidDomainReturnsValid` — valid domain for BACKOFFICE_OPERATOR role returns valid result
* `validateEmailDomainForWeatherPersonWithValidDomainReturnsValid` — valid domain for WEATHER_PERSON role returns valid result
* `validateEmailDomainForAdminWithInvalidDomainReturnsFailure` — invalid domain for ADMIN role returns `INVALID_EMAIL_DOMAIN`
* `validateEmailDomainForBackofficeOperatorWithInvalidDomainReturnsFailure` — invalid domain for BACKOFFICE_OPERATOR role returns `INVALID_EMAIL_DOMAIN`
* `validateEmailDomainForWeatherPersonWithInvalidDomainReturnsFailure` — invalid domain for WEATHER_PERSON role returns `INVALID_EMAIL_DOMAIN`
* `validateEmailDomainForPilotSkipsDomainValidation` — PILOT role skips domain validation
* `validateEmailDomainForFlightControlOperatorSkipsDomainValidation` — FLIGHT_CONTROL_OPERATOR role skips domain validation
* `validateEmailDomainForAirTransportCompanyCollaboratorSkipsDomainValidation` — AIR_TRANSPORT_COMPANY_COLLABORATOR role skips domain validation
* `validateEmailDomainWithEmptyRolesReturnsFailure` — empty role set returns `NO_ROLE_SELECTED`
* `validateRegisterableRoleForAdminSucceeds` — ADMIN is allowed via US031
* `validateRegisterableRoleForBackofficeOperatorSucceeds` — BACKOFFICE_OPERATOR is allowed via US031
* `validateRegisterableRoleForWeatherPersonSucceeds` — WEATHER_PERSON is allowed via US031
* `validateRegisterableRoleForPilotReturnsFailure` — PILOT is rejected via US031
* `validateRegisterableRoleWithEmptyRolesReturnsFailure` — empty role set returns `NO_ROLE_SELECTED`
* `validateRegistrationStopsAtRoleValidationWhenRoleIsInvalid` — combined validation stops at role check