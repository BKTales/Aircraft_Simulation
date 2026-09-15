# US111 — Generate a Simulation Report

## User Story

As a **Flight Control Operator**, I want to receive a **summary of the simulation results** so that I can determine if the programmed flights are safe to run.

---

## Requirements

### Functional

1. **R1 — Summary after area simulation**  
   After a successful area simulation (US100), the system generates a summary report for the Flight Control Operator.

2. **R2 — Flight counts and execution status**  
   The summary includes the **total number of flights** simulated and the **execution status** of each one.

3. **R3 — Safety violations detail**  
   When safety violations occur, the summary lists each event with a **timestamp** (simulation step / elapsed seconds) and **position** (latitude, longitude, altitude).

4. **R4 — Pass/fail validation result**  
   The summary clearly indicates whether the scheduled flights **passed or failed** validation.

5. **R5 — Persisted text file**  
   The summary is stored as a **`.txt` file** on the filesystem. The file is **not transient** (it remains available after the session ends).

6. **R6 — Seamless experience**  
   The FCO runs the simulation from the backoffice console (US100) and receives the summary in the **same flow** — no separate manual step to import a C report (PO clarification US111).

7. **R7 — Data source**  
   Summary data is derived from the US109 C simulator output (`report.csv` parsed in Java). Optionally, the raw `report.csv` / `report.txt` may be archived alongside the summary for audit or US112.

8. **R8 — Single air control area**  
   Each summary report covers exactly the **air control area (ACA)** selected for the simulation. An FCO does not generate reports for areas they do not operate in (PO clarification US112, point 5).

9. **R9 — Authorisation**  
   Only **Flight Control Operator** users may trigger area simulation and receive the summary report.

10. **R10 — Strategy pattern foundation**  
    Report generation follows a **Strategy pattern** interface so that future report types (e.g. US112 monthly statistics) can reuse the same structure with different data sources and formats.

### Non-functional

11. **NFR1 — Layered architecture**  
    Console UI, application controller/service, parser, generator, and filesystem integration follow the project layered architecture.

12. **NFR2 — No database persistence for summary**  
    The summary file on the filesystem is sufficient. Persisting the report in the relational database (NFR08) is **not required** (PO clarification US111).

13. **NFR3 — Text format**  
    Output format is plain text (`.txt`). Graphs or PDF are optional enhancements.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given a successful US100 area simulation, when the simulation completes, then a `.txt` summary file is created in a permanent directory (not a temp dir). |
| AC2 | The summary contains the total number of flights, per-flight execution status, and the global PASS/FAIL result. |
| AC3 | Given safety violations in the US109 CSV, the summary lists each event with step/elapsed time and coordinates for the involved flights. |
| AC4 | Given a simulation with no violations, the violations section states zero events or is omitted with a clear message. |
| AC5 | The FCO sees the formatted summary in the console (not only PASS/FAIL) and the path of the saved file. |
| AC6 | Given a user who is not a Flight Control Operator, when the simulation controller is invoked, then authorisation fails. |
| AC7 | The summary references the selected ACA code and the simulation time interval. |
| AC8 | The summary file remains accessible after closing the console session. |

---

## Product Owner Clarifications

### US111 (direct)

| Question | PO answer |
|----------|-----------|
| Should Java read a C file independently, or trigger simulation from UI? | Seamless experience — the FCO runs simulation and gets the summary in one flow. |
| Persist summary in RDBMS? | User story says store a file on the filesystem; it is not transient. |

### US112 (indirect — design guidance for US111)

| Topic | PO guidance |
|-------|-------------|
| Report format | Text file is enough. |
| Data source | Team chooses; Strategy pattern is key (enunciado page 23). |
| Violation granularity | Team chooses (per-event vs aggregate). |
| Multi-area reports | FCO should not generate reports for an ACA they do not work in. |

---

## Related User Stories

| US | Role |
|----|------|
| [US100](../US100/requirements.md) | FCO triggers area simulation (prerequisite) |
| US109 (SCOMP) | C simulator generates `report.csv` / `report.txt` |
| [US085](../US085/requirements.md) | Pilot validation — shares simulation engine, does **not** produce summary file |
| US112 | Monthly statistics report — reuses Strategy pattern introduced here |
