## ANALYSIS

### Business Goal
Allow an Air Transport Company Collaborator to define company routes that can later be used by flight planning features.

### Scope (US073 only)
- Create route for authenticated collaborator's company.
- Validate route naming and endpoint constraints.
- Capture route operational type (`REGULAR` or `CHARTER`).
- Capture route schedule model according to selected type.
- Persist route.

### Out of Scope
- Route deactivation rules from US074 (`DeactivationDate` exists in domain but is not set on create).
- Flight scheduling on route.
- DSL/import integration.

---

## Assumptions

1. Route name uniqueness is **global** in the system (not per company).
2. Airport input for this US uses **IATA airport code**.
3. Company is inferred from authenticated collaborator and cannot be manually selected.
4. The route name prefix (2 letters) is inferred from the collaborator company IATA; user enters only numeric suffix.
5. `REGULAR` routes store only recurring days (`DayOfWeek`), not concrete calendar dates.

---

## Business Rules

1. Only `AIR_TRANSPORT_COMPANY_COLLABORATOR` can create routes.
2. Route must belong to the collaborator's company (`companyIATACode`).
3. Route name format: `^[A-Z]{2}[0-9]{1,4}$`.
4. Route name must be unique.
5. Origin airport must exist.
6. Destination airport must exist.
7. Origin and destination must be different.
8. Destination options presented to the user must exclude the selected origin airport.
9. On route name validation error, the user must be prompted to retry route numeric suffix input.
10. Route must define exactly one scheduling model:
    - `CHARTER` → `RouteSchedule(scheduledDeparture, scheduledArrival)`
    - `REGULAR` → `RouteRecurringSchedule` with one or more `RecurringScheduleEntry` (`DayOfWeek`)
11. For `CHARTER`, `scheduledArrival` cannot be before `scheduledDeparture`.
12. For `REGULAR`, recurring days must be unique and contain at least one entry.

---

## Domain Decisions

- Introduce **`Route`** aggregate root (entity name aligned with global `RouteAggregate`).
- Introduce **`RouteName`** value object to centralize route name invariant.
- Persist **`operatedBy`**, **`origin`** and **`destination`** as `@ManyToOne` JPA associations to `AirTransportCompany` and `Airport` (domain model cross-aggregate links).
- Reuse **`FlightType`** enum (`REGULAR`, `CHARTER`) from `flightmanagement` to avoid duplicate type vocabularies.
- Introduce **`RouteSchedule`**, **`RouteRecurringSchedule`** and **`RecurringScheduleEntry`** value objects.
- **`RouteRecurringSchedule`** uses `@ElementCollection` of embeddable `RecurringScheduleEntry` for JPA persistence.
- Optional **`DeactivationDate`** on `Route` reserved for US074.

---

## Risks and Open Questions

1. **Uniqueness interpretation:** if Product Owner later clarifies uniqueness per company, repository constraints must change.
2. **Airport identifier choice:** if UI receives ICAO instead of IATA, conversion or alternate fields will be needed.
3. **US074 alignment:** deactivation must preserve the schedule model already stored in US073.
4. **Database migration:** JPA table name is `ROUTE` (not `FLIGHT_ROUTE`); existing PostgreSQL schemas may need bootstrap/reset after rename.

---

## Implementation Status

| Layer | Status | Notes |
|-------|--------|-------|
| Domain | Done | `Route`, value objects, charter/regular invariants |
| Application | Done | `CreateRouteController`, `RouteService`, `CompanyRouteCollaboratorSession` |
| Persistence | Done | `JpaRouteRepository`, `InMemoryRouteRepository` |
| Console UI | Done | `CreateRouteUI`, `RoutePrinter`, menu in `MainMenu` |
| Remote TCP | Done | Server handler + `CreateRouteRemoteUI` (US078) |
| Unit tests | Done | See [tests.md](tests.md) — `aisafe.core` routemanagement package |
| Handler integration tests | Partial | `AtccCommandHandler` route opcodes not yet covered by dedicated tests |

All acceptance criteria AC1–AC16 are satisfied by the current implementation; AC9, AC10, AC15 and AC16 rely on console/remote manual verification (documented as M1–M5 in tests.md).
