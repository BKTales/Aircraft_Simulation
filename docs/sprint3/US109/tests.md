# US109 — Tests

> Central plan: [TESTS.md](../../scomp/TESTS.md)

## Automated — C

```bash
cd flight_simulator && bash scripts/run_tests.sh all
```

Each `expected/{env}.sh` validates metrics and CSV/TXT content.

## Automated — Java

```bash
cd aisafe.base/aisafe.core && mvn test -Dtest=SimulationReportParserTest
```

| Test | Validates |
|------|-----------|
| Parse metrics | `validation_result`, counts |
| Parse flights | `execution_status` mapping |
| Parse violations | Collision with coordinates |
| Empty violations | PASS run without violations section |

## CSV assertions (collision)

```bash
grep '^validation_result,FAIL$' reports/tests/collision/report.csv
awk -F, '/^safety_violation_events,/{print $2}' reports/tests/collision/report.csv
# > 0
grep 'COLLISION' reports/tests/collision/report.csv
```

## Manual

1. Inspect `report.txt` — formatted VIOLATIONS section.
2. Confirm step vs `elapsed_s` consistency.
3. US100 manual: CSV path returned to FCO after simulation.

## Traceability

| AC | Test |
|----|------|
| AC1 | `SimulationReportParserTest` |
| AC2 | `collision.sh` |
| AC3 | `all_valid.sh` |
| AC4 | `SimulationSummaryReportGeneratorTest` (US111) |
