# US080 — Tests

Requirements: [requirements.md](requirements.md). Design: [design.md](design.md).

**TCP / remote tests** are documented in [US086/tests.md](../US086/tests.md).

---

## Status

Automated tests live in `aisafe.core` (flight management + DSL export). Console wizard and confirmation prompts are covered by manual scenarios.

Run focused suite:

```bash
cd aisafe.base
mvn -q -pl aisafe.core test -Dtest=CreateFlightPlanServiceTest,CreateFlightPlanControllerTest,FlightPlanDslExporterTest,FlightPlanJsonExporterTest,FuelQuantityTest,OperationalSuffixTest
```

Full module:

```bash
cd aisafe.base
mvn -q -pl aisafe.core -am test
```

---

## Acceptance criteria traceability

| AC | Criterion (summary) | Automated | Manual |
|----|---------------------|-----------|--------|
| AC1 | Persist DRAFT with DSL + JSON | `CreateFlightPlanServiceTest.createsDraftFlightForCharterRoute` | M1 |
| AC2 | Inactive route on date | `CreateFlightPlanServiceTest.failsWhenRouteDeactivated` | M2 |
| AC3 | Schedule mismatch | Route/domain tests via service failures | M2 |
| AC4 | Pilot not certified | Domain rules in `Flight.createDraftForRoute` | M3 |
| AC5 | Operational suffix → new designator | `OperationalSuffixTest` | M1 |
| AC6 | Silent replace DRAFT / SIM_REJECTED | `replacesPlanSilentlyWhenExistingPlanIsDraft`, `…SimRejected` | M4 |
| AC7 | Confirm required on SIM_APPROVED / SUBMITTED | `needsConfirmationWhenExistingPlanIsSimApproved`, `needsConfirmationWhenExistingPlanIsSubmittedForSimulation` | M4 |
| AC8 | Confirmed replace | `replacesPlanWhenSimApprovedAndUserConfirms` | M4 |
| AC9 | Success summary | — | M1 (`FlightPlanCreationSummary`) |
| AC10 | Non-Pilot blocked | `CreateFlightPlanControllerTest.createFlightPlan_throwsWhenUnauthorized` | — |
| AC11 | `flightLoad` null | `CreateFlightPlanServiceTest` assertions | M1 |

---

## Automated tests

### Application — `CreateFlightPlanServiceTest`

Service-level coverage (listings are thin repository queries; create/replace is fully tested here):

| Test | Coverage |
|------|----------|
| `createsDraftFlightForCharterRoute` | Happy path; DSL + JSON stored; `flightLoad` null |
| `replacesPlanSilentlyWhenExistingPlanIsDraft` | Auto replace; `replacedFromStatus` DRAFT |
| `replacesPlanSilentlyWhenExistingPlanIsSimRejected` | Auto replace from SIM_REJECTED |
| `needsConfirmationWhenExistingPlanIsSimApproved` | No save until confirm |
| `needsConfirmationWhenExistingPlanIsSubmittedForSimulation` | Same rule for SUBMITTED_FOR_SIMULATION |
| `replacesPlanWhenSimApprovedAndUserConfirms` | Replace with `confirmReplace=true` |
| `listSelectableRoutes_returnsActiveRoutesForSessionPilotCompany` | Wizard route list by company/date |
| `listSelectableRoutes_excludesDeactivatedRouteOnDate` | Deactivated routes omitted |
| `listCompanyActiveAircraftRegistrations_returnsSortedRegistrations` | Fleet list for wizard |
| `listCompanyPilots_returnsActivePilotsForCompany` | Roster list for wizard |
| `listMethods_throwWhenSessionPilotNotFound` | Unknown session pilot |
| `failsWhenArrivalNotAfterDeparture` | Schedule validation |
| `failsWhenRouteDeactivated` | Route inactive on date |

### Application — `CreateFlightPlanControllerTest`

Thin controller coverage (authz + delegation only; business rules stay in service tests):

| Test | Coverage |
|------|----------|
| `constructorThrowsWhenAuthzIsNull` / `…ServiceIsNull` | Dependency validation |
| `createFlightPlan_delegatesToServiceWhenAuthorized` | PILOT role check + service call |
| `createFlightPlan_throwsWhenUnauthorized` | No service interaction on auth failure |
| `listSelectableRoutes_passesSessionUsernameAndDate` | Session username forwarded to service |
| `listCompanyActiveAircraftRegistrations_delegatesWithSessionUsername` | Same delegation pattern |
| `listCompanyPilots_delegatesWithSessionUsername` | Same delegation pattern |
| `throwsWhenNoAuthenticatedSession` | List methods fail without session |

### DSL export — `FlightPlanDslExporterTest`

Round-trip exported direct-route DSL through `FlightDslParser`; invalid arrival fails semantic validation after export.

### Simulator JSON — `FlightPlanJsonExporterTest`

| Test | Coverage |
|------|----------|
| `ensureClimbSegmentEndAltitudeIsCruiseLevel` | Segment altitude in JSON |
| `ensureJsonMatchesSimulatorShape` | Root `Load`, `FlightProfile`, embedded airports/aircraft |
| `ensureGeneratedJsonHasNoStrayCommasAfterLoadRelocation` | Valid leg JSON after Load-at-root refactor (#192) |

### Supporting tests

| Class | Coverage |
|-------|----------|
| `FuelQuantityTest` | kg / litres conversion |
| `OperationalSuffixTest` | Designator suffix rules |
| `RouteAvailabilityTest` | Route schedule / deactivation |
| `RouteRepositoryTest.findActiveByCompanyExcludesDeactivatedRoutes` | Active route query |
| `FlightTest` | Aggregate behaviour (shared with US121) |

---

## Key assertions (`CreateFlightPlanServiceTest`)

- `saved.flightPlan().hasDslContent()` and `hasJsonContent()` after success.
- `saved.flightPlan().dslContent()` contains flight designator (e.g. `flight TP123`).
- `saved.flightLoad()` is **null**.
- Replacement: `result.replaced()` and `replacedFromStatus()` for silent and confirmed paths.

---

## Manual scenarios (console)

### M1 — Happy path create

1. Login as `pilot1` / `./run-aisafe.sh`.
2. **Flights > Create flight plan (US080)**.
3. Example: route `TP1001`, CS-TP02, 2026-06-22 10:00–13:00, fuel 12000 kg, modest load, suffix **A**.
4. Expect summary with designator `TP1001A`, DRAFT status, route/aircraft/pilot/fuel/load.
5. **Flights > Validate (US085)** on same designator should reach simulation (DSL OK).

### M2 — Route / schedule rejection

- Deactivated route or wrong weekday for REGULAR route → error, no flight saved.

### M3 — Certification

- Aircraft model not certified for selected pilot → error.

### M4 — Plan replacement

| Precondition | Action | Expected |
|--------------|--------|----------|
| `TP1001` DRAFT exists | Create again same designator (no suffix) | Silent replace; summary note mentions previous DRAFT |
| `TP1001` SIM_REJECTED | Same | Silent replace |
| `TP1001` SIM_APPROVED | Create same designator | Confirmation prompt; **n** cancels; **y** replaces to DRAFT |

### M5 — Operational suffix

- Create `TP1001` then create `TP1001` with suffix **B** → two distinct flights.

---

## Remote (US086)

Same business rules over TCP; see [US086/tests.md](../US086/tests.md) and `PilotCommandHandlerTest` / `CreateFlightPlanRemoteUI` manual checks.
