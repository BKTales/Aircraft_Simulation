# US109 — Analysis (Sprint 2 baseline)

## User story

As a PO, I want a final simulation report, to document results, violations, and each flight's status.

## Context

US109 aggregates data collected during simulation into **TXT** (human) and **CSV** (machine) reports. CSV is consumed by Java (`SimulationReportParser`) in US085, US100, and US111.

## Report content

| Section | Data |
|---------|------|
| Header | Date, flight count, global result |
| Flights | ID, airports, status, final step, coordinates |
| Violations | Pairs, type, step, separations, positions |
| CSV metrics | Aggregates, `validation_result` |

## Execution statuses

| Status | Source |
|--------|--------|
| `SUCCESS` | Route completed |
| `OUT OF FUEL` | US101 — `no_fuel` |
| `LOW ALTITUDE` | Altitude < 228 m in cruise |
| `COLLISION` | US102 |
| `COMMUNICATION LOST` | Child did not respond on step |
| `ABORTED` | Parent aborted simulation |

## Sprint 3 evolution

- Async violation recording via report thread (US107).
- Enriched CSV format for Java parsing.
- `handle_report_output()` at simulation end.

See also [Sprint 3 US109 analysis](../../sprint3/US109/analysis.md).

## Global validation rules

```
validation_result = PASS  iff  violation_count == 0 AND no critical failures
validation_result = FAIL  otherwise
```

## Tests

Assertions in `scripts/expected/*.sh`; `SimulationReportParserTest` (Java). See [TESTS.md](../TESTS.md).
