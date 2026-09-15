## TESTS

### Status

US073 unit tests implemented in `aisafe.core` under `eapli.aisafe.routemanagement` (domain, application, repository interfaces).

Console and remote TCP flows are covered by manual scenarios; `AtccCommandHandler` route opcodes are wired but not yet exercised by dedicated handler tests.

Run:

```bash
cd aisafe.base
mvn -pl aisafe.core test "-Dtest=eapli.aisafe.routemanagement.**"
```

---

### Acceptance criteria traceability

| AC | Criterion (summary) | Automated coverage | Manual / UI |
|----|---------------------|--------------------|-------------|
| AC1 | Persist route linked to ATCC company | `RouteServiceTest.createCharterRoutePersistsWhenValid`, `createRegularRoutePersistsWhenValid`; `CreateRouteControllerTest` delegation | M1 |
| AC2 | Invalid route name format | `RouteNameTest.rejectsInvalidFormat` | M2 |
| AC3 | Duplicate route name | `RouteServiceTest.validateRouteNameFailsWhenDuplicate`, `createRouteFailsWhenNameExists` | M2 |
| AC4 | Origin airport not found | `RouteServiceTest.createRouteFailsWhenAirportMissing` (origin) | — |
| AC5 | Destination airport not found | — (same `requireAirport` path as AC4) | — |
| AC6 | Origin equals destination | `RouteTest.rejectsEqualOriginAndDestination`, `RouteServiceTest.createRouteFailsWhenOriginAndDestinationAreEqual` | M3 |
| AC7 | User without ATCC role | `CompanyRouteCollaboratorSessionTest.failsWhenNotAuthorized`; `CreateRouteControllerTest.currentCompanyContextReturnsCompanyIata` | — |
| AC8 | Numeric suffix + company prefix | `CreateRouteControllerTest.validateRouteNameDelegatesWhenCompanyMatches` | M1, M4 |
| AC9 | Destination list excludes origin | — | M3 |
| AC10 | Retry on invalid/duplicate name | — (`RouteAlreadyExistsException` extends `IllegalArgumentException`, caught in UI loop) | M2 |
| AC11 | Charter schedule mandatory | `RouteTest.charterRouteCreatesWithSchedule`, `RouteServiceTest.createCharterRoutePersistsWhenValid` | M1 |
| AC12 | Regular recurring days mandatory | `RouteTest.regularRouteCreatesWithRecurringSchedule`, `RouteServiceTest.createRegularRoutePersistsWhenValid`, `createRegularRouteFailsWhenNoRecurringDays` | M1 |
| AC13 | Duplicate recurring days | `RouteRecurringScheduleTest.rejectsDuplicateDays` | M3 (UI also blocks) |
| AC14 | Arrival before departure | `RouteScheduleTest.rejectsArrivalBeforeDeparture` | M1 |
| AC15 | Menu **Routes > Create Route (US073)** | — | M1 |
| AC16 | Success summary after creation | — | M1 (`RoutePrinter.printCreationSummary`) |

---

### Domain — `RouteName`

| Test | Description |
|------|-------------|
| `acceptsValidName` | Valid `TP123` format |
| `normalizesToUppercaseAndTrim` | Input normalization |
| `rejectsInvalidFormat` | AC2 — letters-only suffix, wrong length |
| `compareToOrdersLexicographically` | Ordering |
| `equalsAndHashCode` | Value object semantics |

---

### Domain — `RouteSchedule`

| Test | Description |
|------|-------------|
| `acceptsValidDates` | Happy path |
| `acceptsSameDayDepartureAndArrival` | Same-day charter allowed |
| `rejectsNullDates` | Null departure or arrival |
| `rejectsArrivalBeforeDeparture` | AC14 |
| `equalsAndHashCode` | Value object semantics |

---

### Domain — `RecurringScheduleEntry`

| Test | Description |
|------|-------------|
| `ofCreatesEntry` | Factory |
| `rejectsNullDay` | Null `DayOfWeek` |
| `equalsAndHashCode` | Value object semantics |

---

### Domain — `RouteRecurringSchedule`

| Test | Description |
|------|-------------|
| `acceptsDistinctDays` | AC12 — multiple days |
| `entriesAreUnmodifiable` | Defensive copy |
| `rejectsEmptyList` | AC12 — at least one day |
| `rejectsNullEntry` | Null entry in list |
| `rejectsDuplicateDays` | AC13 |
| `equalsAndHashCode` | Value object semantics |

---

### Domain — `Route`

| Test | Description |
|------|-------------|
| `charterRouteCreatesWithSchedule` | AC1, AC11 — charter aggregate |
| `regularRouteCreatesWithRecurringSchedule` | AC1, AC12 — regular aggregate |
| `rejectsMissingRequiredFields` | Null name, company, airports |
| `rejectsEqualOriginAndDestination` | AC6 |
| `rejectsCharterWithoutSchedule` | AC11 — charter requires `RouteSchedule` |
| `rejectsRegularWithoutRecurringSchedule` | AC12 — regular requires `RouteRecurringSchedule` |
| `sameAsComparesByIdentity` | Identity by `RouteName` |
| `deactivateSetsDateWhenActive` | US074 cross-cutting |
| `deactivateFailsWhenAlreadyDeactivated` | US074 cross-cutting |
| `deactivateRejectsNullDate` | US074 cross-cutting |

---

### Domain — `RouteOrmConstructorsTest`

| Test | Description |
|------|-------------|
| `protectedConstructorsExistForJpa` | JPA no-arg constructor present |

---

### Application — `RouteService`

| Test | Description |
|------|-------------|
| `constructorRejectsNullRepositories` | Guard clauses |
| `validateRouteNameReturnsNormalizedValue` | AC2/AC8 — normalization |
| `validateRouteNameFailsWhenDuplicate` | AC3 |
| `createCharterRoutePersistsWhenValid` | AC1, AC11 |
| `createRegularRoutePersistsWhenValid` | AC1, AC12 |
| `createRouteFailsWhenNameExists` | AC3 |
| `createRouteFailsWhenNamePrefixDoesNotMatchCompany` | Company IATA prefix rule |
| `createRouteFailsWhenOriginAndDestinationAreEqual` | AC6 |
| `createRouteFailsWhenAirportMissing` | AC4 (origin) |
| `createRegularRouteFailsWhenNoRecurringDays` | AC12 |
| `createRouteFailsWhenCompanyNull` | Null company guard |
| `listAirportsDelegatesToRepository` | Airport listing |

---

### Application — `CreateRouteController`

| Test | Description |
|------|-------------|
| `defaultConstructorIsCovered` | Production wiring |
| `constructorRejectsNullDependencies` | Guard clauses |
| `currentCompanyContextReturnsCompanyIata` | AC7, AC8 — session company |
| `validateRouteNameDelegatesWhenCompanyMatches` | AC8 — prefix + suffix |
| `validateRouteNameFailsWhenCompanyMismatch` | Invalid company context |
| `listAirportsDelegatesToService` | Airport listing |
| `createCharterRouteDelegatesToService` | AC11 delegation |
| `createRegularRouteDelegatesToService` | AC12 delegation |

---

### Application — `CompanyRouteCollaboratorSession`

| Test | Description |
|------|-------------|
| `returnsCollaboratorWhenSessionValid` | AC7 — happy path |
| `failsWhenNotAuthorized` | AC7 |
| `failsWhenSessionEmpty` | No authenticated session |
| `failsWhenCollaboratorNotRegistered` | Missing collaborator profile |
| `failsWhenIdentityThrowsClassCastException` | Invalid session identity |

---

### Application — `RouteAlreadyExistsException`

| Test | Description |
|------|-------------|
| `carriesMessage` | Exception message |

---

### Repository — `RouteRepository` (interface default methods)

| Test | Description |
|------|-------------|
| `existsByNameReturnsTrueWhenPresent` | AC3 uniqueness query |
| `findByCompanyIATACodeReturnsMatchingRoutes` | Company-scoped lookup |
| `findByCompanyDelegatesToIataLookup` | Delegation |
| `findActiveByCompanyExcludesDeactivatedRoutes` | US074 filter |
| `findByCompanyIATACodeRejectsNull` | Null guard |

Uses `DeactivateRouteTestRepositories` in-memory implementation.

---

### UI / RCOMP (manual)

| ID | Scenario |
|----|----------|
| M1 | Console: **Routes > Create Route (US073)** — charter or regular → `RoutePrinter` summary (AC15, AC16) |
| M2 | Console: invalid or duplicate numeric suffix → error message and re-prompt (AC10) |
| M3 | Console: select origin → destination list excludes origin; regular duplicate day blocked in UI (AC9, AC13) |
| M4 | RCOMP (US078): login ATCC → `CreateRouteRemoteUI` — same flow over opcodes `ROUTE_COMPANY_CONTEXT` (36), `VALIDATE_ROUTE_NAME` (37), `LIST_ROUTE_AIRPORTS` (38), `CREATE_ROUTE` (27) |
| M5 | RCOMP: `CREATE_ROUTE` with `CHARTER;departure;arrival` or `REGULAR;MONDAY,...` payload → `OK\|route` response |

---

### Gaps / follow-up (optional)

| Area | Missing test | Notes |
|------|--------------|-------|
| `RouteService` | `createRouteFailsWhenDestinationAirportDoesNotExist` | Same code path as origin; low priority |
| `RouteService` | `createCharterRouteFailsWhenArrivalBeforeDeparture` | Covered by `RouteScheduleTest` |
| `Route` | `rejectsCharterWithRecurringSchedule` / `rejectsRegularWithRouteSchedule` | Enforced in private constructor; not isolated |
| `AtccCommandHandler` | `CREATE_ROUTE`, `VALIDATE_ROUTE_NAME`, `LIST_ROUTE_AIRPORTS` | Controller mocked in handler test; no opcode integration tests |
| `CreateRouteRemoteUI` | Automated UI tests | Manual via M4/M5 |

---

### Fixtures and conventions

- Shared test data: `RouteTestFixtures` (`COMPANY_TP`, `AIRPORT_OPO`, `AIRPORT_LIS`, `ROUTE_NAME_TP123`, schedules).
- Controller default-constructor tests use `TestRouteControllersRepositoryFactory` and in-memory properties.
- Bootstrap demo routes: `RoutesBootstrapper` (company TP).

---

### Coverage

- Target JaCoCo gate for `routemanagement.application*`, `routemanagement.domain*`, `routemanagement.repositories*` per project convention.
- US074 tests extend the same package; deactivate-specific tests are documented in [US074/tests.md](../US074/tests.md).
