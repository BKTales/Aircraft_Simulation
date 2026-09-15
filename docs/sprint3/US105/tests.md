# US105 — Tests

> Central test plan: [TESTS.md](../../scomp/TESTS.md)

## Automated

```bash
cd flight_simulator/src/main && make flight_simulator
cd ../.. && FS_NON_INTERACTIVE=1 bash scripts/run_tests.sh all
```

US105 is infrastructure — validated indirectly: if simulation completes and produces a report, SHM + semaphores work.

## Scenarios

| Scenario | Environment | Criterion |
|----------|-------------|-----------|
| Multi-slot SHM | `all_valid` (≥2 plans) | PASS; all flights reported |
| Semaphore barrier | `all_valid` | No hang; steps increment |
| Cleanup | Any | No obvious orphaned `/dev/shm/aisafe_*` after normal exit |

## Manual

1. Run simulator with 2+ plans; stderr must not show `[flight N] IPC not initialized`.
2. During run: `ls /dev/shm | grep aisafe` — parent PID region should exist.
3. After exit: region removed (or document cleanup if crashed).

## Regression

- US108 step protocol depends on US105 — US108 failures manifest as hang or `COMMUNICATION LOST`.
