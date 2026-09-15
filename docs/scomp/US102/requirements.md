# US102 — Requirements

## User story

As a PO, I want safety violation detection between flights, to identify collision or dangerous proximity situations.

---

## Functional requirements

1. **R1 — Pair comparison**  
   After each step, compare all pairs of active flights with valid updates.

2. **R2 — Horizontal distance**  
   Use haversine formula between both flights' lat/lon.

3. **R3 — Vertical separation**  
   Compare absolute altitude difference in metres.

4. **R4 — Configurable thresholds**  
   `CLOSE_PLANE_X` and `CLOSE_PLANE_Y` in `flight_simulator.h`.

5. **R5 — Child notification**  
   Send `SIGUSR1` to involved processes.

6. **R6 — Recording**  
   Log event in report with step, IDs, positions, and separations.

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | `collision` environment produces `validation_result,FAIL`. |
| AC2 | CSV contains `safety_violation_events > 0`. |
| AC3 | Both involved flights have status `COLLISION`. |
| AC4 | `all_valid` environment produces zero violations. |
| AC5 | Children receive signal and exit without hang. |

---

## Tests

[TESTS.md](../TESTS.md) — `collision.sh`, [US107 tests](../../sprint3/US107/tests.md).
