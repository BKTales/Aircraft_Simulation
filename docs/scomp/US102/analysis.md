# US102 — Analysis

## User story

As a PO, I want safety violations detected when two flights get too close, to simulate proximity alerts and collisions.

## Context

Detection runs in the **parent process** after each simulation step, when all positions are updated (US103/US108). Compares active flight pairs using haversine distance and vertical separation.

## Proximity criteria

Defined in [`flight_simulator.h`](../../../flight_simulator/src/main/flight_simulator.h):

| Dimension | Threshold | Constant |
|-----------|-----------|----------|
| Horizontal | < 5000 m | `CLOSE_PLANE_X` |
| Vertical | < 300 m | `CLOSE_PLANE_Y` |

Both conditions must hold simultaneously.

## Actions on violation

1. Log `[CRITICAL]` with IDs and distances.
2. `kill(pid, SIGUSR1)` on both children.
3. Mark flights inactive; increment `failure_count`.
4. Record violation (US107/109 — report thread in Sprint 3).

## Sprint 3 evolution

- **US106:** detection moves to `safety_dedicated_thread_fn`.
- **US107:** async recording via condition variable to `report_dedicated_thread_fn`.

Geometric logic (`check_collisions_detect`) is unchanged.

## Out of scope

- US085 (pilot validation) does **not** use collision detection — only fuel/altitude.
- Real regulatory separation (NM) — project uses didactic values.

## Tests

Environment `collision` + `collision.sh` assertion. See [TESTS.md](../TESTS.md).
