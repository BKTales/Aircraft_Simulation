# US108 — Analysis

## User story

As a PO, I want step-by-step synchronization via semaphores, to guarantee lock-step between parent and child processes using shared memory.

## Context

US103 defined lock-step semantics (Sprint 2, pipes). US108 implements the protocol on US105 infrastructure:

- `sem_post(step_start[i])` — parent authorizes child to process step N
- `sem_wait(step_done[i])` — parent waits for child completion
- Data in SHM slot — child writes `update`, parent reads after barrier

## Edge cases

| Situation | Behaviour |
|-----------|-----------|
| Not yet departed | Excluded from barrier (`departure_time_s > sim_time_s`) |
| No flight airborne | `try_advance_time` jumps clock; no semaphores |
| Child unresponsive | `update_ready == 0` → `COMMUNICATION LOST` |
| `sem_post` failure | `mark_comm_lost_on_publish_failure` |

## Dependencies

- US105 — SHM + semaphore creation
- US101 — child processes step after `sem_wait(start)`
- US106 — safety runs after complete collect

## Diagrams

[../SCOMP/us108-step-barrier-sd.puml](../SCOMP/us108-step-barrier-sd.puml)  
[../SCOMP/shm-sync-overview.md](../SCOMP/shm-sync-overview.md)

## Tests

[tests.md](tests.md)
