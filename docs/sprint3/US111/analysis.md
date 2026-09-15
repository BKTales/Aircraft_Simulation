## ANALYSIS

**Client Clarification / Product Owner Session**

The actor is the **Flight Control Operator**. The use case extends **US100** (Simulate Flights in Area): after the C simulator completes, the FCO receives a **formatted summary** on the console and a **persisted `.txt` file** on the filesystem.

The PO clarified that the client expects a **seamless experience** — the FCO does not manually import a report produced by an independent C process. Simulation is triggered from the backoffice console; summary generation is the immediate post-simulation step in Java.

Regarding persistence: the summary must be stored as a **file on the filesystem** and is **not transient**. Storing the report in the relational database is **not required** (NFR08 applies to domain data, not this artefact).

US112 clarifications (indirect) guide US111 design: text format is sufficient; the team chooses the data source and violation granularity; **Strategy pattern** is the key architectural approach for all report types; an FCO operates only within their assigned air control area.

---

## BUSINESS RULES

* Summary generation runs only after a **successful** C simulation (`exit code 0` and `report.csv` present in `FS_REPORTS_DIR`).
* Global **PASS/FAIL** follows the `validation_result` metric in the US109 CSV (same rule as US100 and US085).
* Per-flight execution status comes from the CSV flights section (`flight_id`, `execution_status`).
* Safety violations are parsed from the CSV violations section when present (`flight_id_a`, `flight_id_b`, `violation_type`, `step`, `elapsed_s`, coordinates and speeds for both flights).
* **Flight ID mapping (v1):** the C simulator uses a numeric `flight_id` (stable hash of designator). The summary lists simulator IDs; optional future enhancement maps IDs back to IATA designators via the eligible-flight list exported before simulation.
* Summary path: `reports/simulations/{areaCode}/{yyyy-MM-dd_HHmmss}-summary.txt` under the repository root (or configurable via `aisafe.reports.dir`).
* Optionally archive raw `report.csv` and `report.txt` in the same directory for audit trail and US112 consumption.
* US111 is **read-only** on flight plan state — it does **not** update `FlightPlanStatus` (unlike US085, which sets `SIM_APPROVED` / `SIM_REJECTED`).
* Operation is protected by role-based authorisation (**Flight Control Operator** only).
* Report covers exactly the ACA and time interval selected for US100.

---

## US111 vs US100 vs US085 vs US109

| Aspect | US100 | US109 (SCOMP) | US111 | US085 |
|--------|-------|---------------|-------|-------|
| Actor | FCO | C process | FCO | Pilot |
| Trigger | Area + interval | End of simulation | Post-US100 | Single flight validation |
| Output to user | Pass/fail (current) | Technical CSV/TXT | Summary TXT + console | Pass/fail + reason |
| File persistence | Temp dir only (current) | Temp or default C dir | **Permanent** summary `.txt` | No summary file |
| Domain side-effect | None | None | None | Updates `FlightPlanStatus` |
| Plan export | Clipped per area | N/A | N/A | Full plan |

---

## RELATION TO US112

US112 (monthly statistics report) will introduce another `ReportGenerator` implementation. US111 establishes:

* the `ReportGenerator` interface (Strategy pattern);
* the on-disk layout under `reports/simulations/`;
* the text-report formatting conventions.

US112 may aggregate data by reading persisted US111 summary files or querying the database — the PO leaves this choice to the team.

---

## CURRENT GAPS (baseline before implementation)

| Gap | Location |
|-----|----------|
| UI shows only PASS/FAIL and temp paths | `SimulateFlightsInAreaUI` |
| Violations not parsed from CSV | `SimulationReportParser` returns empty violation list |
| Reports written to temp directories | `FlightSimulationService.runSimulation` |
| No summary generator or path resolver | Not yet implemented |
| No US111 documentation folder | Created in this sprint task |

---

## DOMAIN MODEL

The summary is built from the existing value object **`FlightSimulationReport`** (`aisafe.core` domain package), which aggregates:

* `AreaCode evaluatedIn`
* `ValidationStatus validationResult`
* `List<FlightStatus> flightStatuses`
* `List<ScheduleFlightStatus> scheduleFlightStatuses`
* `List<SafetyViolation> safetyViolations`

See [FlightSimulationReport aggregate justification](../../sprint1/Aggregate_Justifications/Flight_Simulation_Report_aggregate/fsr_justification.md). In v1 the report is **not** persisted as a JPA entity — only the `.txt` file is stored.

---

## TEST COVERAGE (planned)

Automated tests under `aisafe.core/src/test/java/`:

* `SimulationReportParserTest` — extended for violations section
* `SimulationSummaryReportGeneratorTest` — file content and path
* `SimulationReportsPathResolverTest` — directory creation and naming
* `FlightSimulationServiceTest` — summary path in `SimulationResult`
* `SimulateFlightsInAreaControllerTest` — FCO authorisation unchanged

Console: `SimulateFlightsInAreaUI` (manual acceptance; no automated UI tests).

See [tests.md](tests.md) for scenarios and commands.
