# US073 — Create a Route

## User Story
As an Air Transport Company Collaborator, I want to add a route for my company.
A route is between two airports (start and end) and has a name with 2 letters (my company's initials)
and up to 4 numbers (e.g. `TP123`). The route name must be unique.

---

## Requirements

### Functional

1. **R1 — Create route**
   An authenticated and authorized Air Transport Company Collaborator (ATCC) can create a flight route.

2. **R2 — Route ownership**
   The created route is automatically associated with the ATCC's company (derived from session).

3. **R3 — Route endpoints**
   A route has exactly one origin airport and one destination airport.

4. **R4 — Distinct endpoints**
   Origin and destination airports must be different.

5. **R5 — Route name format**
   Route name must match: `2 uppercase letters + 1 to 4 digits` (e.g. `TP1`, `TP1234`).

6. **R6 — Route name prefix from company**
   The two-letter prefix is derived from the ATCC company initials. The user only inputs the numeric suffix (`1..4` digits).

7. **R7 — Route name uniqueness**
   Route name must be unique in the system.

8. **R8 — Route type selection**
   When creating a route, the user must select whether it is Regular or Charter.

9. **R9 — Charter schedule**
   If route type is `CHARTER`, the user must provide `scheduledDeparture` and `scheduledArrival` (`LocalDate`).

10. **R10 — Regular recurring schedule**
    If route type is `REGULAR`, the user must provide at least one recurring day (`DayOfWeek`) through `RouteRecurringSchedule` and `RecurringScheduleEntry`.

11. **R11 — Mutually exclusive schedule models**
    A route cannot have both `RouteSchedule` and `RouteRecurringSchedule` at the same time.

12. **R12 — Airport existence**
   Both origin and destination airports must exist in the system.

13. **R13 — Airport selection UX**
   The system presents a selectable list of possible origin airports; after origin selection, destination list must exclude the selected origin.

14. **R14 — Route name retry UX**
    If route name validation fails (invalid format or duplicate), the system shows the error and asks the user to re-enter the numeric suffix.

15. **R15 — Authorization**
   Only users with role `AIR_TRANSPORT_COMPANY_COLLABORATOR` can execute this use case.

16. **R16 — Console entry point**
   Backoffice menu: **Routes > Create Route (US073)** (`CreateRouteAction` / `CreateRouteUI`).

17. **R17 — Creation success feedback**
   After successful creation, the console shows all persisted route parameters (name, company IATA, endpoints, type, and schedule or recurring days).

18. **R18 — Charter date input (UI)**
   For `CHARTER`, the console accepts flexible `LocalDate` text (`YYYY-MM-DD` or `YYYY-M-D`, e.g. `2026-7-15`).

### Non-functional

19. **R19 — Persistence**
   Created route data must be persisted through `RouteRepository` (JPA or in-memory, per configuration).

20. **R20 — Architectural consistency**
    The implementation must follow the project's layered architecture and DDD style used in existing use cases.

### Implementation mapping (code)

| Concern | Class / artifact |
|---------|------------------|
| Aggregate | `Route`, `RouteName`, `RouteSchedule`, `RouteRecurringSchedule`, `RecurringScheduleEntry` |
| Application | `CreateRouteController`, `RouteService`, `RouteAlreadyExistsException` |
| Session helper | `CompanyRouteCollaboratorSession` |
| Persistence | `RouteRepository`, `JpaRouteRepository`, `InMemoryRouteRepository` |
| Console UI | `CreateRouteAction`, `CreateRouteUI`, `RoutePrinter` |
| Console menu | `MainMenu` → **Routes > Create Route (US073)**; `CollaboratorMenus` (remote ATCC profile) |
| Remote server | `AtccCommandHandler` — opcodes `CREATE_ROUTE` (27), `ROUTE_COMPANY_CONTEXT` (36), `VALIDATE_ROUTE_NAME` (37), `LIST_ROUTE_AIRPORTS` (38) |
| Remote client | `CreateRouteRemoteUI` (US078 ATCC TCP client) |
| Bootstrap | `RoutesBootstrapper` — demo routes for company TP |

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given a valid ATCC session and valid route input, when the route is created, then it is persisted and linked to the ATCC company. |
| AC2 | Given a route name that does not match the format, when creation is attempted, then validation fails and no route is persisted. |
| AC3 | Given an already existing route name, when creation is attempted, then validation fails and no route is persisted. |
| AC4 | Given an origin airport that does not exist, when creation is attempted, then validation fails and no route is persisted. |
| AC5 | Given a destination airport that does not exist, when creation is attempted, then validation fails and no route is persisted. |
| AC6 | Given equal origin and destination airports, when creation is attempted, then validation fails and no route is persisted. |
| AC7 | Given a user without ATCC role, when creation is attempted, then authorization fails. |
| AC8 | Given a logged ATCC, when creating a route, then only the numeric suffix is requested and the system composes the full route name with company initials. |
| AC9 | Given a selected origin airport, when selecting destination, then the destination list does not include the selected origin airport. |
| AC10 | Given an invalid or duplicated route name, when creation is attempted, then an error is shown and the system asks again for route numeric suffix. |
| AC11 | Given flight type `CHARTER`, when creating a route, then `scheduledDeparture` and `scheduledArrival` are mandatory and persisted as `RouteSchedule`. |
| AC12 | Given flight type `REGULAR`, when creating a route, then at least one `DayOfWeek` is mandatory and persisted as `RouteRecurringSchedule`. |
| AC13 | Given `REGULAR`, when duplicate days are provided, then validation fails. |
| AC14 | Given `CHARTER`, when `scheduledArrival` is before `scheduledDeparture`, then validation fails and no route is persisted. |
| AC15 | Given a logged ATCC, when opening backoffice, then route creation is available under **Routes > Create Route (US073)**. |
| AC16 | Given a successfully created route, when the flow completes, then the console displays a summary with route name, company IATA, origin, destination, flight type, and schedule details. |
