# US107 — Tests

## Automated (C simulator)

From `flight_simulator/`:

```bash
cd src/main && make flight_simulator
cd ../..
FS_NON_INTERACTIVE=1 ./scripts/run_tests.sh collision mixed_failures all_valid
```

### collision environment

- Expect `safety_violation_events > 0` in CSV.
- Expect `COLLISION` rows and `validation_result,FAIL`.
- Stderr should show `[CRITICAL]` from safety thread and `[REPORT] Safety violation recorded` from report thread.

### all_valid environment

- Expect `safety_violation_events,0`.
- No violation notify log lines.

## Manual validation

1. Run simulator with collision environment; confirm report thread log appears **after** critical collision log and **before** simulation ends.
2. Inspect generated `report.txt` / `report.csv` under `reports/tests/collision/` — violation section populated.
3. Confirm simulation still passes step barrier (no deadlock between safety and main thread).

## Regression

- US106 thread startup/shutdown unchanged.
- Non-collision flights still recorded by main thread via `check_flight_status()`.
