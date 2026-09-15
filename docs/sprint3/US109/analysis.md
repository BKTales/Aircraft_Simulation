# US109 — Analysis (Sprint 3)

## User story

As a PO, I want thread-based final report generation, to replace the Sprint 2 pipe-only model and support concurrent recording.

## Context

Sprint 2 US109 produced the report synchronously at simulation end. In Sprint 3:

- **Report thread** (US106) records violations in near real time (US107).
- **Main thread** records flight completion (`check_flight_status`).
- `handle_report_output()` serializes everything to TXT/CSV on shutdown.

Structured CSV is the **contract** with Java (`SimulationReportParser`).

## CSV sections (contract)

1. **metrics** — aggregates including `validation_result`
2. **flights** — one row per reported flight
3. **violations** — zero or more safety events

## Java consumers

| US | CSV usage |
|----|-----------|
| US085 | PASS/FAIL + target flight status |
| US100 | Area simulation PASS/FAIL |
| US111 | Full parse → persisted summary TXT |

## PASS/FAIL rules

- `FAIL` if collision/proximity violation exists
- `FAIL` if `failure_count >= WARNING_THRESHOLD`
- US085 separately requires target flight `SUCCESS`

## Sprint 2 baseline

Historical documentation: [../../scomp/US109/analysis.md](../../scomp/US109/analysis.md)

## Tests

[tests.md](tests.md), `SimulationReportParserTest`
