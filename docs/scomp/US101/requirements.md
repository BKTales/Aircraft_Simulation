# US101 — Requirements

## User story

As a PO, I want to capture and process each flight's movements, to reflect position and consumption evolution during simulation.

---

## Functional requirements

1. **R1 — Per-step update**  
   Each tick updates flight state (lat, lon, alt, speed, fuel).

2. **R2 — Flight phases**  
   Support climb, cruise, and descend per plan segments.

3. **R3 — Fuel consumption**  
   Fuel decreases proportionally to thrust and aircraft TSFC.

4. **R4 — Out-of-fuel detection**  
   When fuel ≤ 0, set `no_fuel` and stop movement.

5. **R5 — Route completion**  
   When all segments/legs are complete, set `done = 1`.

6. **R6 — Update publication**  
   Each step produces a `flight_update_t` available to the parent (pipe/SHM).

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Valid flights in `all_valid` end with `execution_status = SUCCESS`. |
| AC2 | Insufficient fuel plan ends with `OUT OF FUEL`. |
| AC3 | Final position differs from initial (effective movement). |
| AC4 | `fuel_kg` decreases monotonically during cruise. |
| AC5 | With wind (US110), trajectory differs from no-wind baseline. |

---

## Tests

[TESTS.md](../TESTS.md) — environments `all_valid`, `out_of_fuel`, US110 scripts.
