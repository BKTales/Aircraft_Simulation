# US108 — Tests

> Central plan: [TESTS.md](../../scomp/TESTS.md)

## Automated

```bash
cd flight_simulator && FS_NON_INTERACTIVE=1 FS_NO_WALL_SLEEP=1 bash scripts/run_tests.sh all
```

Any environment that completes validates semaphore + SHM protocol.

## Specific scenarios

| Scenario | How to validate |
|----------|-----------------|
| Multi-flight lock-step | `all_valid` with ≥2 plans; all SUCCESS |
| Staggered departures | Plans with different `departure_time`; sim ends |
| Barrier failure | Manually `kill -9` a child → `COMMUNICATION LOST` in CSV |
| Idle clock jump | Plan with future departure; sim advances without hang |

## Manual — staggered departures

1. Create two JSON with `departure_time` 300 s apart.
2. Run simulator; first flight idle until clock reaches departure.
3. Confirm steps increment in parallel once both airborne.

## Regression

- Hang = US108 failure (critical regression)
- US107 safety scan assumes complete collect — order: publish → collect → safety

## Performance

With `FS_NO_WALL_SLEEP=1`, `all_valid` should finish in seconds, not minutes.
