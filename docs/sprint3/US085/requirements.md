# US085 — Test/Validate Flight Plan

## User Story

As a **Pilot**, I want to **test and validate a flight plan** I created, so that I can confirm it is safe before its execution.

---

## Requirements

### Functional

1. **R1 — DSL validation**  
   The system validates the DSL stored in the flight plan (from US080 or US121) using the ANTLR parser (US120): lexical, syntactic, and semantic analysis.

2. **R2 — C test component**  
   After successful DSL validation, the system invokes the C simulator (`flight_simulator`) to evaluate **only the selected flight plan** against real operational conditions.

3. **R3 — Fuel check**  
   The simulator checks that the aircraft has sufficient fuel for each leg and enough residual fuel on arrival.

4. **R4 — Altitude check**  
   The simulator verifies that the planned altitude per segment meets minimum requirements.

5. **R5 — Result stored**  
   The test outcome (`SIM_APPROVED` / `SIM_REJECTED`) is persisted in the flight plan status. Failure details are returned to the Pilot in the validation response.

6. **R6 — Pilot authorisation**  
   Only the Pilot who owns the flight plan may execute the test.

### PO clarification

US085 validates **one flight plan only**. There are **no concurrent peer flights** and **no collision detection** as part of this user story. Multi-flight area simulation and collisions belong to **US100**.

### Non-functional

7. **R7 — No external DSL file**  
   The Pilot does not provide a DSL file in US085. The DSL already stored in the flight plan is used.

8. **R8 — Weather void**  
   If weather data is updated after a test (US082), the previous test result is void and the Pilot must re-run US085.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | The system validates the stored DSL (lexical, syntactic, semantic) before invoking the C component. |
| AC2 | The test component is implemented in C (`flight_simulator`). |
| AC3 | The simulator validates fuel sufficiency (per leg and residual at destination). |
| AC4 | The simulator validates minimum altitude per segment. |
| AC5 | DSL errors are reported with type, line, and column. |
| AC6 | If the DSL is invalid, the C simulator is **not** invoked. |
| AC7 | The test outcome (`SIM_APPROVED` / `SIM_REJECTED`) is stored in the flight plan; failure details are shown in the response. |
| AC8 | Adding weather after a test (US082) marks the result as void. |
| AC9 | Only the Pilot who owns the plan may run the test. |
| AC10 | Only the selected flight plan is simulated (no concurrent flights). |

---

## Related user stories

| US | Role |
|----|------|
| US080 / US121 | Creates the flight plan and stores the DSL |
| US082 | Attaching weather voids a previous test |
| US100 | Multi-flight area simulation (collisions, clipping) |
| US120 | DSL parser used as validation gate |
| US086 | Remote access for Pilots (opcode 45) |
