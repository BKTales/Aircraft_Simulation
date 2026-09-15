## TESTS

See [analysis.md](analysis.md) § Test coverage for planned JUnit classes.

### Automated (JUnit)

```bash
cd aisafe.base/aisafe.core && mvn test \
  -Dtest=SimulationSummaryFileParserTest,MonthlyStatisticsCollectorTest,MonthlyStatisticsAggregatorTest,MonthlyStatisticsReportGeneratorTest,MonthlyStatisticsSnapshotTest,MonthlyReportRequestTest,MonthlyReportResultTest,SimulationSummaryRecordTest,NoMonthlySimulationDataExceptionTest,GenerateMonthlyStatisticsReportServiceTest,GenerateMonthlyStatisticsReportControllerTest,FlightControlOperatorSessionTest
```

| Test class | Scenario |
|------------|----------|
| `SimulationSummaryFileParserTest` | Parse US111 summary; blank/missing fields; invalid dates |
| `MonthlyStatisticsCollectorTest` | Filter by ACA + month; I/O errors; validation |
| `MonthlyStatisticsAggregatorTest` | Totals; empty input; pass rate |
| `MonthlyStatisticsReportGeneratorTest` | File output, ASCII chart, overwrite, I/O failure |
| `MonthlyStatisticsSnapshotTest` | Snapshot + breakdown row accessors |
| `MonthlyReportRequestTest` / `MonthlyReportResultTest` | DTO validation |
| `SimulationSummaryRecordTest` | Record accessors |
| `NoMonthlySimulationDataExceptionTest` | Exception message |
| `GenerateMonthlyStatisticsReportServiceTest` | E2E, validation, delegation |
| `GenerateMonthlyStatisticsReportControllerTest` | FCO auth, ACA scope, default constructor |
| `FlightControlOperatorSessionTest` | Session edge cases |
| `MonthlyReportsPathResolverTest` | Creates `reports/monthly/{area}/{yyyy-MM}-statistics.txt` |

---

### Unit test scenarios

| Test | Description |
|------|-------------|
| `parserReadsPassSummary` | PASS result, flight count, zero violations |
| `parserReadsFailSummaryWithViolations` | FAIL + violation count from header |
| `collectorIgnoresOtherMonths` | June summaries only when month=2026-06 |
| `collectorIgnoresOtherAreas` | AREA-0 vs AREA-4 isolation |
| `aggregatorComputesPassRate` | 2 pass, 1 fail → 66.7% |
| `generatorWritesBrandedHeader` | Contains `AISafe MONTHLY STATISTICS REPORT` |
| `generatorOverwritesSameMonth` | Second run replaces `{yyyy-MM}-statistics.txt` |
| `serviceFailsWhenNoSummaries` | `NoMonthlySimulationDataException` |
| `controllerRejectsNonFco` | Pilot / ATCC user denied |
| `controllerUsesFcoAssignedArea` | fco1 → AREA-0 |

Use fixture summaries matching [US111 design](../US111/design.md) format in `src/test/resources/us112/` or inline strings in tests.

---

### Manual acceptance (FCO)

**Prerequisite:** at least one US111 summary exists for the target month (run US100 first).

```bash
cd aisafe.base
./reset-db.sh && ./run-aisafe.sh --bootstrap
./run-aisafe.sh
```

1. Login FCO: `fco1` / `password123`
2. **MainMenu** → Flight Control → **Simulate Flights in Area (US100)** — run once to create a summary (or use existing `reports/simulations/AREA-0/`)
3. **MainMenu** → Flight Control → **Generate Monthly Statistics Report (US112)**
4. Enter current **year** and **month**

| Scenario | Expected |
|----------|----------|
| Month with summaries | Console shows statistics + path; `reports/monthly/AREA-0/{yyyy-MM}-statistics.txt` exists |
| Month without summaries | Clear error; no file created |
| Login as Pilot (`user3`) | Flight Control unavailable or authorisation error |
| Close console, reopen file | Report still readable on disk |

Verify file:

```bash
cat reports/monthly/AREA-0/2026-06-statistics.txt
```

---

### Acceptance criteria traceability

| AC | Test |
|----|------|
| AC1 | `MonthlyStatisticsReportGeneratorTest`, manual step 4 |
| AC2 | `aggregatorComputesPassRate`, `generatorWritesBrandedHeader` |
| AC3 | `serviceFailsWhenNoSummaries` |
| AC4 | `controllerUsesFcoAssignedArea` (extend with wrong-area attempt if UI allows) |
| AC5 | `controllerRejectsNonFco` |
| AC6 | Manual — console output + path |
| AC7 | Manual — file survives session close |
| AC8 | `generatorWritesBrandedHeader` |
