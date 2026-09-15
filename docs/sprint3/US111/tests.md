## TESTS

See [analysis.md](analysis.md) § Test coverage for planned JUnit classes.

### Automated (JUnit)

```bash
cd aisafe.base/aisafe.core && mvn test \
  -Dtest=SimulationReportParserTest,SimulationSummaryReportGeneratorTest,SimulationReportsPathResolverTest,FlightSimulationServiceTest,SimulateFlightsInAreaControllerTest
```

| Test class | Scenario |
|------------|----------|
| `SimulationReportParserTest` | Parse `validation_result`, flight rows, and violations section from US109 CSV; zero violations when section absent |
| `SimulationSummaryReportGeneratorTest` | Generated `.txt` contains ACA, interval, PASS/FAIL, flight statuses, violation details; file created at expected path |
| `SimulationReportsPathResolverTest` | Creates `reports/simulations/{areaCode}/`; filename matches `{timestamp}-summary.txt` pattern |
| `FlightSimulationServiceTest` | After mock simulator run, `SimulationResult.summaryReportPath()` points to existing file |
| `SimulateFlightsInAreaControllerTest` | FCO authorisation required; result includes summary path when simulation succeeds |

C simulator regression (existing):

```bash
cd flight_simulator && scripts/run_tests.sh all
```

---

### Unit test scenarios (service / parser / generator)

* `EnsureParserReadsViolationsFromCsv` — two-flight collision row maps to violation with step and coordinates
* `EnsureParserReturnsEmptyViolationsWhenSectionAbsent` — PASS simulation with no violation block
* `EnsureSummaryGeneratorWritesPassResult` — file contains `FINAL RESULT : PASS` and flight count
* `EnsureSummaryGeneratorWritesFailResultWithViolations` — file lists violation events with positions
* `EnsurePathResolverCreatesAreaSubdirectory` — `AREA-0` folder created under `reports/simulations/`
* `EnsureSimulationServiceReturnsSummaryPath` — integration with mocked `SimulatorGateway`
* `EnsureSimulateFlightsRequiresFlightControlOperatorAuthorization` — non-FCO rejected (existing)
* `EnsureSummaryNotGeneratedWhenSimulatorFails` — exit code != 0 → no summary file, exception propagated

---

### Manual acceptance (FCO)

```bash
cd aisafe.base
./reset-db.sh && ./run-aisafe.sh --bootstrap
./run-aisafe.sh
```

1. Login FCO: `fco1` / `password123`
2. **MainMenu** → Flight Control → **Simulate Flights in Area (US100)**
3. Select **AREA-0** (TMA Lisboa)
4. Use interval from [US100/tests.md](../US100/tests.md) (PASS window or TP085OK window)

| Scenario                                  | Expected console | Expected filesystem |
|-------------------------------------------|------------------|---------------------|
| AREA-0, PASS window (5 eligible flights)  | Formatted summary: 5 flights, statuses, PASS or FAIL, file path | `reports/simulations/AREA-0/*-summary.txt` exists |
| AREA-0, TP085OK interval (collision demo) | Summary shows FAIL, violation section with step and coordinates | Summary file lists COLLISION events |
| Invalid interval (`end < start`)          | Error before simulation | No summary file created |
| No eligible flights                       | "No eligible flights" | No summary file created |
| Login as Pilot (`pilot1`)                 | Flight Control menu unavailable or authorisation error | No summary file |

5. After simulation, close the console and verify the summary file is still readable:

```bash
cat reports/simulations/AREA-0/*-summary.txt
```

6. Confirm optional archive files (`*-report.csv`, `*-report.txt`) if implemented.

---

### Acceptance criteria traceability

| AC | Test |
|----|------|
| AC1 | `SimulationSummaryReportGeneratorTest`, manual step 5 |
| AC2 | `EnsureSummaryGeneratorWritesPassResult` / manual console output |
| AC3 | `EnsureParserReadsViolationsFromCsv`, TP085OK manual scenario |
| AC4 | `EnsureParserReturnsEmptyViolationsWhenSectionAbsent` |
| AC5 | Manual — console shows full summary + path |
| AC6 | `SimulateFlightsInAreaControllerTest`, Pilot login manual |
| AC7 | Summary file header contains ACA and interval |
| AC8 | Manual step 5 — file survives session close |
