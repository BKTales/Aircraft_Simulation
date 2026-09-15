# US110 — Tests

## C — Weather loader

| Case | Expected |
|------|----------|
| Valid JSON file | Records loaded; lookup returns wind for matching epoch window |
| Missing `FS_WEATHER_FILE` | Empty config; lookup returns invalid / zero wind |
| Time outside all windows | `valid = 0` |
| Malformed JSON | Load fails gracefully; simulation runs without area wind |

Run: `make -C flight_simulator/src/main weather_config_test && ./weather_config_test`

## C — Integration

Automated scripts (recommended):

```bash
cd flight_simulator
chmod +x scripts/test_us110_wind.sh scripts/test_us110_spatial_wind.sh
./scripts/test_us110_wind.sh          # wind alters trajectory
./scripts/test_us110_spatial_wind.sh # bounds + crossing two wind boxes
```

Manual steps:

1. Run a self-contained flight plan without weather — baseline `report.csv`.
2. Run with `FS_WEATHER_FILE` pointing to `src/data/weather/weather_constant_west.json` — final lat/lon differ.
3. Run with segment wind in flight-plan JSON — trajectory reflects segment override.

Note: `environments/all_valid/` uses legacy JSON without embedded airports; use `mixed_failures/flight_plans/flight_plan_ok.json` instead.

## C — Environment thread

With `FS_VERBOSE_LOAD=1`, confirm environment SHM `valid == 1` before each publish when weather file is present.

## Java — Snapshot exporter

`SimulatorWeatherSnapshotExporterTest`:
- Given `WeatherData` with wind 270° / 15 kn bulk value → JSON contains `windSpeedMs` in m/s and correct bounds.

## Java — Orchestration

`FlightSimulationServiceTest`:
- When `WeatherDataRepository` returns records, `FS_WEATHER_FILE` is set and file exists in temp dir.
- When repository is null or returns empty list and no flight weather, `FS_WEATHER_FILE` still written (empty records array) or omitted per design.

## Manual

1. Import `data_weather/weather_bulk_01_lisboa_morning.csv` (US042).
2. Attach weather to a flight (US082).
3. Run area simulation (US100) and compare trajectories with/without weather data.
