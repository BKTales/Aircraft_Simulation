## TESTS

### Status

US074 unit tests implemented in `aisafe.core` (`eapli.aisafe.routemanagement`) and handler delegation tests in `aisafe.rcomp.server` (`AtccCommandHandlerTest`).

Blocking-flight logic is exercised via `DeactivateRouteServiceTest` using `DeactivateRouteTestRepositories` (in-memory `RouteRepository` + `FlightRepository` with `existsPlannedFlightOnRouteAfter`).

Run:

```bash
cd aisafe.base
mvn -pl aisafe.core,aisafe.rcomp.server test "-Dtest=DeactivateRouteServiceTest,DeactivateRouteControllerTest,RouteServiceDeactivateConsumerTest,AtccCommandHandlerTest"
```

---

### Acceptance criteria traceability

| AC | Criterion (summary) | Automated coverage | Manual / UI |
|----|---------------------|--------------------|-------------|
| AC1 | Happy path — persist `deactivationDate` | `DeactivateRouteServiceTest.deactivateSucceedsWhenNoBlockingFlights` | M1 |
| AC2 | Route not found | `deactivateFailsWhenRouteNotFound` | M4 |
| AC3 | Wrong company | `deactivateFailsWhenWrongCompany` | — |
| AC4 | Date in the past | `deactivateFailsWhenDateInPast` | M1 |
| AC5 | Already deactivated | `deactivateFailsWhenAlreadyDeactivated`; `RouteTest.deactivateFailsWhenAlreadyDeactivated` | — |
| AC6 | Blocking planned flight (R6) | `deactivateFailsWhenDraftFutureFlightExists` | M2 |
| AC7 | `SIM_REJECTED` does not block | `deactivateSucceedsWhenOnlySimRejectedFutureFlight` | — |
| AC8 | Schedule without plan blocks | `deactivateFailsWhenScheduledFlightWithoutPlanExists` | M2 |
| AC9 | Only past flights — succeeds | `deactivateSucceedsWhenOnlyPastFlightOnRoute` | — |
| AC10 | User without ATCC role | `CompanyRouteCollaboratorSessionTest.failsWhenNotAuthorized`; auth verified in `listActiveRouteOptionsDelegatesToService` | — |
| AC11 | Charter and regular | Domain/service use same `deactivateRoute`; charter covered in `RouteRepositoryTest` fixtures | M1 |
| AC12 | Console success summary | — | M1 (`RoutePrinter.printDeactivationSummary`) |
| AC13 | RCOMP success response | `AtccCommandHandlerTest.ensureDeactivateRouteDelegates` | M3 |

---

### Domain — `Route` / `DeactivationDate`

| Test class | Test | Description |
|------------|------|-------------|
| `RouteTest` | `deactivateSetsDateWhenActive` | Active route receives `DeactivationDate` |
| `RouteTest` | `deactivateFailsWhenAlreadyDeactivated` | AC5 — second call throws |
| `RouteTest` | `deactivateRejectsNullDate` | Null date guard |
| `DeactivationDateTest` | `valueOfCreatesDate`, `rejectsNull`, `equalsAndHashCode` | Value object |

---

### Application — `DeactivateRouteService`

| Test | Description |
|------|-------------|
| `deactivateSucceedsWhenNoBlockingFlights` | AC1 — happy path |
| `deactivateFailsWhenRouteNotFound` | AC2 |
| `deactivateFailsWhenWrongCompany` | AC3 |
| `deactivateFailsWhenAlreadyDeactivated` | AC5 — `RouteAlreadyDeactivatedException` |
| `deactivateFailsWhenDateInPast` | AC4 |
| `deactivateFailsWhenDraftFutureFlightExists` | AC6 — case A (`DRAFT`) |
| `deactivateSucceedsWhenOnlySimRejectedFutureFlight` | AC7 — case C |
| `deactivateFailsWhenScheduledFlightWithoutPlanExists` | AC8 — case D |
| `deactivateSucceedsWhenOnlyPastFlightOnRoute` | AC9 — case E |
| `listActiveRouteOptionsIncludesLastPlannedFlightDate` | UI helper — last planned departure |
| `listActiveRouteOptionsIgnoresPastPlannedFlights` | Past flights excluded from hint |
| `listActiveRouteOptionsShowsEmptyWhenNoPlannedFlights` | No blocking flights |
| `listActiveRoutesExcludesDeactivated` | Only active routes listed |

Uses `RouteTestFixtures` and `DeactivateRouteTestRepositories`.

---

### Application — `DeactivateRouteController`

| Test | Description |
|------|-------------|
| `defaultConstructorIsCovered` | Production wiring |
| `constructorRejectsNullDependencies` | Guard clauses |
| `listActiveRouteOptionsDelegatesToService` | AC10 — auth + delegation |
| `deactivateRouteDelegatesToService` | AC1 — company from session |

---

### Application — R14 consumer (`RouteService`)

| Test class | Test | Description |
|------------|------|-------------|
| `RouteServiceDeactivateConsumerTest` | `assertRouteAcceptsNewFlightWhenRouteActive` | Active route allows flight |
| `RouteServiceDeactivateConsumerTest` | `assertRouteAcceptsNewFlightFailsWhenDeactivatedOnSameDay` | R14 — inclusive date |
| `RouteServiceDeactivateConsumerTest` | `assertRouteAcceptsNewFlightSucceedsDayBeforeDeactivation` | Day before deactivation OK |

Enforcement also wired in `Flight.assignSchedule` via `route.assertUsableForFlight`.

---

### Repository — `RouteRepository`

| Test | Description |
|------|-------------|
| `findActiveByCompanyExcludesDeactivatedRoutes` | Active-route listing filter |
| `existsByNameReturnsTrueWhenPresent` | US073 + US074 shared |
| `findByCompanyIATACodeReturnsMatchingRoutes` | Company scope |

---

### Repository — `FlightRepository.existsPlannedFlightOnRouteAfter`

Implemented in `InMemoryFlightRepository` and `JpaFlightRepository`. Logic mirrored in `DeactivateRouteTestRepositories` for service tests.

Reference table (deactivation date `2026-06-15`, route `TP1001`) from `requirements.md`:

| Case | Departure | Plan | Blocks? | Service test |
|------|-----------|------|---------|--------------|
| A | ≥ date | `DRAFT` | Yes | `deactivateFailsWhenDraftFutureFlightExists` |
| B | ≥ date | `SIM_APPROVED` | Yes | — (same query path as A) |
| C | ≥ date | `SIM_REJECTED` | No | `deactivateSucceedsWhenOnlySimRejectedFutureFlight` |
| D | ≥ date | none, has schedule | Yes | `deactivateFailsWhenScheduledFlightWithoutPlanExists` |
| E | &lt; date | `DRAFT` | No | `deactivateSucceedsWhenOnlyPastFlightOnRoute` |

Dedicated `FlightRepositoryTest` cases A–E (isolated per status) are **not** implemented; coverage is via service + in-memory repository duplication.

---

### RCOMP — `AtccCommandHandlerTest`

| Test | Description |
|------|-------------|
| `ensureListActiveRoutesReturnsRoutes` | Opcode `LIST_ACTIVE_ROUTES` (39) |
| `ensureDeactivateRouteDelegates` | AC13 — opcode `DEACTIVATE_ROUTE` (28), payload `routeName;date` |
| `ensureDeactivateRouteRejectsMissingDate` | Bad payload → `BAD_REQUEST` |

---

### UI / RCOMP (manual)

| ID | Scenario |
|----|----------|
| M1 | Console: **Routes > Deactivate Route (US074)** — select active route, valid date → summary (AC12) |
| M2 | Console: route with blocking future flight → `RouteHasPlannedFlightsException` message (AC6, AC8) |
| M3 | RCOMP: `DEACTIVATE_ROUTE` with `TP7401;2026-12-01` → `OK\|route` (AC13) |
| M4 | RCOMP: unknown route → error response (AC2) |
| M5 | Bootstrap demo routes `TP7401`–`TP7404` (company TP) for manual testing |

---

### Gaps / follow-up (optional)

| Area | Missing | Notes |
|------|---------|-------|
| `DeactivateRouteService` | Explicit charter deactivation test | Same code path as regular; domain invariant is type-agnostic |
| `FlightRepository` | Isolated tests for cases B, `SUBMITTED_FOR_SIMULATION` | Query logic shared; only DRAFT exercised in service test |
| `RouteRepository` | `savePersistsDeactivationDate` reload test | Covered indirectly via `deactivateSucceedsWhenNoBlockingFlights` |
| `DeactivateRouteRemoteUI` | Automated client tests | Manual via M3 |

---

### Fixtures and conventions

- `DeactivateRouteTestRepositories` — in-memory route + flight repos for integration-style service tests.
- `RoutesBootstrapper` — demo routes `TP7401`–`TP7404` for US074 manual tests.
- Exception types: `RouteAlreadyDeactivatedException`, `RouteHasPlannedFlightsException`.

---

### Coverage

- Target JaCoCo gate for `routemanagement.application*`, `routemanagement.domain*`, `routemanagement.repositories*` (shared with US073).
- `FlightRepository` new method tested via service tests and persistence implementations, not a dedicated test class.
