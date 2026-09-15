# US109 — Requirements

## User story

As a PO, I want final simulation report generation, to analyse results and violations after execution.

---

## Functional requirements

1. **R1 — TXT report**  
   Human-readable file with summary, flights, and violations.

2. **R2 — CSV report**  
   Structured format with metrics, flights, and violations sections.

3. **R3 — Global result**  
   `validation_result` field with `PASS` or `FAIL`.

4. **R4 — Per-flight record**  
   Each simulated flight appears with `execution_status` and final data.

5. **R5 — Violation record**  
   Safety events with step, IDs, and coordinates.

6. **R6 — Thread-safe (Sprint 3)**  
   Concurrent writes protected by mutex (US107).

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | After simulation, `report.txt` and `report.csv` exist. |
| AC2 | CSV contains `validation_result,PASS` in `all_valid`. |
| AC3 | CSV contains `validation_result,FAIL` in `collision`. |
| AC4 | Java parses CSV without errors (`SimulationReportParserTest`). |
| AC5 | Report survives `FS_NON_INTERACTIVE=1` (not deleted). |

---

## Tests

[TESTS.md](../TESTS.md), [Sprint 3 tests](../../sprint3/US109/tests.md).
