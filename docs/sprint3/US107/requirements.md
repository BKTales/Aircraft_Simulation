# US107 — Requirements

## User Story

As a PO, I want the safety violation detection thread to notify the report generation thread through condition variables when a safety violation occurs, so that the report is updated in real time with accurate information.

---

## Requirements

### Functional

1. **R1 — Dedicated safety thread**
   The parent process runs a safety thread that monitors shared-memory flight updates for proximity violations (US106).

2. **R2 — Condition-variable notification**
   When a violation is detected, the safety thread enqueues a `pending_violation_t` and signals the report thread via `pthread_cond_signal`.

3. **R3 — Report thread consumption**
   The report thread waits on the condition variable, drains the violation queue under mutex, and records events in `simulation_report_t`.

4. **R4 — Decoupled recording**
   Violation **detection** (safety thread) is separated from violation **recording** (report thread); the main thread no longer writes violation rows synchronously inside `check_collisions()`.

5. **R5 — Mutex protection**
   Queue access uses `violation_mutex`; report file writes use `simulation_report_t.mutex`.

### Non-functional

6. **R6 — No deadlock with step sync**
   Notification must not break the US108 step barrier between main, safety, and report threads.

7. **R7 — Graceful shutdown**
   On simulation end, threads are broadcast-woken and joined; the report thread writes final TXT/CSV output.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Safety thread scans SHM slots after each collect step and detects pairs within horizontal/vertical thresholds. |
| AC2 | On violation, safety thread sends `SIGUSR1`, marks flights inactive, enqueues event, and signals `violation_cond`. |
| AC3 | Report thread wakes, records violation batch, and logs `[REPORT] Safety violation recorded`. |
| AC4 | Collision test environment produces `safety_violation_events > 0` in CSV; `all_valid` produces zero violations. |
| AC5 | Simulation completes without deadlock between safety, report, and main threads. |

---

## Dependencies

- US105 — shared memory IPC layout
- US106 — dedicated safety and report threads
- US108 — step collect publishes fresh SHM updates before safety scan
