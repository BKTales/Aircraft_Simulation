# US082 — Tests

## Automated Tests

- `InsertWeatherInFlightControllerTest`
  - successful weather attach and persistence
  - resets plan to `DRAFT` when status was `SIM_APPROVED`
  - resets plan to `DRAFT` when status was `SIM_REJECTED`
  - missing flight validation
  - missing weather validation
  - role-gated weather lookup (`PILOT` only)
  - constructor dependency guards
- `FlightTest`
  - `ensureAssignWeatherDataResetsPlanToDraftWhenSimApproved`
  - null weather rejected
  - error path when flight has no plan

## Execution

```bash
cd aisafe.base
mvn -q -pl aisafe.core -am test
```
