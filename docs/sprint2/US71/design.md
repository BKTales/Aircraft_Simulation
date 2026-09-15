## DESIGN

* Follow the standard layered application architecture

**Domain:** `Aircraft`, `OperationalStatus`

Decommission is a state change on the aggregate: `Aircraft.retireFromActiveService()` sets `DECOMMISSIONED` and rejects repeat calls.

**Console UI:** `DecommissionAircraftUI` — lists active fleet via controller; user selects registration from `SelectWidget`.

**Controller:** `DecommissionAircraftController`

* `listActiveCompanyAircraft()` — active aircraft for session company
* `decommissionAircraft(registration)` — uses `LocalDateTime.now()` as reference time

**Service:** `DecommissionAircraftService` (not `AircraftService`)

| Step | Responsibility |
|------|----------------|
| Load | `AircraftRepository.findByRegistration` |
| Authorise fleet | Compare `ownerCompanyIata` with session company |
| Guard | Reject if not active; reject if pending flights |
| Mutate | `retireFromActiveService()` |
| Persist | `AircraftRepository.save` |

**Repositories:** `AircraftRepository`, `FlightRepository`

**Exceptions (application):** `AircraftNotFoundException`, `AircraftNotInCompanyFleetException`, `AircraftAlreadyDecommissionedException`, `AircraftHasPendingFlightsException`

**Shared:** `CompanyCollaboratorSession`

**Bootstrap / demo:** `FlightBootstrapper` may attach a future flight to `CS-DEMO` to demonstrate the pending-flight rule; `CS-TP02` is suitable for a successful decommission demo.
