# US103 — Analysis

## User story

As a PO, I want step-by-step execution synchronization, so all flights advance together at the same simulation instant.

## Context

Without synchronization, each child would advance at its own pace and positions compared for collisions (US102) would be from **different moments** — false positives/negatives.

The **lock-step** model ensures:

1. Parent publishes tick step N.
2. All active children process step N.
3. Parent collects all updates before advancing to N+1.

## Architectural evolution

| Sprint | Mechanism |
|--------|-----------|
| 2 (US103) | Pipes: parent writes `"GO\n"`, child responds with `FlightUpdate` |
| 3 (US108) | POSIX named semaphores: `sem_post(start)` / `sem_wait(done)` + SHM |

Lock-step **semantics** are identical; only transport changed.

## Edge cases

- **Pre-departure:** not-yet-departed flights excluded from barrier.
- **Post-arrival:** `done` or inactive flights excluded.
- **Clock jump:** when no flight is airborne, `try_advance_time` skips to next departure without semaphores.

## Dependencies

- US100 — parent/child processes
- US101 — per-tick processing
- US102 — requires same-step updates

## Risks

| Risk | Mitigation |
|------|------------|
| Deadlock if child dies | `COMMUNICATION LOST`; `SIGKILL` |
| Child missing `sem_post` | Parent blocks — detected via `update_ready == 0` |

## Tests

Multi-flight simulation completes without hang in `run_tests.sh all`. See [TESTS.md](../TESTS.md).
