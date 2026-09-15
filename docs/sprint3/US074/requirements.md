# US074 — Deactivate a Flight Route

## User Story

As an Air Transport Company Collaborator, I want to **deactivate** a flight route from a given date onwards.

- No more flights can be created on a deactivated route from that date (inclusive).
- A route **cannot** be deactivated if there are **planned flights** on that route with departure on or after the deactivation date.

> **Terminology:** “Delete route” in the product wording means **logical deactivation** (`DeactivationDate` on `Route`), not physical removal from the database.

---

## Requirements

### Functional

1. **R1 — Deactivate route**
   An authenticated and authorized Air Transport Company Collaborator (ATCC) can deactivate an existing route by setting a deactivation date.

2. **R2 — Route ownership**
   Only routes belonging to the ATCC's company (from session) can be deactivated.

3. **R3 — Route must exist**
   The route identified by `RouteName` must exist in the system.

4. **R4 — Logical deactivation**
   Deactivation persists `DeactivationDate` on the `Route` aggregate. The route record remains in the repository.

5. **R5 — Inclusive deactivation date**
   The deactivation date is the **first calendar day** on which new flights must not be created on that route.
   - A flight with `scheduledDeparture.toLocalDate() < deactivationDate` does not block deactivation.
   - A flight with `scheduledDeparture.toLocalDate() >= deactivationDate` may block deactivation (see R6).

6. **R6 — Block when planned flights exist**
   Deactivation fails if there is at least one **planned flight** on the same route that would be affected (departure date on or after deactivation date).

   A flight is on the route when:
   ```text
   Flight.routeName equals Route.identity().toString()   (e.g. "TP1001")
   ```

   A flight counts as **planned** (blocking) when **all** of the following hold:
   - `schedule != null`
   - `schedule.scheduledDeparture.toLocalDate() >= deactivationDate`
   - `flightPlan == null` **OR** `flightPlan.status` is one of:
     - `DRAFT`
     - `SUBMITTED_FOR_SIMULATION`
     - `SIM_APPROVED`

   A flight with `flightPlan.status == SIM_REJECTED` does **not** block deactivation.

   A flight with **schedule but no flight plan** (`flightPlan == null`) **does** block deactivation (allowed by domain model; see `DecommissionAircraftServiceTest` precedent).

7. **R7 — Deactivation date validation**
   `deactivationDate` must not be in the past: `deactivationDate >= LocalDate.now()` (system date at execution time).

8. **R8 — Already deactivated**
   If `Route.deactivationDate` is already set, deactivation fails (no second deactivation, no date change).

9. **R9 — Route types**
   Both `CHARTER` and `REGULAR` routes can be deactivated using the same use case.

10. **R10 — Authorization**
    Only users with role `AIR_TRANSPORT_COMPANY_COLLABORATOR` can execute this use case.

11. **R11 — Console entry point**
    Backoffice menu: **Routes > Deactivate Route (US074)** (`DeactivateRouteAction` / `DeactivateRouteUI`).

12. **R12 — Success feedback**
    After successful deactivation, the console shows route identity, company IATA, and the persisted deactivation date.

13. **R13 — Remote entry point (RCOMP)**
    ATCC opcode `DEACTIVATE_ROUTE` (28) with payload encoding route name and deactivation date.

14. **R14 — Consumer rule (cross-cutting)**
    Any use case that **creates or schedules** a flight on a route must reject the operation when:
    - `route.deactivationDate != null`, and
    - the flight's departure date `>= route.deactivationDate.value()`.

    US074 implements deactivation; enforcement in flight-creation UCs may be added in the same sprint or documented as follow-up (see `analysis.md`).

### Non-functional

15. **R15 — Persistence**
    Updated route must be persisted through `RouteRepository` (JPA or in-memory, per configuration).

16. **R16 — Architectural consistency**
    Follow the same layered architecture and DDD style as US073 (`routemanagement` bounded context).

17. **R17 — Reuse session helper**
    Resolve company context via `CompanyRouteCollaboratorSession` (same as US073).

### Implementation mapping (code)

| Concern | Class / artifact |
|---------|------------------|
| Aggregate | `Route.deactivate(DeactivationDate)`, `Route.assertUsableForFlight`, `Route.isActiveOn` |
| Value object | `DeactivationDate` |
| Application | `DeactivateRouteController`, `DeactivateRouteService`, `ActiveRouteOption` |
| Exceptions | `RouteAlreadyDeactivatedException`, `RouteHasPlannedFlightsException` |
| R14 consumer | `RouteService.assertRouteAcceptsNewFlight`; `Flight.assignSchedule` calls `route.assertUsableForFlight` |
| Flight check | `FlightRepository.existsPlannedFlightOnRouteAfter`, `findLastPlannedFlightDepartureDateOnRoute` |
| Session helper | `CompanyRouteCollaboratorSession` |
| Console UI | `DeactivateRouteAction`, `DeactivateRouteUI`, `DeactivateRouteListPrinter`, `RoutePrinter.printDeactivationSummary` |
| Console menu | `MainMenu` → **Routes > Deactivate Route (US074)**; `CollaboratorMenus` (remote ATCC profile) |
| Remote server | `AtccCommandHandler` — `DEACTIVATE_ROUTE` (28), `LIST_ACTIVE_ROUTES` (39) |
| Remote client | `DeactivateRouteRemoteUI`, `DeactivateRouteRemoteAction` |
| Bootstrap | `RoutesBootstrapper` — demo routes `TP7401`–`TP7404` |

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given a valid ATCC session and an active route of the collaborator's company, when deactivation is requested with a valid future date and no blocking flights, then `deactivationDate` is persisted. |
| AC2 | Given a route that does not exist, when deactivation is attempted, then it fails and the route is unchanged. |
| AC3 | Given a route owned by another company, when deactivation is attempted, then it fails and the route is unchanged. |
| AC4 | Given a deactivation date in the past, when deactivation is attempted, then validation fails. |
| AC5 | Given a route already deactivated, when deactivation is attempted again, then it fails with a clear error. |
| AC6 | Given a planned flight on the route with departure on or after the deactivation date (per R6), when deactivation is attempted, then it fails. |
| AC7 | Given a flight on the route with `SIM_REJECTED` plan and departure on or after the deactivation date, when deactivation is attempted, then it succeeds (if no other blocking flights). |
| AC8 | Given a flight on the route with schedule but no flight plan and departure on or after the deactivation date, when deactivation is attempted, then it fails. |
| AC9 | Given only flights on the route with departure before the deactivation date, when deactivation is attempted, then it succeeds. |
| AC10 | Given a user without ATCC role, when deactivation is attempted, then authorization fails. |
| AC11 | Given `CHARTER` or `REGULAR` route type, when other preconditions hold, then deactivation succeeds for both. |
| AC12 | Given successful deactivation via console, when the flow completes, then a summary shows route name, company IATA, and deactivation date. |
| AC13 | Given successful deactivation via RCOMP `DEACTIVATE_ROUTE`, when the command completes, then the client receives a success response with route details. |

---

## Planned flight blocking — reference table

Assumed deactivation date: **2026-06-15**.

| Case | Departure | Flight plan | Blocks? | Reason |
|------|-----------|-------------|---------|--------|
| A | 2026-06-20 | `DRAFT` | Yes | Future planned flight |
| B | 2026-06-20 | `SIM_APPROVED` | Yes | Future approved flight |
| C | 2026-06-20 | `SIM_REJECTED` | No | Rejected plan — not operational |
| D | 2026-06-20 | none, has `schedule` | Yes | Scheduled slot without plan (domain allows) |
| E | 2026-06-10 | `DRAFT` | No | Departure before deactivation date |
