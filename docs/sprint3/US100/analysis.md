## ANALYSIS

**Client Clarification / Product Owner Session**

The actor is the **Flight Control Operator**. The use case is started from **MainMenu** → Flight Control → Simulate Flights in Area (US100). Simulations use a time range and geographic area; weather and safety thresholds use C defaults in the current build.

Flight plans included in the simulation must already exist in the database (**US080**, **US081**). The C component (`flight_simulator`) is invoked as a separate process; JSON files are only a temporary export for that process. The geographic area is an **Air Control Area** already registered in the system (US050), not ad-hoc coordinates.

**Partial crossing rule (confirmed):** a flight that lifts off outside the selected area but whose route crosses the area polygon must be simulated only for the portion inside the polygon. The C simulator has no area concept — clipping is done in Java before JSON export (US100 only; US085 keeps the full plan).

The C simulation engine (fork, shared memory, semaphores, lock-step, collision detection) is documented in [`docs/scomp/README.md`](../../scomp/README.md). Sprint 3 adds the Java backoffice integration on top of that existing component.

---

## BUSINESS RULES

* Air control area must exist (`AirControlAreaRepository`)
* Date-time interval must be valid (`end >= start`)
* At least one eligible `Flight` must exist for the selected area and interval
* Eligible flights are loaded from `FlightRepository` and filtered by `FlightEligibilityService`
* A flight is eligible when **all** of the following hold:
  * `flightPlan` has non-empty JSON ready for simulation (`hasPlanReadyForSimulation`)
  * `schedule` overlaps the requested interval
  * the route **crosses** the area `GeographicBoundary` polygon (parsed from DSL segments) — not merely when origin/destination airports belong to the area
* No `SIM_APPROVED` filter — DRAFT plans are included
* **FULL** vs **CLIPPED**: if the crossing span covers the entire leg (`entryFraction ≈ 0`, `exitFraction ≈ 1`), export the full plan; otherwise `FlightPlanAreaClipper` produces a shortened leg with proportional times, fuel, and synthetic entry/exit airport codes (`ENT`/`EXT`) where needed
* `SimulatorAirportJsonMapper` exports `AreaCode` on airport blocks (debug/reporting; not the primary eligibility criterion)
* On success, eligible plans are exported with `FlightPlanJsonExporter` into a temporary directory
* `flight_simulator` is started with `FS_FLIGHT_PLANS_DIR`, `FS_REPORTS_DIR`, `FS_NON_INTERACTIVE`, and related `FS_*` variables (see `flight_simulator/scripts/run_tests.sh`)
* Invalid plans at C load time are skipped (`init.c`, `is_valid`)
* Operation is protected by role-based authorisation in controller level (**Flight Control Operator**)
* Safety thresholds in the current C build use `CLOSE_PLANE_X`, `CLOSE_PLANE_Y`, `WARNING_THRESHOLD`, and `DT_S` from `flight_simulator.h` unless extended later

---

## US100 vs US085

| Aspect | US085 (pilot validation) | US100 (area simulation) |
|--------|--------------------------|-------------------------|
| Actor | Pilot | Flight Control Operator |
| Plan export | Full plan + concurrent flights | Clipped per selected area when partial crossing |
| Eligibility | Target flight + overlap peers | Geographic crossing + schedule overlap |
| Preview | N/A | Designator list with FULL/CLIPPED |

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/`:

* Geography: `RouteAreaCrossingDetectorTest`, `FlightPlanAreaClipperTest`
* Application: `FlightEligibilityServiceTest`, `FlightSimulationServiceTest`, `SimulateFlightsInAreaControllerTest`, `ValidateFlightPlanServiceTest`
* DSL export: `SimulatorAirportJsonMapperTest`
* Existing C regression: `flight_simulator/scripts/run_tests.sh`

Console: `SimulateFlightsInAreaUI`, `SimulateFlightsInAreaAction` (manual; no automated UI tests).

---

## UNIT TESTS (service / controller scenarios)

* `EnsureSimulateFlightsRequiresFlightControlOperatorAuthorization`
* `EnsureAirControlAreaMustExist`
* `EnsureDateIntervalValidationIsApplied`
* `EnsureAtLeastOneEligibleFlightExists`
* `EnsureEligibleFlightsAreExportedBeforeSimulatorExecution`
* `EnsureAreaCrossingDetectorFindsPartialAndFullSpans`
* `EnsureFlightPlanAreaClipperReducesFuelAndSegmentsForPartialCrossing`
* `EnsureUs100ExportsClippedJsonWhileUs085ExportsFullPlan`
* `EnsurePreviewShowsFullAndClippedModes`
* `EnsurePassFailOutcomeIsReturnedFromReport`
