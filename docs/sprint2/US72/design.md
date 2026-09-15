## DESIGN

* Follow the standard layered application architecture

**Domain:** `Aircraft`, `AircraftRegistration`, `OperationalStatus`, `CabinConfiguration`, `YearOfManufacture`

Age displayed and filtered: `Aircraft.ageInYears()` → `YearOfManufacture.ageInYears()` (current year − year of manufacture).

**Application criteria:** `FleetListCriteria` + `FleetNumericComparison` (`GREATER_THAN`, `LESS_THAN`, `EQUAL`)

| Factory method | Use case |
|----------------|----------|
| `unfiltered()` | US072 |
| `byModel(AircraftModelId)` | US072a |
| `byManufacturer(ManufacturerId)` | US072b |
| `byPassengerCapacity(seats, comparison)` | US072c |
| `byAge(years, comparison)` | US072d |

**Console UI:** `ListCompanyFleetUI` — menu options 1–5; model/maker via `SelectWidget` on fleet-derived lists; capacity/age with three-way comparison submenu.

**Controller:** `ListFleetController`

* `listFleet(FleetListCriteria)`
* `modelsUsedInCompanyFleet()`
* `manufacturersUsedInCompanyFleet()`

**Service:** `AircraftService`

* `listFleet(IATACode, FleetListCriteria)` — routes to repository defaults or in-memory filtering (maker, numeric comparisons)
* `modelsUsedInFleet(IATACode)`
* `manufacturersUsedInFleet(IATACode)`

**Repository (`AircraftRepository` default methods):**

* `findByOwnerCompany(IATACode)`
* `findActiveByOwnerCompany(IATACode)` — used by US071, not US072 list
* `findByOwnerCompanyAndModel(IATACode, AircraftModelId)`
* `findByOwnerCompanyAndCapacity(IATACode, int)` — exact match helper
* `findByOwnerCompanyAndAge(IATACode, int)` — exact age helper

Maker and numeric comparisons (>, <, =) are applied in `AircraftService` over the company fleet.

**Shared:** `CompanyCollaboratorSession`

**Menu:** `MainMenu` → Company fleet → `ListCompanyFleetAction`
