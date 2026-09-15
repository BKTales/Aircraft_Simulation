# US105 — Analysis

## User story

As a PO, I want a hybrid simulation environment with shared memory, to replace pipes while keeping one child process per flight and enabling efficient position communication.

## Context

Sprint 2 (US100–US103) used **two pipes per child** (tick + update). In Sprint 3, the SCOMP team refactors to:

- **SHM** (`shm_open` + `mmap`) — flight data and global clock
- **Semaphores** — lock-step synchronization (US108)
- **Parent threads** — safety, report, environment (US106+)

US105 delivers **IPC infrastructure** and memory layout; US108 implements the step protocol.

## Problem solved

| Aspect | Pipes (S2) | SHM (S3) |
|--------|------------|----------|
| Data copy | `read`/`write` sizeof(update) per step | Direct slot write |
| Scalability | 2×N file descriptors | 1 region + 2×N semaphores |
| Global metadata | Implicit in parent | `sim_global_t` visible to all |

## Rules

1. Each child writes **only** to its `slot_index` (assigned before `fork`).
2. Parent creates and destroys SHM/semaphores; children open by name (`FS_RUN_ID`).
3. `fork_flights()` runs on the main thread, never inside workers.
4. Cleanup: `sim_shm_destroy`, `sim_sem_destroy`, `munmap`, `shm_unlink`.

## Dependencies

- US100 — multi-process model
- US108 — uses semaphores created here
- US110 — extends `sim_global_t` with `environment`

## Risks

| Risk | Mitigation |
|------|------------|
| Orphan SHM after crash | Names include PID; documented manual cleanup |
| Parent/child slot race | `update_ready` protocol + semaphores |

## Tests

Full regression `run_tests.sh all` — SHM works if sim completes with report. See [tests.md](tests.md) and [TESTS.md](../../scomp/TESTS.md).
