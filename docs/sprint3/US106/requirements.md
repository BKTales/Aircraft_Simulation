# US106 — Requirements

## User story

As a PO, I want function-specific threads (safety and report), for concurrent processing in the parent process.

---

## Functional requirements

1. **R1 — Safety thread**  
   Dedicated thread runs violation detection after each collect step.

2. **R2 — Report thread**  
   Dedicated thread records events in `simulation_report_t`.

3. **R3 — Synchronization**  
   Use `pthread_mutex_t` and `pthread_cond_t` between main, safety, and report.

4. **R4 — Startup**  
   Threads created at simulation start via `pthread_create`.

5. **R5 — Shutdown**  
   Broadcast `sim_done`, join all threads before releasing IPC.

6. **R6 — Main thread IPC**  
   Publish/collect remain on main thread (US108 determinism).

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Simulation completes without deadlock between threads. |
| AC2 | Collisions detected by safety thread (`collision` env). |
| AC3 | `[REPORT]` logs appear from report thread. |
| AC4 | Threads terminate cleanly on PASS and FAIL runs. |

---

## Dependencies

- US105, US102, US107 (async report extension)

## Tests

[tests.md](tests.md)
