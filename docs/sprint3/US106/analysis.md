# US106 — Analysis

## User story

As a PO, I want function-specific dedicated threads in the parent process (safety and report), to separate responsibilities and enable concurrent processing.

## Context

Before US106, collisions and report writes ran on the **main thread** after each collect — blocking the loop and mixing detection with I/O.

US106 introduces:

- `safety_dedicated_thread_fn` — collision scan (US102)
- `report_dedicated_thread_fn` — report event writes (base for US107/109)
- `environment_dedicated_thread_fn` — added in US110

Synchronization via `pthread_mutex_t` + `pthread_cond_t` in `sim_thread_sync_t`.

## Acceptance mapping

| PO criterion | Implementation |
|--------------|----------------|
| Dedicated safety thread | `safety_dedicated_thread_fn` |
| Dedicated report thread | `report_dedicated_thread_fn` |
| Mutex/cond vars | `step_mutex`, `violation_mutex`, `done_mutex`, … |
| Start at simulation begin | `sim_dedicated_threads_start()` |
| Ordered shutdown | `sim_dedicated_threads_stop()` join |

## Dependencies

- US105 — SHM with data to scan
- US107 — completes detection/recording decoupling
- US108 — main thread collects before signaling safety

## Risks

- Deadlock between main and safety if cond vars mis-ordered — mitigated with `sim_done` broadcast on shutdown.

## Tests

[tests.md](tests.md), [TESTS.md](../../scomp/TESTS.md)
