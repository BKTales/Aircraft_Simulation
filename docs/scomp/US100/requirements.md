# US100 — Requirements (Sprint 2)

## User story

As a PO, I want to implement the base simulation with processes and threads, to run multiple flight plans in parallel.

---

## Functional requirements

1. **R1 — Parent process**  
   The simulator starts a parent process that manages the simulation lifecycle.

2. **R2 — Child processes**  
   For each valid flight plan, the parent creates a dedicated child via `fork()`.

3. **R3 — Plan loading**  
   Plans are read from JSON files in a configurable directory.

4. **R4 — Pre-simulation validation**  
   Plans with invalid payload/MTOW/fuel are marked and not simulated.

5. **R5 — Coordinated termination**  
   Parent waits for all children (`waitpid`) before generating the final report.

6. **R6 — Report**  
   An aggregated report is produced at the end (US109).

---

## Non-functional requirements

7. **R7 — POSIX portability**  
   Use only POSIX APIs (`fork`, `pipe`/`shm`, `signal`).

8. **R8 — Environment configuration**  
   Plans and reports directories configurable via environment variables.

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given N valid plans, N child processes are created. |
| AC2 | Given an invalid plan, no child is created; simulation continues with others. |
| AC3 | Parent does not exit before all children. |
| AC4 | TXT/CSV report exists after successful simulation. |
| AC5 | Binary builds with `make flight_simulator`. |

---

## Dependencies

- JSON parser (`cJSON`, `flight_plan_parser.c`)
- US101, US102, US103, US109

---

## Tests

See [TESTS.md](../TESTS.md) and `bash scripts/run_tests.sh all`.
