# US108 — Requirements

## User story

As a PO, I want semaphore synchronization on each time step, for lock-step between parent and child processes.

---

## Functional requirements

1. **R1 — Start semaphore**  
   Parent signals step start with `sem_post` on each active flight's start semaphore.

2. **R2 — Done semaphore**  
   Child signals completion with `sem_post` on done; parent `sem_wait`.

3. **R3 — SHM data**  
   Step position and metadata written to slot before `sem_post(done)`.

4. **R4 — Complete barrier**  
   Parent proceeds only after all active children complete the step.

5. **R5 — Pre-departure exclusion**  
   Flights before departure do not participate in barrier.

6. **R6 — Idle clock jump**  
   With no flight airborne, advance clock without semaphores.

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Synchronized multi-flight run terminates without hang. |
| AC2 | Collisions reflect same-step positions (US102). |
| AC3 | Staggered departures respect `departure_time_s`. |
| AC4 | Dead child detected as `COMMUNICATION LOST`. |

---

## Dependencies

- US105, US103 (semantics)

## Tests

[tests.md](tests.md)
