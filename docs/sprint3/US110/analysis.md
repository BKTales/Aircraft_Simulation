# US110 — Analysis

## Current State (post US110)

| Layer | Status |
|-------|--------|
| Java `WeatherData` aggregate | Implemented (US041–043, US082) |
| DSL segment wind | Parsed in `SegmentDescriptor`; exported via `FlightPlanJsonExporter` |
| Weather snapshot export | `SimulatorWeatherSnapshotExporter` writes `FS_WEATHER_FILE` JSON |
| C simulator SHM | `sim_environment_t` in `sim_global_t` |
| C environment thread | `environment_dedicated_thread_fn` publishes wind each tick |
| C physics | Ground-track drift from segment, spatial, or area wind |

## Integration flow

1. **Java** exports `weather_snapshot.json` and sets `FS_WEATHER_FILE` before `ProcessBuilder` launch.
2. **C parent** environment thread loads the snapshot at start and writes `sim_environment_t` into SHM each tick.
3. **C child** reads SHM; segment wind from JSON overrides area wind when present.
4. **Physics** applies ground-track drift: ground velocity ≈ air velocity + wind vector.

## Wind Resolution Priority (per flight, per tick)

1. Active segment `WindDirectionDeg` / `WindSpeedMs` (if present in JSON)
2. SHM `global.environment` (from environment thread / weather snapshot)
3. Zero wind

## Time Semantics

Weather record windows use **epoch seconds** (same basis as `global_sim_time_s` / `departure_time_s` via `datetime_to_epoch_s`).
