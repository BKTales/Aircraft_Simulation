# US107 — Analysis

## User story

As a PO, I want the safety violation detection thread to notify the report generation thread through condition variables when a safety violation occurs, so that the report is updated in real time with accurate information.

## Context

Sprint 3 SCOMP refactors the flight simulator to a hybrid model (US105–US106): parent process with dedicated pthreads and child flight processes using shared memory.

US106 introduced `safety_dedicated_thread_fn` and `report_dedicated_thread_fn` in [`sim_dedicated_thread.c`](../../../flight_simulator/src/main/utils/sim_dedicated_thread.c). The initial implementation signalled the report thread but still recorded violations synchronously inside `check_collisions()` on the safety thread.

US107 completes the decoupling: detection enqueues events; the report thread records them.

## Acceptance criteria mapping

| Criterion | Implementation |
|-----------|----------------|
| Safety thread monitors SHM for conflicts | `check_collisions_detect()` reads `ipc->shm->slots[i].update` after each collect step |
| Safety thread signals report thread on violation | `pthread_cond_signal(&violation_cond)` after enqueue |
| Report thread waits on cond var and logs violation | `report_dedicated_thread_fn` waits on `violation_cond`, calls `sim_record_violation_batch()` |
| Mutex-protected notification | `violation_mutex` for queue; `simulation_report_t.mutex` for report writes |

## Dependencies

- US105 — shared memory and parent context
- US106 — dedicated safety and report threads
- US108 — step collect publishes fresh SHM updates before safety scan

## Risks

- Concurrent report writes from main thread (`check_flight_status`, comm lost) and report thread — mitigated with `simulation_report_t.mutex`.
- Queue overflow (`VIOLATION_QUEUE_MAX`) — unlikely in course scenarios; extra collisions are dropped with log from safety thread.
