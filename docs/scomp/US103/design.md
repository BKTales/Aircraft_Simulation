# US103 — Design

## Sprint 2 — Pipes

```
Parent                       Child
  | write(ctrl_pipe, "GO")     |
  | -------------------------> | read(ctrl_pipe)
  |                            | advance_step()
  | read(data_pipe, update)    |
  | <------------------------- | write(data_pipe, update)
```

Historical pseudo-code in [README.md](../README.md) § Sprint 2.

## Sprint 3 — Semaphores + SHM (US108)

Current implementation in [`flight_simulator.c`](../../../flight_simulator/src/main/flight_simulator.c):

### Publish (`sim_step_publish`)

1. Write `sim_time_s`, `dt_s` to `global`.
2. For each active departed slot: set `pending_step`, clear `update_ready`.
3. `global.active_flights = n_active`.
4. `sem_post(step_start[i])` for each.

Returns `0` if `n_active == 0` (skip collect).

### Collect (`sim_step_collect`)

1. `sem_wait(step_done[i])` for all active flights.
2. If `slot.update_ready`: copy `slot.update` → `latest_updates[i]`.
3. Else: kill child, record `COMMUNICATION LOST`.

### Advance time (`try_advance_time`)

When publish returns 0, advance `global_sim_time_s` until next `departure_time_s` without semaphores.

## Child loop

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

## Diagram

See [us108-step-barrier-sd.puml](../../sprint3/SCOMP/us108-step-barrier-sd.puml).

## Decisions

| Decision | Reason |
|----------|--------|
| Per-slot semaphores | Independent flights join/leave barrier |
| `update_ready` flag | Distinguish written slot vs dead child |
| Main thread publish/collect | Deterministic IPC; no fork races |
