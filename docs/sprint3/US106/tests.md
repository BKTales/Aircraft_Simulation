# US106 — Tests

> Central plan: [TESTS.md](../../scomp/TESTS.md)

## Automated

```bash
cd flight_simulator && FS_NON_INTERACTIVE=1 bash scripts/run_tests.sh collision all_valid mixed_failures
```

| Environment | Validates US106 |
|-------------|-----------------|
| `collision` | Safety + report threads active; no deadlock |
| `all_valid` | Threads start and stop; PASS |
| `mixed_failures` | Multi-failure stress; correct join |

## Log markers

| Log | Thread |
|-----|--------|
| `[CRITICAL]` | Safety |
| `[REPORT] Safety violation recorded` | Report |

## Manual

1. Run `collision`; confirm CRITICAL → REPORT order on stderr.
2. Simulation finishes in < 30 s with `FS_NO_WALL_SLEEP=1`.
3. Process exit code 0 or 1, never hang (implicit `pthread_join`).

## Regression

- US107 tests extend US106 — violation queue + cond var.
- US110 adds environment thread — third join on shutdown.
