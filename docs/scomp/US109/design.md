# US109 — Design (Sprint 2 baseline)

## Main module

[`flight_report.c`](../../../flight_simulator/src/main/utils/flight_report.c)

| Function | Purpose |
|----------|---------|
| `simulation_report_init` | Allocate structures |
| `simulation_report_record_flight` | Record flight (mutex in Sprint 3) |
| `simulation_report_record_violation` | Record violation |
| `simulation_report_write_files` | Write TXT + CSV |
| `simulation_report_print_to_stream` | stdout output |

## Internal structure

```c
typedef struct simulation_report {
    report_flight_record_t *flights;
    report_violation_record_t *violations;
    size_t flight_count, violation_count;
    pthread_mutex_t mutex;   /* Sprint 3 — US107 */
    int dt_s;
} simulation_report_t;
```

## CSV format

```csv
metric,value
flights_reported,3
flights_success,2
safety_violation_events,1
validation_result,FAIL

flight_id,departure,arrival,execution_status,step,...
789013,LPPT,EGLL,SUCCESS,42,...

flight_id_a,flight_id_b,violation_type,step,elapsed_s,separation_m,alt_sep_m,...
789013,456789,COLLISION,15,15,1200.5,50.0,...
```

## TXT format

ASCII header with `====` separators, flight and violation sections, line `FINAL VALIDATION RESULT : PASS|FAIL`.

## Output paths

[`reports_path.c`](../../../flight_simulator/src/main/utils/reports_path.c) + `FS_REPORTS_DIR`.

In tests: `reports/tests/{environment}/report.csv`.

## Java integration

`SimulationReportParser.parse(csvPath, areaCode)` → `FlightSimulationReport`.

Detailed mapping: [US111 design](../../sprint3/US111/design.md).

## Sprint 3 extensions

See [../../sprint3/US109/design.md](../../sprint3/US109/design.md).
