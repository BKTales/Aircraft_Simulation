## ANALYSIS

**Client Clarification / Product Owner Session**

“Aircraft age” (US072d) is the **physical age of the aircraft**, derived from **year of manufacture** stored at registration (US070), not from the date the company added the aircraft to the fleet.

The company is always taken from the authenticated collaborator; it is never typed by the user.

---

## BUSINESS RULES

* Only aircraft belonging to the logged-in collaborator’s company are returned
* Both **active** and **decommissioned** aircraft appear in the full fleet list
* The company is identified from the session — not provided explicitly by the user
* **US072** — list full fleet (no filter)
* **US072a** — filter by aircraft model: UI shows models **used in the company fleet**; user selects one; list matching aircraft
* **US072b** — filter by maker: UI shows manufacturers of models **used in the fleet**; user selects one; list matching aircraft
* **US072c** — filter by passenger capacity (total seats): user enters a seat count, then chooses **more than**, **less than**, or **exactly** that capacity
* **US072d** — filter by aircraft age (years): user enters an age, then chooses **more than**, **less than**, or **exactly** that age (computed from year of manufacture)

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/aircraftmanagement/`:

* Application: `ListFleetServiceTest`, `ListFleetControllerTest`
* Domain: `YearOfManufactureTest` (shared with US070)
* Repository: `AircraftRepositoryTest` (`findByOwnerCompany*`, `findActiveByOwnerCompany`)

Console: `ListCompanyFleetUI`, `ListCompanyFleetAction` (manual; no automated UI tests).

---

## UNIT TESTS (service scenarios)

* `EnsureOnlyCompanyAircraftAreReturned`
* `EnsureAllAircraftReturnedWhenNoFilterApplied`
* `EnsureFilterByModelReturnsCorrectAircraft`
* `EnsureFilterByMakerReturnsCorrectAircraft`
* `EnsureFilterByCapacityExactlyReturnsCorrectAircraft`
* `EnsureFilterByCapacityGreaterThanReturnsCorrectAircraft`
* `EnsureFilterByCapacityLessThanReturnsCorrectAircraft`
* `EnsureFilterByAgeExactlyReturnsCorrectAircraft`
* `EnsureFilterByAgeGreaterThanReturnsCorrectAircraft`
* `EnsureFilterByAgeLessThanReturnsCorrectAircraft`
* `EnsureModelsUsedInFleetReturnsDistinctModels`
* `EnsureManufacturersUsedInFleetReturnsDistinctMakers`
* `EnsureDecommissionedAircraftAreIncludedInList`
* `EnsureEmptyListReturnedWhenNoAircraftMatchFilter`
