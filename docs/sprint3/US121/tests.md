# US121 — Tests

## Test Locations (Phase 3)

| Test class | Package |
|------------|---------|
| `FlightPlanJsonExporterTest` | `eapli.aisafe.dsl` |
| `FlightPlanDescriptorMapperTest` | `eapli.aisafe.flightmanagement.application` |
| `ImportFlightPlanServiceTest` | `eapli.aisafe.flightmanagement.application` |
| `ImportFlightPlanFromFileControllerTest` | `eapli.aisafe.flightmanagement.application` or console |

Fixtures: reuse `aisafe.core/src/test/resources/dsl/valid.txt`.

---

## Unit Tests — FlightPlanJsonExporter

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureJsonContainsFlightProfileWithClimbAndDescend` | Valid descriptor | `"Flight Profile"`, non-empty `Climb`/`Descend`, no `Cruise` |
| `ensureDepartureArrivalAreObjects` | Valid descriptor | `Airport` and `AreaCode` keys |
| `ensureRootHasAircraftIdAndMass` | With aircraft + mass params | Fields present |
| `ensureSegmentsHaveMode` | Multi-segment route | Mode not always `cruise` *(when implemented)* |

---

## Unit Tests — FlightPlanDescriptorMapper

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureMapsDesignatorAndType` | Descriptor | Correct `FlightDesignator`, `FlightType` |
| `ensureDerivesRouteName` | OPO→LIS legs | `routeName` = `OPO-LIS` |
| `ensureScheduleFromLegTimes` | Two legs | Min/max datetime on `FlightSchedule` |
| `ensureFlightLoadFromFirstLeg` | Load in DSL | `FlightLoad` populated |

---

## Unit Tests — ImportFlightPlanService

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureInvalidParseDoesNotCallSave` | Invalid `ParseResult` | `FlightRepository.save` never called |
| `ensureValidImportSavesFlight` | Valid descriptor + dsl text | `save` once; plan has dsl + json |
| `ensureDuplicateDesignatorThrows` | Existing flight id | Business exception / failure result |
| `ensureUnknownAircraftFails` | Bad registration | Failure before save |
| `ensurePlanStatusIsDraft` | Success | `FlightPlanStatus.DRAFT` |

Use in-memory `FlightRepository` and mocked `AircraftRepository`.

---

## Integration Tests

| Scenario | Expected |
|----------|----------|
| Import `valid.txt` end-to-end (in-memory persistence) | Flight retrievable; `jsonContent` and `dslContent` non-blank |
| Import invalid syntax file | No row in repository |
| Re-import same designator | Second import rejected |

---

## Manual Test Plan

1. Log in as Pilot.
2. Place valid `.txt` in `flightplans/`.
3. Menu → Import flight plan (US121).
4. Select file → validation success message.
5. Select aircraft → confirm import.
6. Verify flight appears in list / DB with `DRAFT` status.
7. Repeat with invalid file → errors shown, no new flight.
8. Confirm no prompt to export JSON to `flight_simulator/`.

---

## Regression

* US120 `FlightDslParserTest` must remain green (US121 does not change grammar).
* `FlightSimulationService` can load `jsonContent` from imported flight *(after exporter alignment)*.
