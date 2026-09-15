## DESIGN

### Architectural Approach

Same layered style as US073:

- UI (console + RCOMP)
- Application (controller + service)
- Domain (`Route`, `DeactivationDate`)
- Persistence (`RouteRepository`, `FlightRepository`)

Bounded context: `eapli.aisafe.routemanagement` (application) with cross-context read via `FlightRepository`.

---

### Implemented Components (US074)

#### Domain (`eapli.aisafe.routemanagement.domain`)

- **`Route`** (extend existing aggregate)
  - add method e.g. `void deactivate(DeactivationDate date)`
  - invariant: `deactivationDate` must be null before assign
  - does not modify schedule embeddables (`RouteSchedule` / `RouteRecurringSchedule`)

- **`DeactivationDate`** (existing value object)
  - `LocalDate value()`, not null when constructed

#### Repositories

- **`RouteRepository`** (existing)
  - `ofIdentity(RouteName)`, `findByCompanyIATACode` for UI listing active routes

- **`FlightRepository`** (extend in `flightmanagement`)
  - new query, e.g.:
    ```java
    boolean existsPlannedFlightOnRouteAfter(RouteName routeName, LocalDate deactivationDate);
    ```
  - JPA implementation filters:
    - `UPPER(TRIM(e.routeName)) = UPPER(TRIM(:routeName))` (or exact match per project convention)
    - `e.schedule IS NOT NULL`
    - `e.schedule.scheduledDeparture >= :startOfDeactivationDay` (as `LocalDateTime` at start of day or date comparison in JPQL)
    - `(e.flightPlan IS NULL OR e.flightPlan.status IN (:draft, :submitted, :approved))`

#### Application (`eapli.aisafe.routemanagement.application`)

- **`DeactivateRouteService`**
  - `deactivateRoute(String routeName, LocalDate deactivationDate, AirTransportCompany company)`
  - `listActiveRoutesByCompany`, `listActiveRouteOptionsByCompany` (with `findLastPlannedFlightDepartureDateOnRoute` hint)
  - steps:
    1. validate date not in the past
    2. load route; verify company ownership and route is active
    3. if `flights.existsPlannedFlightOnRouteAfter(...)` → throw `RouteHasPlannedFlightsException`
    4. `route.deactivate(DeactivationDate.valueOf(date))` + `routes.save(route)`

- **`DeactivateRouteController`**
  - `@UseCaseController`, same dependency pattern as `CreateRouteController`
  - `CompanyRouteCollaboratorSession` for company scope
  - methods: `listActiveRoutes()`, `listActiveRouteOptions()`, `deactivateRoute(String routeName, LocalDate date)`

- **`ActiveRouteOption`** — route + optional last planned flight departure for UI table

- **Exceptions**
  - `RouteAlreadyDeactivatedException`
  - `RouteHasPlannedFlightsException`
  - reuse / align with US073: route not found, wrong company → `IllegalArgumentException` or dedicated types

- **`RouteService.assertRouteAcceptsNewFlight`** — R14 consumer helper (implemented)

#### UI (console)

- **`DeactivateRouteAction`** / **`DeactivateRouteUI`**
  - menu: **Routes > Deactivate Route (US074)**
  - list active routes via `listActiveRouteOptions()` (`DeactivateRouteListPrinter` table with last planned flight hint)
  - prompt deactivation date (flexible `YYYY-MM-DD` or `YYYY-M-D`)
  - `RoutePrinter.printDeactivationSummary` on success

#### Remote (RCOMP)

- **`AtccCommandHandler`**
  - `LIST_ACTIVE_ROUTES` (39) → `DeactivateRouteController.listActiveRoutes()`
  - `DEACTIVATE_ROUTE` (28) — payload `routeName;deactivationDate` (`uuuu-MM-dd`)
  - response `OK|{formatted route}` via `AtccResponseFormatter`

- **`DeactivateRouteRemoteUI`** — same flow as console, over TCP (US078)

#### External dependencies

- `FlightRepository` — planned flight guard
- `CompanyCollaboratorUserRepository` — session company (via `CompanyRouteCollaboratorSession`)

---

### Main Flow (console)

1. ATCC opens **Routes > Deactivate Route (US074)**.
2. Controller resolves company via `CompanyRouteCollaboratorSession`.
3. UI lists **active** routes for that company.
4. ATCC selects route and enters deactivation date.
5. Controller delegates to `DeactivateRouteService`.
6. Service loads route, validates ownership, active state, date, and planned flights.
7. Service sets `deactivationDate` and saves.
8. UI prints summary.

---

### Error Flows

| Condition | Outcome |
|-----------|---------|
| Unauthorized user | Authorization failure |
| Route not found | Error, no change |
| Route belongs to another company | Error, no change |
| Route already deactivated | `RouteAlreadyDeactivatedException` |
| `deactivationDate` in the past | Validation error |
| Blocking planned flights (R6) | `RouteHasPlannedFlightsException` |
| Invalid date format (UI) | Prompt retry |

---

### Sequence Diagram

See `us074-sd.puml`.

---

### Database

- Column `DEACTIVATION_DATE` on table `ROUTE` already mapped by `@Embedded DeactivationDate`.
- No schema migration required if US073 table is deployed.

---

### Testing strategy (see `tests.md`)

- Domain: `Route.deactivate`, already deactivated, null date
- Service: success, blocked by flight, wrong company, past date
- Repository: `existsPlannedFlightOnRouteAfter` scenarios (A–E table)
- Controller: authorization, delegation
- Integration: in-memory repositories with flight fixtures
