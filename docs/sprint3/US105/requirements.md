# US105 — Requirements

## User story

As a PO, I want a hybrid simulation environment with shared memory and child processes, for efficient parent–flight communication.

---

## Functional requirements

1. **R1 — Shared memory**  
   POSIX `shm_open` region mapped with `mmap` containing global state and per-flight slots.

2. **R2 — Typed layout**  
   Structures `sim_global_t`, `sim_flight_slot_t`, `flight_update_t` in `sim_ipc.h`.

3. **R3 — Named semaphores**  
   Start/done pair per slot via `sem_open`.

4. **R4 — Run identification**  
   `FS_RUN_ID` (parent PID) prefixes IPC names.

5. **R5 — Slot per child**  
   Slot index passed to child; no cross-slot writes.

6. **R6 — Shutdown**  
   `global.shutdown` flag visible to children for graceful termination.

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Multi-flight simulation completes using SHM (no data pipes). |
| AC2 | Children open semaphores using parent `run_id`. |
| AC3 | IPC resources released after exit. |
| AC4 | `sim_shm_bytes(capacity)` computes correct region size. |

---

## Dependencies

- US100 (fork model)
- US108 (step protocol)

## Tests

[tests.md](tests.md)
