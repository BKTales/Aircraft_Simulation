## DESIGN

### Architectural Approach
Layered architecture consistent with existing project conventions:
- UI layer (console and remote TCP command entry point)
- Application layer (controller + service)
- Domain layer (aggregate + value objects)
- Persistence layer (repository interface + JPA/InMemory implementations)

Aligned with global **RouteAggregate** in `docs/sprint3/global_artifacts/DomainModel.puml`.

---

### Implemented Components (US073)

#### Domain (`eapli.aisafe.routemanagement.domain`)

- **`Route`** (aggregate root / entity, table `ROUTE`)
  - `RouteName routeName` (identity, embedded id)
  - `@ManyToOne AirTransportCompany airTransportCompany` (`operatedBy`)
  - `@ManyToOne Airport originAirport` (`origin`)
  - `@ManyToOne Airport destinationAirport` (`destination`)
  - `FlightType flightType` (reuses `eapli.aisafe.flightmanagement.domain.FlightType`: `REGULAR`, `CHARTER`)
  - `RouteSchedule routeSchedule` (embedded, only for `CHARTER`)
  - `RouteRecurringSchedule routeRecurringSchedule` (embedded, only for `REGULAR`)
  - `DeactivationDate deactivationDate` (embedded, optional — used by US074, not set in US073)

- **`RouteName`** (value object)
  - validates `^[A-Z]{2}[0-9]{1,4}$`
  - normalizes input to uppercase and trimmed value
  - two-letter prefix encodes company IATA (see global domain model note)

- **`RouteSchedule`** (value object)
  - `LocalDate scheduledDeparture`
  - `LocalDate scheduledArrival`
  - invariant: `scheduledArrival >= scheduledDeparture`

- **`RecurringScheduleEntry`** (value object, `@Embeddable`)
  - `DayOfWeek dayOfWeek`

- **`RouteRecurringSchedule`** (value object, `@Embeddable`)
  - `List<RecurringScheduleEntry> entries` via `@ElementCollection` / collection table `ROUTE_RECURRING_SCHEDULE_ENTRY`
  - invariants: list not empty, no duplicate `DayOfWeek`

- **`DeactivationDate`** (value object, optional on `Route`)
  - out of scope for US073 creation flow

**Cross-aggregate references (global domain model):**

```text
Route ..> AirTransportCompany : operatedBy (by company IATA)
Route ..> Airport            : origin / destination (by IATACode)
```

#### Repositories
- **`RouteRepository`** extends `DomainRepository<RouteName, Route>`
  - `existsByName(RouteName name)` (default method)
  - `findByCompanyIATACode(IATACode companyIataCode)` (default method)

- Implementations: `JpaRouteRepository`, `InMemoryRouteRepository`
- Factory access: `PersistenceContext.repositories().routes()`

#### Application
- **`CreateRouteController`**
  - ensures user authorization (`AIR_TRANSPORT_COMPANY_COLLABORATOR`)
  - resolves current collaborator company via `CompanyRouteCollaboratorSession`
  - delegates creation to `RouteService` (`createCharterRoute` / `createRegularRoute`)

- **`RouteService`**
  - validates route invariants and dependencies
  - checks route uniqueness (`RouteAlreadyExistsException`)
  - validates origin/destination airport existence
  - branches by route type:
    - `createCharterRoute(...)`
    - `createRegularRoute(...)`
  - persists created aggregate

- **`RouteAlreadyExistsException`** — duplicate route name

#### UI (console)
- **`CreateRouteAction`** / **`CreateRouteUI`** — backoffice menu **Routes > Create Route (US073)**
- **`RoutePrinter`** — success summary with all route parameters

#### Remote (RCOMP / US078)

- **`AtccCommandHandler`** — ATCC profile route commands:
  - `ROUTE_COMPANY_CONTEXT` (36) → `CreateRouteController.currentCompanyContext()`
  - `VALIDATE_ROUTE_NAME` (37) → suffix `\d{1,4}` + `validateRouteName(suffix, company)`
  - `LIST_ROUTE_AIRPORTS` (38) → `listAirports()` formatted via `AtccResponseFormatter`
  - `CREATE_ROUTE` (27) → payload `routeName;origin;destination;flightType;...`
    - `CHARTER` → `...;CHARTER;scheduledDeparture;scheduledArrival` (`LocalDate`)
    - `REGULAR` → `...;REGULAR;MONDAY,TUESDAY,...` (`DayOfWeek` list)

- **`CreateRouteRemoteUI`** (`aisafe.rcomp.tcpclient`) — same interactive flow as `CreateRouteUI`, over TCP

#### External dependencies
- `AirportRepository` for airport existence validation
- `CompanyCollaboratorUserRepository` for current company resolution

---

### Main Flow
1. Actor opens **Routes > Create Route (US073)** (or sends ATCC `CREATE_ROUTE`).
2. Controller resolves authenticated collaborator and company IATA via `CompanyRouteCollaboratorSession`.
3. UI asks only for route numeric suffix and composes full route name using company IATA prefix.
4. If route name fails validation/uniqueness, UI shows error and asks numeric suffix again.
5. UI requests list of airports and asks user to select origin.
6. UI requests destination options excluding selected origin and asks user to select destination.
7. UI asks for route type (`REGULAR` or `CHARTER`).
8. If type is `CHARTER`, UI collects `scheduledDeparture` and `scheduledArrival` (flexible date format `YYYY-MM-DD` or `YYYY-M-D`).
9. If type is `REGULAR`, UI collects one or more recurring `DayOfWeek` values.
10. Controller delegates to service branch for final validation and persistence.
11. Service validates business rules and saves aggregate via `RouteRepository`.
12. UI displays success summary via `RoutePrinter`.

---

### Error Flows
- Invalid route name format.
- Duplicate route name (`RouteAlreadyExistsException`).
- Origin airport not found.
- Destination airport not found.
- Origin equals destination.
- Unauthorized user or missing collaborator profile.
- Route name validation errors trigger retry loop on numeric suffix input.
- `CHARTER` with invalid schedule dates.
- `REGULAR` without recurring entries.
- `REGULAR` with duplicated recurring day.

---

### Sequence Diagram
See `us073-sd.puml`.
