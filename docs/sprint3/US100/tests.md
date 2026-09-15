## TESTS

> Central test plan (C + integration): [TESTS.md](../../scomp/TESTS.md)

See `analysis.md` § Test coverage for JUnit classes.

### Automated (JUnit)

```bash
cd aisafe.base/aisafe.core && mvn test \
  -Dtest=RouteAreaCrossingDetectorTest,FlightPlanAreaClipperTest,FlightEligibilityServiceTest,FlightSimulationServiceTest,SimulateFlightsInAreaControllerTest,SimulatorAirportJsonMapperTest
```

| Test class | Scenario |
|------------|----------|
| `RouteAreaCrossingDetectorTest` | OPO→LIS full span (wide boundary); LIS→FNC partial span (bootstrap TMA Lisboa); FNC→PDL no crossing |
| `FlightPlanAreaClipperTest` | Partial clip reduces fuel/segments; full leg returns unchanged descriptor |
| `FlightEligibilityServiceTest` | Schedule overlap; geographic eligibility; FULL vs CLIPPED mode |
| `FlightSimulationServiceTest` | US100 clipped export; US085 full export |
| `SimulateFlightsInAreaControllerTest` | Authorisation + preview |
| `SimulatorAirportJsonMapperTest` | `AreaCode` present in airport JSON |

C simulator regression (existing):

```bash
cd flight_simulator && scripts/run_tests.sh all
```

### Manual acceptance (FCO)

```bash
cd aisafe.base
./reset-db.sh && ./run-aisafe.sh --bootstrap
# Quick preview check (no C simulator required):
mvn -pl aisafe.bootstrap -q package -DskipTests
mvn -pl aisafe.bootstrap dependency:build-classpath -Dmdep.outputFile=target/cp.txt -q
java -cp "$(cat aisafe.bootstrap/target/cp.txt):aisafe.bootstrap/target/aisafe.bootstrap-1.0.0-SNAPSHOT.jar" \
  eapli.aisafe.infrastructure.bootstrapers.Us100AreaSimulationVerify
./run-aisafe.sh
```

1. Login FCO: `fco1` / `password123`
2. **MainMenu** → Flight Control → **Simulate Flights in Area (US100)**
3. Note listed areas; select **AREA-0** (TMA Lisboa)
4. Interval covering bootstrap flights (first Mon/Thu ≥ today+1 month, 10:00–12:00 — same window as `TP085OK`)

| Scenario | Expected preview | Expected run |
|----------|------------------|--------------|
| AREA-0, interval TP085OK | `TP085OK [CLIPPED]`, `TP085DSL`/`TP085SIM` if DSL valid | Plans exported ≥ 1; PASS/FAIL from CSV |
| AREA-0, interval TP100CLIPPED | `TP100CLIPPED [CLIPPED]` (LIS→FNC) | Shorter clipped JSON vs full plan |
| AREA-0, **PASS window** (1.ª terça ≥ hoje+2 meses, 13:00–17:00) | 5 voos: 3× FULL + 2× CLIPPED | Preview lista todos; sim pode PASS/FAIL conforme overlap |
| Invalid interval (`end < start`) | Error before preview | No C invocation |
| Empty interval (no overlap) | "No eligible flights" | No C invocation |

**PASS window** — isolated LIS→FAO flight (`TP100PASS` on route `TP1003`), no overlap with US085 demo flights:

```text
Start: <1.ª terça ≥ hoje+2 meses> 13:00
End:   <mesmo dia> 17:00
```

Example (bootstrap run 2026-06-08): `11-8-2026 13:00` .. `11-8-2026 17:00`:

```text
TP100FULL1 [FULL]    FAO→LIS  13:15–14:45
TP100CLIP1 [CLIPPED] OPO→LIS  13:45–15:15
TP100PASS  [FULL]    LIS→FAO  15:00–16:30
TP100FULL2 [FULL]    LIS→FAO  15:30–17:00
TP100CLIP2 [CLIPPED] LIS→FNC  16:45–18:15
```

Verify clipped export (optional): inspect temp dir logged during simulation or add debug logging — clipped legs use `EXT` arrival and fewer segments than the stored full plan.
