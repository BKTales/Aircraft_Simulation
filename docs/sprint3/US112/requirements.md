# US112 — Monthly Statistics Report Generation

## User Story

As a **Flight Control Operator**, I want to **generate a monthly statistics report** for my air control area.

This report is the first of several planned report types (e.g. compliance, incident). The implementation must be **foundational**: consistent branding, structure, and extensibility for future reports.

> **Enunciado (V3):** each report type may have its own data collection, sections, and graphics. For this sprint, **plain text** is sufficient (PO clarification via US111/US112).

---

## Requirements

### Functional

1. **R1 — Generate monthly report**  
   An authenticated and authorized Flight Control Operator can request a **monthly statistics report** for a selected **calendar month** and **air control area (ACA)**.

2. **R2 — FCO authorisation**  
   Only users with role `FLIGHT_CONTROL_OPERATOR` may generate the report.

3. **R3 — Single ACA scope**  
   Each report covers **exactly one ACA**. An FCO may only generate reports for the **ACA they are assigned to** (PO clarification — same rule as US111/US112).

4. **R4 — Month selection**  
   The FCO selects **year** and **month** (calendar month, e.g. `2026-06`). The report aggregates simulation activity whose **summary was generated** in that month (based on the `Generated at` timestamp in US111 summary files, or file name prefix `yyyy-MM-dd_*`).

5. **R5 — Data source**  
   Statistics are derived from **persisted US111 simulation summary files** under:
   ```text
   reports/simulations/{areaCode}/*-summary.txt
   ```
   The team may optionally enrich metrics from archived `*-report.csv` files; v1 minimum is US111 summary files.

6. **R6 — Statistics content (minimum)**  
   The monthly report includes:
   - report period (year-month) and ACA code;
   - **number of area simulations** run in the month;
   - **pass / fail** count of those simulations;
   - **total flights** simulated across all runs;
   - **total safety violations** recorded;
   - **per-simulation breakdown** (timestamp, final result, flight count, violation count).

7. **R7 — Persisted text file**  
   The report is stored as a **`.txt` file** on the filesystem (not transient). Path pattern (v1):
   ```text
   reports/monthly/{areaCode}/{yyyy-MM}-statistics.txt
   ```

8. **R8 — Console output**  
   After generation, the FCO sees a **formatted summary on the console** and the **absolute path** of the saved file (same UX pattern as US111).

9. **R9 — Empty month handling**  
   If no US111 summaries exist for the selected ACA and month, generation fails with a clear message (no empty file, or an explicit “no data” report — team chooses; recommended: **fail fast** with message).

10. **R10 — Strategy pattern**  
    Implementation uses the existing `ReportGenerator<RQ, RS>` interface (introduced in US111) with a new strategy:
    `MonthlyStatisticsReportGenerator`.

11. **R11 — Console entry point**  
    Backoffice menu: **Flight Control → Generate Monthly Statistics Report (US112)**.

12. **R12 — Foundational branding**  
    Header/footer lines, typography, and section ordering follow the same conventions as US111 (`SimulationSummaryReportGenerator`) so future report types remain consistent.

13. **R13 — Graphics (v1)**  
    Optional **ASCII** mini-charts (e.g. pass/fail bar) are allowed in text. PDF, images, or external chart libraries are **out of scope** for v1.

### Non-functional

14. **NFR1 — Layered architecture**  
    UI → controller → service → collector/parser → generator → path resolver.

15. **NFR2 — No database persistence**  
    The monthly report file on the filesystem is sufficient (aligned with US111 / PO clarification).

16. **NFR3 — Reuse reports base directory**  
    Same configuration as US111: `AISAFE_REPORTS_DIR` env var or `aisafe.reports.dir` system property (default `reports/`).

17. **NFR4 — Extensibility**  
    New report types (compliance, incident) add new `ReportGenerator` implementations and collectors without changing US111/US112 contracts.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given an FCO session and a month with at least one US111 summary for their ACA, when generation is requested, then `{yyyy-MM}-statistics.txt` is created under `reports/monthly/{areaCode}/`. |
| AC2 | The file contains simulation count, pass/fail totals, total flights, total violations, and a per-simulation table. |
| AC3 | Given a month with no summaries for the ACA, when generation is requested, then it fails with a clear error and no file is written. |
| AC4 | Given an FCO assigned to `AREA-0`, when they request a report for another ACA, then authorisation/validation fails. |
| AC5 | Given a user without FCO role, when generation is attempted, then authorisation fails. |
| AC6 | The console shows the report content (or key sections) and the saved file path. |
| AC7 | The report file remains readable after closing the console session. |
| AC8 | Report header branding matches US111 style (separator lines, AISafe title block). |

---

## Related User Stories

| US | Role |
|----|------|
| [US100](../US100/requirements.md) | Area simulation (produces data indirectly) |
| [US111](../US111/requirements.md) | Per-simulation summary files (primary data source) |
| US109 (SCOMP) | C simulator raw CSV (optional enrichment) |
| US050 | Air control area registration |

---

## Out of scope (v1)

- PDF / HTML / graphical dashboards
- Cross-area or global (multi-ACA) monthly reports
- RCOMP remote opcode (optional follow-up)
- Persisting monthly reports in RDBMS
- Compliance or incident report types (future US)
