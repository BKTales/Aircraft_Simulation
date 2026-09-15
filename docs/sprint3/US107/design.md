# US107 — Design

## Objective

When the safety thread detects a collision, enqueue a `pending_violation_t` and wake the report thread with a condition variable. The report thread drains the queue and writes violation and flight records into `simulation_report_t`.

## Modules

| File | Role |
|------|------|
| [`sim_dedicated_thread.h`](../../../flight_simulator/src/main/utils/sim_dedicated_thread.h) | Sync primitives, violation queue, thread API |
| [`sim_dedicated_thread.c`](../../../flight_simulator/src/main/utils/sim_dedicated_thread.c) | Safety/report thread loops, batch recording |
| [`parent_safety.c`](../../../flight_simulator/src/main/utils/parent_safety.c) | `check_collisions_detect()` — SHM scan + enqueue |
| [`flight_report.c`](../../../flight_simulator/src/main/utils/flight_report.c) | Mutex-protected `simulation_report_record_*` |

## Data structures

```c
typedef struct {
    int flight_i, flight_j, sim_step;
    double dist_m, alt_diff;
    flight_update_t update_i, update_j;
} pending_violation_t;

typedef struct {
    pthread_mutex_t violation_mutex;
    pthread_cond_t  violation_cond;
    pending_violation_t violations[VIOLATION_QUEUE_MAX];
    size_t violation_queue_len;
    /* step + shutdown sync ... */
} sim_thread_sync_t;
```

## Per-step flow

1. Main thread runs `sim_step_collect()` (reads SHM into `latest_updates`).
2. Main calls `sim_dedicated_threads_after_collect()` → signals safety thread.
3. Safety thread runs `check_collisions_detect()`:
   - Scans SHM slots for pairs within horizontal/vertical thresholds.
   - Sends `SIGUSR1`, marks flights inactive.
   - Enqueues `pending_violation_t`, `pthread_cond_signal(violation_cond)`.
4. Report thread wakes, copies queue under `violation_mutex`, unlocks, calls `sim_record_violation_batch()`.
5. Main continues with `check_flight_status()` while report thread may record in parallel.

## Shutdown

`sim_dedicated_threads_stop()` sets `sim_done`, broadcasts all condition variables, joins threads. Report thread writes final TXT/CSV via `handle_report_output()`.

## Diagram

See [`us107-violation-notify-sd.puml`](us107-violation-notify-sd.puml).
