## ANALYSIS

**Client Clarification / Product Owner Session**

Only **Air Transport Company Collaborator** users may decommission aircraft. The company scope is resolved from the session (`CompanyCollaboratorSession`). The UI lists **active** company aircraft and the user selects a registration; decommission is blocked if the aircraft has **pending flights** (scheduled departure in the future relative to the reference time, typically `LocalDateTime.now()`).

---

## BUSINESS RULES

* The aircraft must belong to the logged-in collaborator’s company
* Only **ACTIVE** aircraft can be decommissioned
* Decommission is **irreversible** (`OperationalStatus.DECOMMISSIONED`); the aircraft cannot be assigned to new flights
* An aircraft with at least one **pending** flight cannot be decommissioned
* Past flights (departure before reference time) do not block decommission
* Decommissioned aircraft cannot be decommissioned again

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/aircraftmanagement/`:

* Application: `DecommissionAircraftServiceTest`, `DecommissionAircraftControllerTest`
* Domain behaviour covered via `AircraftTest` (`retireFromActiveService`, `assertAssignableToNewFlight`)

Flight pending check uses `FlightRepository.existsPendingFlightForAircraft(registration, referenceTime)`.

Demo data: `FlightBootstrapper` and `AircraftBootstrapper` (module `aisafe.bootstrap`) — no automated bootstrap tests.

Console UI (`DecommissionAircraftUI`) is exercised manually; no automated UI tests.

---

## UNIT TESTS (service scenarios)

* `EnsureAircraftExistsInCompanyFleet`
* `EnsureAircraftIsActive`
* `EnsureDecommissionFailsWhenPendingFlightsExist`
* `EnsureDecommissionSucceedsWhenOnlyPastFlightsExist`
* `EnsureDecommissionedAircraftCannotBeDecommissionedAgain`
* `EnsureStatusIsTerminalAfterDecommission`
* `EnsureAircraftIsPersistedAfterDecommission`
