## ANALYSIS

**Client Clarification / Product Owner Session**

Only **Air Transport Company Collaborator** users may register aircraft. The owning company is taken from the authenticated collaborator’s profile (`CompanyCollaboratorUser.companyIataCode()`), not from user input. Operational status is set to **ACTIVE** in the application layer (not prompted in the UI).

---

## BUSINESS RULES

* An aircraft registration number (tail number) must be unique worldwide
* The aircraft must reference a valid, existing `AircraftModel`
* The engine fitted must be certified for the chosen aircraft model
* Total passenger seats (economy + business + first class) cannot exceed the model’s maximum capacity; at least one seat is required
* Registration country is ISO 3166-1 alpha-2 and may differ from the owner company’s home country
* The aircraft is added directly to the collaborator’s company fleet (`ownerCompanyIata`)
* A newly registered aircraft is **ACTIVE** by default (`OperationalStatus.ACTIVE`)
* **Year of manufacture** is collected at registration and stored on the aircraft; it represents the physical age of the aircraft (not fleet tenure)

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/aircraftmanagement/`:

* Domain: `AircraftTest`, `AircraftRegistrationTest`, `CabinConfigurationTest`, `RegistrationCountryTest`, `NumberOfFlightCrewTest`, `YearOfManufactureTest`
* Application: `AircraftServiceTest`, `RegisterAircraftControllerTest`
* Repository: `AircraftRepositoryTest` (default methods on `AircraftRepository`)

Demo data: `AircraftBootstrapper` (module `aisafe.bootstrap`) — no automated bootstrap tests.

Console UI is exercised manually; no automated UI tests for this use case.

---

## UNIT TESTS (service / domain scenarios)

* `EnsureRegistrationIsUniqueWorldwide`
* `EnsureAircraftModelExists`
* `EnsureEngineModelIsCertifiedForAircraftModel`
* `EnsureTotalSeatsDoNotExceedModelCapacity`
* `EnsureAircraftIsCreatedWithActiveStatus`
* `EnsureOwningCompanyIsSetFromSession`
* `EnsureYearOfManufactureIsStoredAndAgeIsDerived`
