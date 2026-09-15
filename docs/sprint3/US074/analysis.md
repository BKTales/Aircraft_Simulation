## ANALYSIS

### Business Goal

Allow an Air Transport Company Collaborator to **stop using a route from a given date onwards**, without deleting historical data, while protecting operations that already have future flights planned on that route.

### Scope (US074)

- Deactivate route for authenticated collaborator's company.
- Set `DeactivationDate` on existing `Route` (logical deactivation).
- Validate deactivation date (not in the past).
- Reject deactivation when blocking planned flights exist (see requirements R6).
- Reject second deactivation on the same route.
- Support both `CHARTER` and `REGULAR` routes.
- Console UI and RCOMP `DEACTIVATE_ROUTE` entry points.

### Out of Scope

- Physical delete of `Route` from database.
- Reactivation (clearing `deactivationDate`).
- Changing deactivation date after first assignment.
- Automatic cancellation of existing flights (only **prevents** new flights via consumer rule R14).
- Full enforcement in every flight-creation UC in this US (documented as cross-cutting; minimal helper recommended).

---

## Assumptions

1. **Route identification for flights:** a flight belongs to a route when `Flight.routeName` equals `Route.identity().toString()` (`RouteName` value, e.g. `TP1001`).

2. **Technical debt (flight import):** current `FlightPlanDescriptorMapper` sets `routeName` from airport pair (e.g. `LPPT-EGLL`), not from `RouteName`. Blocking queries are correct for flights that already use `TPxxxx`; aligning import/scheduling UCs with `RouteName` is follow-up work, not a blocker for documenting US074.

3. **Deactivation date** uses `LocalDate` (calendar day), compared to `FlightSchedule.scheduledDeparture` converted to date.

4. **System date** for “not in the past” is `LocalDate.now()` at use-case execution time.

5. **Global route name** uniqueness from US073 still applies; deactivation does not change `RouteName`.

---

## Business Rules

1. Only `AIR_TRANSPORT_COMPANY_COLLABORATOR` can deactivate routes.

2. Route must belong to the collaborator's company (`Route.airTransportCompany`).

3. Route must exist and must be **active** (`deactivationDate == null`).

4. `deactivationDate >= today`.

5. No blocking planned flights on route (R6 in `requirements.md`).

6. On success, `Route.deactivationDate` is set and persisted.

7. After deactivation, no new flights may be scheduled on that route with departure date `>= deactivationDate` (consumer rule for other UCs).

---

## Domain Decisions

- Reuse **`Route`** aggregate and existing **`DeactivationDate`** value object (introduced in US073, optional on create).

- Add domain behaviour on `Route`, e.g. `deactivate(DeactivationDate date)`:
  - fails if already deactivated;
  - assigns `deactivationDate`.

- **No cascade** to `Flight` entities: existing flights remain; only future creation/scheduling is restricted.

- **Planned flight** definition mirrors pilot deactivation pattern (`existsActiveFlightForPilot`) but keyed by `routeName` and `LocalDate` instead of pilot user.

---

## Comparison with similar use cases

| Use case | What blocks the operation? |
|----------|----------------------------|
| Remove / deactivate pilot | Future flights for pilot with active plan statuses |
| Decommission aircraft | Future flights for aircraft registration (`schedule` only) |
| **US074 deactivate route** | Future flights on route name with schedule ≥ date and non-rejected plan (or no plan but with schedule) |

---

## Risks and Open Questions

1. **Flight–route link in production data:** until flight UCs persist `RouteName` on `Flight.routeName`, blocking may not see flights created only via DSL import. Mitigation: document R-LINK; optional small fix when implementing flight-on-route scheduling.

2. **Timezone:** comparison uses `LocalDate` from `LocalDateTime` schedule without time-zone conversion — consistent with existing `FlightRepository` queries.

3. **Consumer enforcement:** `RouteService.assertRouteAcceptsNewFlight` is implemented and tested; `Flight.assignSchedule` enforces via `route.assertUsableForFlight`. Other flight-creation UCs should call the helper where applicable.

4. **UI route selection:** show only **active** routes (`deactivationDate == null`) for the current company — implemented via `listActiveRouteOptions`.

---

## Implementation Status

| Layer | Status | Notes |
|-------|--------|-------|
| Domain | Done | `Route.deactivate`, `DeactivationDate`, `assertUsableForFlight` |
| Application | Done | `DeactivateRouteService`, `DeactivateRouteController`, `ActiveRouteOption` |
| Flight query | Done | `existsPlannedFlightOnRouteAfter` in JPA + in-memory |
| R14 consumer | Done | `RouteService.assertRouteAcceptsNewFlight` + `Flight.assignSchedule` |
| Console UI | Done | `DeactivateRouteUI`, list printer, summary |
| Remote TCP | Done | Handler + `DeactivateRouteRemoteUI` |
| Unit tests | Done | See [tests.md](tests.md) |
| FlightRepository isolated tests | Partial | Cases A–E covered via service tests; no dedicated `FlightRepositoryTest` |

All acceptance criteria AC1–AC13 are satisfied; AC12 and AC13 rely partly on manual verification (M1, M3).
