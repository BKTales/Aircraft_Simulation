# US110 — Requirements

## User Story

As a PO, I want the simulation to incorporate environmental factors such as wind into the simulation, so that the flight paths become more realistic and adapt to dynamic conditions.

## Acceptance Criteria

- The parent process spawns an additional **environment** thread at simulation start.
- This thread loads environmental configuration (wind speed/direction) from a **weather service**.
- Environment data is written into the shared memory segment at each time step.

## Functional Requirements

- Wind direction is expressed in degrees relative to North (0–359), meteorological convention (direction wind comes from).
- Wind speed is expressed in m/s at the Java→C boundary.
- Segment-level wind in flight-plan JSON overrides area weather from the snapshot file.
- Simulations without `FS_WEATHER_FILE` continue to run (zero area wind; segment wind still applies).

## Related Stories

- US041–US043 — Weather data registration and consultation
- US082 — Attach weather data to flight
- US105 — Shared-memory simulation architecture
- US100 — Area simulation orchestration (Java launch)
