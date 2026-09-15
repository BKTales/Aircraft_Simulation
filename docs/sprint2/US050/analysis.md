## ANALYSIS

**Client Clarification / Product Owner Session**

Air control area registration requires a name, polygon boundary coordinates, and minimum fuel requirement. The new boundary must not collide with already registered areas. Authorised roles: **Admin** or **Backoffice Operator**.

---

## BUSINESS RULES

* Area name is mandatory and validated by domain object
* Geographic boundary must contain enough points and represent non-zero area
* Minimum fuel requirement must be non-negative and valid by domain rules
* New area boundary must not overlap existing area boundaries
* A unique area code is generated and assigned by domain model
* On success, the new air control area is persisted
* Operation is protected by role-based authorisation in controller level

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/aircontrolarea/`:

* Application: `RegisterAirControlAreaControllerTest`, `AirControlAreaServiceTest`, `ListAirControlAreasControllerTest`
* Domain support: `AirControlAreaTest`, `AirControlAreaNameTest`, `AreaCodeTest`, `GeographicBoundaryTest`, `MinFuelRequirementTest`

---

## UNIT TESTS (service / controller scenarios)

* `EnsureRegisterAreaRequiresAuthorization`
* `EnsureBoundaryValidationIsApplied`
* `EnsureMinFuelValidationIsApplied`
* `EnsureOverlapWithExistingAreasIsRejected`
* `EnsureValidAreaIsPersisted`
