# US082 — Requirements

## User Story

As a **Pilot**, I want to attach weather data to a flight, so that flight validation reflects the latest weather conditions.

---

## Requirements

### Functional

1. **R1 — Attach weather reference**
   The system shall allow attaching a `WeatherData` entity reference to an existing flight (aggregate root).

2. **R2 — Preconditions**
   The target flight must exist and have a flight plan. The weather data entry must exist.

3. **R3 — Void prior test**
   When weather is attached after a simulation test, the previous test result is **void**: if the flight plan status is `SIM_APPROVED` or `SIM_REJECTED`, it is reset to `DRAFT` and the pilot must re-run US085 before execution.

4. **R4 — No change for in-progress plans**
   If the plan is already `DRAFT` or `SUBMITTED_FOR_SIMULATION`, attaching weather does not change status.

5. **R5 — Authorisation**
   Only authenticated users with role `PILOT` may execute this operation (backoffice and US086 remote TCP opcode 44).

6. **R6 — Persistence**
   The weather reference is persisted on `Flight` (`FLIGHT.WEATHER_DATA_ID`); the flight plan row is updated when status is reset.

### Non-functional

7. **R7 — Layered architecture**
   Console UI, `InsertWeatherInFlightController`, domain (`Flight#assignWeatherData`), and repositories follow the project layered style.

---

## Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given a valid flight with a plan and valid weather data, when attach succeeds, then `Flight.weatherData` is persisted. |
| AC2 | Given a plan in `SIM_APPROVED` or `SIM_REJECTED`, when weather is attached, then status becomes `DRAFT`. |
| AC3 | Given a plan in `DRAFT`, when weather is attached, then status remains `DRAFT`. |
| AC4 | Given a missing flight or weather id, when attach is attempted, then validation fails and nothing is persisted. |
| AC5 | Given a flight without a plan, when attach is attempted, then validation fails. |
| AC6 | Given a user without `PILOT` role, when attach is attempted, then authorization fails. |

---

## Related Stories

| US | Relationship |
|----|--------------|
| US043 | Weather data consulted before attach |
| US085 | Re-validation required after void (R3) |
| US086 | Remote attach via TCP opcode 44 |
| US110 | Attached weather feeds simulator snapshot export |
