## DESIGN

US111 extends the US100 simulation flow with post-simulation **summary parsing, formatting, and filesystem persistence**. It follows the standard layered application architecture and introduces a **Strategy pattern** foundation for US112.

---

### Responsibilities

| Layer | Component | Responsibility |
|-------|-----------|----------------|
| **UI** | `SimulateFlightsInAreaUI` (extend) | Display formatted summary and saved file path after simulation |
| **Controller** | `SimulateFlightsInAreaController` | Authorisation (FCO); delegate to service — no business logic |
| **Service** | `FlightSimulationService` (extend) | Orchestrate US100 simulation + US111 summary generation |
| **Application** | `SimulationReportParser` (extend) | Parse US109 CSV: metrics, flights, violations |
| **Application** | `SimulationSummaryReportGenerator` | Format and write persisted `.txt` summary |
| **Application** | `ReportGenerator<RQ, RS>` (interface) | Strategy contract for all report types |
| **Application** | `SimulationSummaryRequest` / `SimulationSummaryResult` | Input/output DTOs for summary generation |
| **Infrastructure** | `SimulationReportsPathResolver` | Resolve permanent directory; create `{area}/{timestamp}-summary.txt` |
| **Domain** | `FlightSimulationReport`, `SafetyViolation` | Summary domain model (value objects) |
| **C process** | `flight_simulator` / `flight_report.c` | US109 — produces `report.csv` and `report.txt` |

---

### Flow

1. FCO confirms area simulation in `SimulateFlightsInAreaUI` (US100 preview step unchanged).
2. `SimulateFlightsInAreaController.simulateFlightsInArea(...)` delegates to `FlightSimulationService`.
3. Service exports JSON plans, launches `flight_simulator` with `FS_*` environment variables.
4. C simulator completes and writes `report.csv` (+ `report.txt`) to `FS_REPORTS_DIR`.
5. `SimulationReportParser.parse(csvPath, areaCode)` builds a complete `FlightSimulationReport` (including violations).
6. `SimulationSummaryReportGenerator.generate(SimulationSummaryRequest)` writes the summary `.txt` to a permanent path via `SimulationReportsPathResolver`.
7. Optionally copies raw `report.csv` / `report.txt` to the same permanent directory.
8. `SimulationResult` is extended with `summaryReportPath()` and returned to the UI.
9. UI prints the formatted summary and the file path.

**US085 does not invoke the summary generator** — pilot validation only updates `FlightPlanStatus` and returns pass/fail in the response.

---

### Strategy Pattern (US112 foundation)

```java
public interface ReportGenerator<RQ, RS> {
    RS generate(RQ request);
}
```

| Implementation | US | Input | Output |
|----------------|-----|-------|--------|
| `SimulationSummaryReportGenerator` | US111 | `SimulationSummaryRequest` | `Path` (summary `.txt`) |
| `MonthlyStatisticsReportGenerator` | US112 | `MonthlyReportRequest` | `Path` (monthly `.txt`) |

US112 may read persisted US111 files or query the database — independent of US111's chosen data source.

---

### US109 CSV mapping

Source: `flight_simulator/src/main/utils/flight_report.c` — `write_csv()`.

**Metrics section:**

```csv
metric,value
flights_reported,3
flights_success,2
validation_result,PASS
...
```

**Flights section:**

```csv
flight_id,departure,arrival,execution_status,step,...
789013,LPPT,EGLL,SUCCESS,15,...
```

**Violations section** (when `violation_count > 0`):

```csv
flight_id_a,flight_id_b,violation_type,step,elapsed_s,separation_m,alt_sep_m,
lat_a_deg,lon_a_deg,alt_a_m,speed_kt_a,speed_ms_a,
lat_b_deg,lon_b_deg,alt_b_m,speed_kt_b,speed_ms_b
```

`SimulationReportParser` maps violations to `SafetyViolation` (or an enriched `SafetyViolationEvent`) with:
* timestamp → `"step {step} ({elapsed_s}s)"`
* position → `"lat {lat}, lon {lon}, alt {alt} m"` per involved flight

---

### Summary file format (example)

Path: `reports/simulations/AREA-0/2026-06-08_143000-summary.txt`

```text
================================================================================
 AISafe SIMULATION SUMMARY REPORT
================================================================================
 Area              : AREA-0
 Interval          : 2026-05-26 00:00 → 2026-08-26 23:59
 Generated at      : 2026-06-08 14:30:00

 FINAL RESULT      : FAIL
 Total flights     : 3
 Completed (SUCCESS): 2
 Failed execution  : 1

 FLIGHT EXECUTION STATUS
   789013  SUCCESS
   456789  COLLISION
   123456  OUT_OF_FUEL

 SAFETY VIOLATIONS (1)
   [1] COLLISION — step 42 (420s)
       Flight A (ID 789013): lat +41.12345, lon -8.67890, alt 9500.00 m
       Flight B (ID 456789): lat +41.12350, lon -8.67885, alt 9450.00 m
================================================================================
```

When there are zero violations, the section reads `SAFETY VIOLATIONS (0)` or `No safety violations recorded.`

---

### Filesystem layout

```
reports/
└── simulations/
    └── {areaCode}/
        ├── 2026-06-08_143000-summary.txt    ← US111 output
        ├── 2026-06-08_143000-report.csv     ← optional archive (US109)
        └── 2026-06-08_143000-report.txt     ← optional archive (US109)
```

Base directory configurable via system property or environment variable `AISAFE_REPORTS_DIR` (default: repository root `reports/`).

---

### Main components (existing + new)

| Layer | Component | Status |
|-------|-----------|--------|
| UI | `SimulateFlightsInAreaUI`, `SimulateFlightsInAreaAction` | Exists — extend display |
| Application | `SimulateFlightsInAreaController` | Exists |
| Application | `FlightSimulationService` | Exists — extend post-simulation |
| Application | `SimulationReportParser` | Exists — extend violations |
| Application | `SimulationResult` | Exists — add `summaryReportPath` |
| Application | `SimulationSummaryReportGenerator` | **New** |
| Application | `ReportGenerator` | **New** |
| Application | `SimulationSummaryRequest` | **New** |
| Infrastructure | `SimulationReportsPathResolver` | **New** |
| Domain | `FlightSimulationReport`, `SafetyViolation` | Exists |
| C | `flight_simulator`, `simulation_report_write_files` | Exists (US109) |

**Menu:** `MainMenu` → Flight Control → Simulate Flights in Area (US100) — summary shown at end of same action.

**Sequence diagram:** [us111-sd.puml](us111-sd.puml) / [us111-sd.svg](us111-sd.svg)

---

### Out of scope (v1)

* JPA persistence of simulation reports
* Separate menu action “View last simulation report”
* PDF or graphical reports
* Mapping simulator flight IDs back to IATA designators (optional enhancement)
