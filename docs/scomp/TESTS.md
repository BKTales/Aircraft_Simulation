# Flight Simulator — Test Strategy and Plan

Central test document for the **C** component (`flight_simulator`) and its Java integration. Each user story keeps a local `tests.md` with specific scenarios; this file consolidates strategy, commands, environments, and traceability.

> **Engineering:** [README.md](README.md)

---

## 1. Test pyramid

```
                    ┌─────────────────────┐
                    │  Manual acceptance  │  FCO/Pilot via console (US100, US085)
                    └──────────┬──────────┘
               ┌───────────────┴───────────────┐
               │   Java ↔ C integration        │  FlightSimulationServiceTest, ValidateFlightPlanServiceTest
               └───────────────┬───────────────┘
          ┌────────────────────┴────────────────────┐
          │  C regression (environments + assertions) │  scripts/run_tests.sh
          └────────────────────┬────────────────────┘
     ┌─────────────────────────┴─────────────────────────┐
     │  Isolated C unit tests                               │  weather_config_test
     └─────────────────────────────────────────────────────┘
```

| Level | Tool | Location |
|-------|------|----------|
| C unit | `make weather_config_test` | `src/main/tests/weather_config_test.c` |
| C regression | `scripts/run_tests.sh` | `scripts/expected/*.sh` |
| Wind US110 | `test_us110_*.sh` | `scripts/` |
| Java domain | JUnit 5 + Mockito | `aisafe.core/src/test/java/` |
| CI | GitHub Actions | C build + `mvn verify` |

---

## 2. Quick run

```bash
# 1. Build simulator
cd flight_simulator/src/main && make flight_simulator

# 2. Full C regression
cd flight_simulator && bash scripts/run_tests.sh all

# 3. Weather unit test (US110)
cd flight_simulator/src/main && make weather_config_test && ./weather_config_test

# 4. Wind (US110)
cd flight_simulator
bash scripts/test_us110_wind.sh
bash scripts/test_us110_spatial_wind.sh

# 5. Java — simulation integration
cd aisafe.base/aisafe.core && mvn test \
  -Dtest=FlightSimulationServiceTest,ValidateFlightPlanServiceTest,SimulationReportParserTest
```

---

## 3. C regression framework (`run_tests.sh`)

### 3.1 Behaviour

1. Discovers environments under `src/data/environments/*/`
2. Runs `flight_simulator` with `FS_FLIGHT_PLANS_DIR`, `FS_REPORTS_DIR`, `FS_NON_INTERACTIVE=1`, `FS_NO_WALL_SLEEP=1`
3. Checks `report.txt` and `report.csv` exist
4. Runs `scripts/expected/{env}.sh` when present

### 3.2 Environments

| Environment | Plans | Goal | US covered |
|-------------|-------|------|------------|
| `all_valid` | Valid non-conflicting flights | PASS, zero violations | US101, US103, US108, US109 |
| `collision` | Converging trajectories | FAIL, collision detected | US102, US107 |
| `out_of_fuel` | Insufficient fuel | FAIL, `OUT OF FUEL` | US101, US085 |
| `mixed_failures` | Collision + fuel + wind | Composite FAIL | US102, US110 |

### 3.3 Assertions (`scripts/expected/`)

| Script | Checks |
|--------|--------|
| `all_valid.sh` | `validation_result,PASS`; zero violations |
| `collision.sh` | `validation_result,FAIL`; `safety_violation_events > 0`; `COLLISION` row |
| `out_of_fuel.sh` | `OUT OF FUEL` or `OUT_OF_FUEL` in CSV |
| `mixed_failures.sh` | FAIL with multiple failure types |

### 3.4 Test plan format

JSON under `environments/` must be **self-contained** (embedded aircraft + airports), matching Java export. Regenerate fixtures:

```bash
cd flight_simulator && python3 scripts/embed_flight_plan_data.py
```

---

## 4. Traceability by user story

### Sprint 2 (C engine — reference)

| US | Automated | Manual |
|----|-----------|--------|
| **US100** | `run_tests.sh all` | N forked children visible in logs |
| **US101** | `all_valid` — positions advance; fuel decreases | Telemetry with verbose logging |
| **US102** | `collision.sh` | `[CRITICAL]` on stderr |
| **US103** | All environments complete without hang | Implicit timeout — sim ends |
| **US109** | TXT/CSV assertions | Open `reports/tests/{env}/report.txt` |

### Sprint 3 (SHM + threads)

| US | Automated | Manual |
|----|-----------|--------|
| **US105** | `run_tests.sh` — SHM/semaphores work | No orphaned `/dev/shm/aisafe_*` |
| **US106** | Sim completes with 3 threads | No deadlock in `mixed_failures` |
| **US107** | `collision` — `[REPORT] Safety violation recorded` | Order: CRITICAL → REPORT |
| **US108** | Multi-flight sync in `all_valid` | Same step for all active flights |
| **US109** | CSV sections parsed by Java | `SimulationReportParserTest` |
| **US110** | `weather_config_test`; `test_us110_*.sh` | Trajectory differs with/without wind |

### Java integration

| US | Main JUnit classes | Manual |
|----|-------------------|--------|
| **US085** | `ValidateFlightPlanServiceTest` (32), `FlightSimulationServiceTest` | Pilot `TP085OK` / `TP085SIM` |
| **US100** | `FlightEligibilityServiceTest`, `RouteAreaCrossingDetectorTest`, `SimulateFlightsInAreaControllerTest` | FCO AREA-0, FULL/CLIPPED preview |
| **US111** | `SimulationSummaryReportGeneratorTest`, `SimulationReportParserTest` | File under `reports/simulations/...` |

Per-US detail: see `tests.md` in each folder (`docs/scomp/USxxx/` or `docs/sprint3/USxxx/`).

---

## 5. Simulator PASS/FAIL criteria

CSV field `validation_result` (US109) is the **source of truth** for Java:

| Result | Typical conditions |
|--------|-------------------|
| `PASS` | All flights `SUCCESS`; zero safety events |
| `FAIL` | Collision, fuel, low altitude, or `failure_count >= 3` |

US085 additionally requires the **target flight** to have `execution_status = SUCCESS` even when aggregate is PASS.

---

## 6. Environment variables in tests

| Variable | CI value | Effect |
|----------|----------|--------|
| `FS_NON_INTERACTIVE` | `1` | No save/view prompts |
| `FS_NO_WALL_SLEEP` | `1` | Instant steps |
| `FS_FLIGHT_PLANS_DIR` | environment path | Isolated plans |
| `FS_REPORTS_DIR` | `reports/tests/{env}/` | Isolated output |
| `FS_WEATHER_FILE` | JSON path | US110 — wind |
| `FS_TARGET_FLIGHT_ID` | designator hash | US085 — stop after target lands |

---

## 7. Relevant Java test packages

```bash
cd aisafe.base/aisafe.core

# US085 — pilot validation
mvn test -Dtest=ValidateFlightPlanServiceTest,ValidateFlightPlanControllerTest

# US100 — area simulation
mvn test -Dtest=FlightEligibilityServiceTest,FlightPlanAreaClipperTest,\
RouteAreaCrossingDetectorTest,SimulateFlightsInAreaControllerTest

# US111 — summary report
mvn test -Dtest=SimulationReportParserTest,SimulationSummaryReportGeneratorTest,\
SimulationReportsPathResolverTest

# US110 — weather snapshot
mvn test -Dtest=SimulatorWeatherSnapshotExporterTest
```

---

## 8. Manual acceptance scripts

### 8.1 FCO — US100 + US111

```bash
cd aisafe.base && ./reset-db.sh && ./run-aisafe.sh --bootstrap && ./run-aisafe.sh
# Login: fco1 / password123
# Menu → Flight Control → Simulate Flights in Area
# AREA-0, PASS window (see US100/tests.md)
cat ../reports/simulations/AREA-0/*-summary.txt
```

### 8.2 Pilot — US085

```bash
# Login: pilot1 / password123
# Menu → Validate Flight Plan
# TP085OK → SIM_APPROVED
# TP085SIM → SIM_REJECTED (OUT_OF_FUEL)
# TP085DSL → DSL errors; simulator not invoked
```

### 8.3 Isolated C collision

```bash
cd flight_simulator
FS_NON_INTERACTIVE=1 FS_NO_WALL_SLEEP=1 \
  FS_FLIGHT_PLANS_DIR=src/data/environments/collision/flight_plans \
  FS_REPORTS_DIR=/tmp/sim-collision \
  src/main/flight_simulator
grep validation_result /tmp/sim-collision/report.csv
# Expected: FAIL
```

---

## 9. CI and quality

- **NFR06:** GitHub Actions builds `flight_simulator` and runs `mvn verify` on every push.
- C test reports under `flight_simulator/reports/tests/` (local runs; reference fixtures may be committed).
- JaCoCo ≥ 90% applies to **Java** new/changed code, not C.

---

## 10. Troubleshooting

| Symptom | Likely cause | Action |
|---------|--------------|--------|
| Simulator hang | US108 semaphore deadlock | Check live children; review logs |
| `NO REPORT` | Crash in `init.c` / invalid JSON | Check stderr `[INIT]`; validate self-contained JSON |
| `COMMUNICATION LOST` | Child died without writing slot | Run child under `gdb` |
| Java PASS but US085 REJECTED | Target flight not SUCCESS | Inspect flights section in CSV |
| Same trajectory with wind | Missing `FS_WEATHER_FILE` | Verify Java export; `FS_VERBOSE_LOAD=1` |
| Orphan `/dev/shm/aisafe_*` | Crash before cleanup | `rm /dev/shm/aisafe_*` (Linux) |

---

## 11. References

- [Engineering README](README.md)
- [shm-sync-overview](../sprint3/SCOMP/shm-sync-overview.md)
- Scripts: `flight_simulator/scripts/run_tests.sh`, `expected/*.sh`, `test_us110_*.sh`
