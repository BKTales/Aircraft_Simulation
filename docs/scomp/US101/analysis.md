# US101 — Analysis

## User story

As a PO, I want each flight's movements captured and processed every simulation step, to obtain updated position, altitude, speed, and fuel.

## Context

US101 implements the **movement engine** inside each child process. After receiving the synchronization tick (US103/US108), the child advances physical state and publishes a `flight_update_t`.

## Physical model

| Phase | Behaviour |
|-------|-----------|
| CLIMB | Interpolation on climb profile (altitude → IAS) |
| CRUISE | Constant cruise altitude and speed |
| DESCEND | Interpolation on descent profile |

Horizontal advance: great-circle step along the active segment, bearing between waypoints.

## Fuel

- Consumption from TSFC, thrust, and flight phase.
- `no_fuel = 1` when `fuel_kg <= 0` → termination with `OUT OF FUEL`.

## Wind (US110 extension)

With wind enabled, **ground** speed differs from TAS:

- Priority: segment JSON wind > spatial lookup > SHM global > zero.

## Business rules

1. Each step advances exactly `DT_S` seconds of simulation time.
2. At segment end, advance to next segment/leg.
3. `done = 1` when all legs are complete.
4. Periodic telemetry every `TELEMETRY_LOG_INTERVAL_S` (900 simulated seconds).

## Dependencies

- US100 — child process and loop
- US103/US108 — synchronization tick
- Embedded aircraft data in JSON (thrust, wing area, profiles)

## Tests

Environment `all_valid`: flights complete with `SUCCESS`; `out_of_fuel`: fuel detection. See [TESTS.md](../TESTS.md).
