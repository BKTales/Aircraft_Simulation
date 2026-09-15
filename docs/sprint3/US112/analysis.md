## ANALYSIS

**Client requirement (enunciado V3, page 23)**

The actor is the **Flight Control Operator**. Unlike US111 (generated automatically after each US100 simulation), US112 is an **on-demand** report: the FCO selects a **calendar month** and receives **aggregated statistics** for their assigned air control area.

The enunciado positions US112 as **foundational** for a family of reports (compliance, incident, etc.). Each future type may differ in data collection and sections, but must share **consistent branding and structure**. Graphics are envisioned long-term; the PO confirmed **text format is enough** for this sprint.

---

## BUSINESS RULES

* Only **Flight Control Operator** role may generate monthly statistics.
* Report scope is **one ACA** per generation request.
* FCO may only request reports for the **ACA linked to their `FlightControlOperatorUser` record** (same constraint as US111 multi-area clarification).
* Calendar month is inclusive: a US111 summary belongs to month *M* when its `Generated at` timestamp falls in *M* (timezone: system default / `LocalDateTime` on server).
* Data is read from **existing US111 summary files** (`reports/simulations/{areaCode}/*-summary.txt`). No new simulation is run.
* If **zero** summaries match ACA + month → operation fails with explicit message (recommended).
* Output path: `reports/monthly/{areaCode}/{yyyy-MM}-statistics.txt`.
* Regenerating the same month **overwrites** the file (idempotent path; last generation wins).
* US112 is **read-only** on domain aggregates — no flight or plan status changes.

---

## DATA SOURCE DECISION

| Option | Pros | Cons | Decision |
|--------|------|------|----------|
| Parse US111 `*-summary.txt` | Stable contract we own; US111 docs mention US112 consumption | Requires a lightweight parser | **Chosen for v1** |
| Parse archived `*-report.csv` | Richer raw metrics | Coupled to US109 CSV layout | Optional enrichment later |
| Query database | Live data | PO said file persistence is enough for reports; no report entity in DB | Not v1 |

**Parser input:** files matching `reports/simulations/{areaCode}/yyyy-MM-dd_HHmmss-summary.txt` where the date prefix falls in the selected year-month.

**Parsed fields per simulation (minimum):**

| Field | Source in summary file |
|-------|------------------------|
| `generatedAt` | `Generated at` line |
| `finalResult` | `FINAL RESULT : PASS/FAIL` |
| `totalFlights` | `Total flights` line |
| `successCount` | `Completed (SUCCESS)` line |
| `violationCount` | `SAFETY VIOLATIONS (N)` header |

---

## US112 vs US111 vs US100

| Aspect | US100 | US111 | US112 |
|--------|-------|-------|-------|
| Trigger | FCO runs simulation | Automatic post-US100 | FCO menu action |
| Granularity | One run | One run | One calendar month |
| Data | Live simulation | Single CSV parse | Aggregate of summaries |
| Output | Pass/fail (+ summary via US111) | `*-summary.txt` | `{yyyy-MM}-statistics.txt` |
| Strategy class | — | `SimulationSummaryReportGenerator` | `MonthlyStatisticsReportGenerator` |

---

## RELATION TO US111

US111 established:

* `ReportGenerator<RQ, RS>` interface;
* text report branding (`================` lines, section headers);
* filesystem layout under `reports/`;
* optional archive of raw US109 artefacts.

US112 **consumes** US111 outputs and **extends** the reporting subsystem without modifying the US100/US111 flow.

---

## IMPLEMENTATION STATUS

All v1 components are implemented and covered by automated tests.

| Component | Location | Status |
|-----------|----------|--------|
| Menu action (item 2) | `MainMenu.buildFlightControlMenu()` | Done |
| UI + Action | `GenerateMonthlyStatisticsReportUI`, `GenerateMonthlyStatisticsReportAction` | Done |
| Controller + Service | `GenerateMonthlyStatisticsReportController`, `GenerateMonthlyStatisticsReportService` | Done |
| FCO session (assigned ACA) | `FlightControlOperatorSession` | Done |
| Summary parser + collector | `SimulationSummaryFileParser`, `SimulationSummaryArchiveCollector` | Done |
| Aggregator + snapshot | `MonthlyStatisticsAggregator`, `MonthlyStatisticsSnapshot` | Done |
| Report generator (strategy) | `MonthlyStatisticsReportGenerator` | Done |
| DTOs | `MonthlyReportRequest`, `MonthlyReportResult`, `SimulationSummaryRecord` | Done |
| Empty-month exception | `NoMonthlySimulationDataException` | Done |
| Path resolver | `MonthlyReportsPathResolver` | Done |
| FCO repository lookup | `FlightControlOperatorUserRepository.findByUsername()` (JPA + in-memory) | Done |
| Documentation | `docs/sprint3/US112/` | Done |

**Out of scope (v1, unchanged):** RCOMP opcode, PDF/HTML, multi-ACA consolidated reports, RDBMS persistence, CSV enrichment from US109.

**Auxiliary (demo / in-memory):** `InMemoryDemoBootstrap` ensures FCO bootstrap data is available when running with `application-inmemory.properties`.

---

## DOMAIN / APPLICATION MODEL (v1)

No new JPA aggregate is required. US112 works with:

* **Value object** `MonthlyStatisticsSnapshot` (in-memory aggregation result)
* **Value object** `SimulationSummaryRecord` (one parsed US111 file)
* **DTO** `MonthlyReportRequest` / `MonthlyReportResult`

See [design.md](design.md) for component mapping.

---

## ASSUMPTIONS

1. US111 summaries exist on disk from prior US100 runs (bootstrap or manual testing).
2. File timestamp in summary filename (`yyyy-MM-dd_HHmmss`) is consistent with `SimulationReportsPathResolver`.
3. FCO `fco1` is assigned to `AREA-0` in bootstrap (existing data).
4. Monthly report uses the same `reports/` base directory as US111.

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/` (all passing):

| Test class | Focus |
|------------|-------|
| `SimulationSummaryFileParserTest` | Parse US111 summary text; invalid/missing fields |
| `MonthlyStatisticsCollectorTest` | Filter by ACA + month; I/O and validation |
| `MonthlyStatisticsAggregatorTest` | Totals, pass rate, empty input |
| `MonthlyStatisticsReportGeneratorTest` | File output, branding, overwrite, I/O failure |
| `MonthlyStatisticsSnapshotTest` | Snapshot and breakdown row accessors |
| `MonthlyReportRequestTest` / `MonthlyReportResultTest` | DTO validation |
| `SimulationSummaryRecordTest` | Record accessors |
| `NoMonthlySimulationDataExceptionTest` | Exception message |
| `GenerateMonthlyStatisticsReportServiceTest` | Orchestration, validation, empty month |
| `GenerateMonthlyStatisticsReportControllerTest` | FCO auth, ACA scope, default constructor |
| `FlightControlOperatorSessionTest` | Session edge cases |
| `MonthlyReportsPathResolverTest` | `reports/monthly/{area}/{yyyy-MM}-statistics.txt` |

Fixtures: `Us112SummaryFixtures`, `Us112TestRepositoryFactory`.

Manual acceptance: `GenerateMonthlyStatisticsReportUI` — login FCO `fco1`, select year/month, verify console output and persisted file. See [tests.md](tests.md).
