## DESIGN

* Follow the standard layered application architecture

**Domain classes (existing / US080):**  
`Flight`, `FlightPlan`, `FlightPlanStatus`, `FlightSchedule`, `FlightDesignator`  
`AirControlArea`, `GeographicBoundary` (US050), `Airport` (US052)  
`AreaCrossingSpan` — entry/exit fractions and coordinates along a leg

**New application / infrastructure (US100):**

| Class | Responsibility |
|-------|----------------|
| `RouteAreaCrossingDetector` | Samples DSL route segments against area polygon; returns `AreaCrossingSpan` |
| `FlightPlanAreaClipper` | Builds clipped `FlightPlanDescriptor` (segments, times, fuel proportional to span) |
| `FlightEligibilityService` | Geographic eligibility + `AreaClipMode` (FULL / CLIPPED) |
| `EligibleFlightPreview` | DTO for UI preview (designator + clip mode) |
| `AreaClipMode` | `FULL` or `CLIPPED` |

**C component (existing — Sprint 2 / SCOMP):**  
`flight_simulator` — Documento de engenharia: [../../scomp/README.md](../../scomp/README.md). Sprint 3: [../SCOMP/shm-sync-overview.md](../SCOMP/shm-sync-overview.md), [../SCOMP/simulator-global-sd.puml](../SCOMP/simulator-global-sd.puml)

### US100 orchestration flow

1. Resolve area using `AirControlAreaRepository`
2. Load flights from `FlightRepository` and filter with `FlightEligibilityService` (DSL parse + `RouteAreaCrossingDetector`)
3. Preview eligible flights with clip mode (`SimulateFlightsInAreaController.previewEligibleFlights`)
4. Reject when no eligible flights (`NoEligibleFlightsException`)
5. For each eligible flight:
   * if **CLIPPED** → `FlightPlanAreaClipper.clip` then `FlightPlanJsonExporter.toSimulatorJson`
   * if **FULL** → stored self-contained JSON or re-export from descriptor
6. Write JSON files to a temporary directory
7. Start `flight_simulator` via `ProcessBuilder` with `FS_*` environment variables
8. Read `validation_result` from CSV in `FS_REPORTS_DIR` and return pass/fail to the UI

**US085** reuses `FlightSimulationService.simulateEligibleFlights` with a **single target flight** (no clipping, no concurrent peers). US100 handles multi-flight area simulation.

**Console UI:** `SimulateFlightsInAreaUI` — lists areas, interval, eligible preview, confirm, result

**Controller:** `SimulateFlightsInAreaController`

**Service:** `FlightSimulationService`

**Repositories:** `AirControlAreaRepository`, `FlightRepository`

**Menu:** `MainMenu` → Flight Control → `SimulateFlightsInAreaAction`

**Sequence diagram:** [us100-sd.puml](us100-sd.puml) / [us100-sd.svg](us100-sd.svg)

### Clipping v1 limitations

* Direct-route segments (straight lines between waypoints)
* Single contiguous inside span per leg (first significant crossing)
* Times and fuel scaled by distance fraction along the leg
* Synthetic `ENT`/`EXT` airports at boundary points when endpoints fall outside the area
