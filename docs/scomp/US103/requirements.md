# US103 — Requirements

## User story

As a PO, I want execution synchronized by time step, to guarantee temporal coherence in multi-flight simulation.

---

## Functional requirements

1. **R1 — Global step**  
   All active flights advance exactly one step per synchronization cycle.

2. **R2 — Barrier**  
   Parent advances clock only after collecting updates from all active children for the current step.

3. **R3 — Inactive exclusion**  
   Finished or not-yet-departed flights do not participate in the barrier.

4. **R4 — Time jump**  
   When no flight is airborne, clock advances to next departure without semaphores.

5. **R5 — Fixed DT**  
   Each step represents `DT_S` seconds (1 s).

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Multi-flight simulation terminates without deadlock. |
| AC2 | Collisions reflect same-step positions (US102). |
| AC3 | Staggered departures respect `departure_time_s`. |
| AC4 | `global_sim_time_s` increases monotonically. |

---

## Dependencies

- US105, US108 (Sprint 3 implementation)
- US103 semantics (Sprint 2)

## Tests

[TESTS.md](../TESTS.md), [US108 tests](../../sprint3/US108/tests.md).
