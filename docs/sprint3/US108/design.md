# US108 — Design

## Main functions

| Function | File | Role |
|----------|------|------|
| `sim_step_publish` | `flight_simulator.c` | Write SHM + `sem_post(start)` |
| `sim_step_collect` | `flight_simulator.c` | `sem_wait(done)` + read slots |
| `try_advance_time` | `flight_simulator.c` | Time jump without barrier |
| `sem_wait/start` (child) | `flight_process.c` | Process step + `sem_post(done)` |

## Detailed protocol

### Publish

1. `global.sim_time_s = global_sim_time_s`
2. `global.dt_s = DT_S`
3. For each active departed slot: set `pending_step`, clear `update_ready`
4. `global.active_flights = n_active`
5. `sem_post(step_start[i])` for each

Returns `0` if `n_active == 0` (skip collect).

### Collect

1. `sem_wait(step_done[i])` for all active flights
2. If `slot.update_ready`: copy `slot.update` → `latest_updates[i]`
3. Else: kill child, record `COMMUNICATION LOST`

### Child loop

```c
while (!stop_flag && !safety_violation_flag) {
    sem_wait(step_start);
    if (global.shutdown) break;
    physics_update(...);
    slot->update = ...;
    slot->update_ready = 1;
    sem_post(step_done);
}
```

## try_advance_time

When publish returns 0, advance `global_sim_time_s` in `DT_S` steps until a flight reaches `departure_time_s` — no `flight_steps` increment, clock only.

## Semaphores

Created in [`sim_sem.c`](../../../flight_simulator/src/main/ipc/sim_sem.c):

- Parent: `sem_open` with `O_CREAT`
- Child: `sem_open` without create, via `FS_RUN_ID` + slot index

## Diagram

See [../SCOMP/us108-step-barrier-sd.puml](../SCOMP/us108-step-barrier-sd.puml).

## Decisions

| Decision | Reason |
|----------|--------|
| Per-step barrier, not single global sem | Flights join/leave independently |
| `update_ready` flag | Distinguish written slot vs dead child |
| Main thread publish/collect | Avoid races with fork and thread pool |
