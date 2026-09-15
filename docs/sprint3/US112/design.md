## DESIGN

US112 adds an **on-demand monthly statistics report** for Flight Control Operators. It reuses the **Strategy pattern** from US111 and reads **persisted simulation summaries** as the data source.

---

### Responsibilities

| Layer | Component | Responsibility |
|-------|-----------|----------------|
| **UI** | `GenerateMonthlyStatisticsReportUI` | Select year/month; display report + file path |
| **UI** | `GenerateMonthlyStatisticsReportAction` | Menu action wrapper |
| **Controller** | `GenerateMonthlyStatisticsReportController` | FCO auth; resolve assigned ACA; delegate |
| **Application** | `GenerateMonthlyStatisticsReportService` | Orchestrate collect → aggregate → generate |
| **Application** | `SimulationSummaryArchiveCollector` | Find and load US111 summaries for ACA + month |
| **Application** | `SimulationSummaryFileParser` | Parse one `*-summary.txt` into `SimulationSummaryRecord` |
| **Application** | `MonthlyStatisticsAggregator` | Build `MonthlyStatisticsSnapshot` from records |
| **Application** | `MonthlyStatisticsReportGenerator` | Format and write monthly `.txt` (`ReportGenerator`) |
| **Application** | `MonthlyReportRequest` / `MonthlyReportResult` | Strategy input/output DTOs |
| **Application** | `FlightControlOperatorSession` | Resolve FCO user → assigned `AreaCode` |
| **Infrastructure** | `MonthlyReportsPathResolver` | `reports/monthly/{area}/{yyyy-MM}-statistics.txt` |
| **Infrastructure** | `ReportsBasePathResolver` (optional extract) | Shared `AISAFE_REPORTS_DIR` / `aisafe.reports.dir` logic |

---

### Flow

1. FCO opens **Flight Control → Generate Monthly Statistics Report (US112)**.
2. UI shows assigned ACA (read-only) and prompts for **year** and **month**.
3. `GenerateMonthlyStatisticsReportController.generate(year, month)`:
   - ensures `FLIGHT_CONTROL_OPERATOR` role;
   - resolves ACA from `FlightControlOperatorSession`;
   - delegates to service.
4. `SimulationSummaryArchiveCollector` lists `reports/simulations/{areaCode}/*-summary.txt` and filters by selected year-month (filename date prefix and/or parsed `Generated at`).
5. If no files → `NoMonthlySimulationDataException` (or `IllegalArgumentException` with clear message).
6. Each file parsed by `SimulationSummaryFileParser` → `SimulationSummaryRecord`.
7. `MonthlyStatisticsAggregator` computes totals and per-simulation rows → `MonthlyStatisticsSnapshot`.
8. `MonthlyStatisticsReportGenerator.generate(MonthlyReportRequest)` writes text file via `MonthlyReportsPathResolver`.
9. UI prints formatted content and absolute path.

**US100 / US111 flows are unchanged.**

---

### Strategy pattern

```java
public interface ReportGenerator<RQ, RS> {
    RS generate(RQ request);
}
```

| Implementation | US | Request | Result |
|----------------|-----|---------|--------|
| `SimulationSummaryReportGenerator` | US111 | `SimulationSummaryRequest` | `SimulationSummaryResult` |
| `MonthlyStatisticsReportGenerator` | US112 | `MonthlyReportRequest` | `MonthlyReportResult` |

`MonthlyReportRequest` fields (v1):

* `areaCode`
* `YearMonth period`
* `MonthlyStatisticsSnapshot snapshot`
* `LocalDateTime generatedAt`

---

### Filesystem layout

```
reports/
├── simulations/                    ← US111 (input)
│   └── AREA-0/
│       ├── 2026-06-08_143000-summary.txt
│       └── 2026-06-15_101500-summary.txt
└── monthly/                        ← US112 (output)
    └── AREA-0/
        └── 2026-06-statistics.txt
```

Base directory: `AISAFE_REPORTS_DIR` → `aisafe.reports.dir` → default `reports/` (same as US111).

---

### Monthly report format (example)

Path: `reports/monthly/AREA-0/2026-06-statistics.txt`

```text
================================================================================
 AISafe MONTHLY STATISTICS REPORT
================================================================================
 Area              : AREA-0
 Period            : 2026-06
 Generated at      : 2026-06-09 12:00:00

 SIMULATION ACTIVITY
   Simulations run   : 3
   Passed            : 2
   Failed            : 1
   Pass rate         : 66.7%

 FLIGHT STATISTICS
   Total flights     : 12
   Successful exec.  : 10
   Failed execution  : 2

 SAFETY OVERVIEW
   Total violations  : 4

 PER-SIMULATION BREAKDOWN
   Date/Time              Result  Flights  Violations
   2026-06-08 14:30:00    PASS    5        0
   2026-06-10 09:15:00    FAIL    4        3
   2026-06-15 10:15:00    PASS    3        1

 PASS/FAIL CHART (ASCII)
   PASS [#############-------] 2
   FAIL [######--------------] 1
================================================================================
```

Branding rules (shared with US111):

* 80-char `=` separator lines
* Title line prefixed with single space: ` AISafe ...`
* Section headers in ALL CAPS
* Key-value lines use ` Label : value` alignment

---

### FCO area scoping

`FlightControlOperatorSession` (new helper, mirrors `CompanyRouteCollaboratorSession`):

```java
FlightControlOperatorUser requireFlightControlOperator(
    AuthorizationService authz,
    FlightControlOperatorUserRepository operators);
```

* Loads collaborator by authenticated username.
* Exposes `airControlArea().identity()` as the only ACA available for US112.
* Controller rejects explicit ACA override from UI (ACA is implicit from session).

---

### Main components (new)

| Package | Class | Status |
|---------|-------|--------|
| `...application` | `GenerateMonthlyStatisticsReportController` | **New** |
| `...application` | `GenerateMonthlyStatisticsReportService` | **New** |
| `...application.reporting` | `MonthlyStatisticsReportGenerator` | **New** |
| `...application.reporting` | `MonthlyReportRequest`, `MonthlyReportResult` | **New** |
| `...application.reporting` | `MonthlyStatisticsSnapshot`, `SimulationSummaryRecord` | **New** |
| `...application.reporting` | `SimulationSummaryArchiveCollector` | **New** |
| `...application.reporting` | `SimulationSummaryFileParser` | **New** |
| `...application.reporting` | `MonthlyStatisticsAggregator` | **New** |
| `...application` | `FlightControlOperatorSession` | **New** |
| `...application.exceptions` | `NoMonthlySimulationDataException` | **New** |
| `...infrastructure.reporting` | `MonthlyReportsPathResolver` | **New** |
| `...console...flightcontrol` | `GenerateMonthlyStatisticsReportUI`, `Action` | **New** |

**Menu change:** `MainMenu.buildFlightControlMenu()` — add item 2: `Generate Monthly Statistics Report (US112)`.

**Sequence diagram:** [us112-sd.puml](us112-sd.puml)

---

### Out of scope (v1)

* RCOMP opcode for monthly report
* PDF / charts beyond ASCII
* Database persistence
* Multi-area consolidated reports
* Parsing archived CSV as primary source
