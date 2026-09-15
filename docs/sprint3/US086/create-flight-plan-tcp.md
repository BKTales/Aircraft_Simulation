# US086 — CREATE_FLIGHT_PLAN TCP contract (US080)

Business rules (replace vs new flight): [US080/analysis.md](../US080/analysis.md).

---

## Helper opcodes (create wizard)

| Opcode | Constant | Request | Success body |
|--------|----------|---------|--------------|
| 43 | `LIST_IMPORT_AIRCRAFT` | empty | one registration per line |
| 47 | `LIST_CREATE_ROUTES` | `yyyy-MM-dd` | `routeName\|origin\|dest\|REGULAR\|CHARTER` per line |
| 48 | `LIST_COMPANY_PILOTS` | empty | `username\|firstName\|lastName\|email` per line |

Note: opcode 43 is shared with US121 import (same aircraft list).

---

## CREATE_FLIGHT_PLAN

Opcode: `PilotOpcodes.CREATE_FLIGHT_PLAN` (40)

### Request payload (semicolon-separated)

```
routeName;aircraftReg;pilotUsername;departure;arrival;fuelValue;fuelUnit;paxCount;paxWeightKg;cargoWeightKg;suffix;confirmReplace
```

| Field | Example | Notes |
|-------|---------|--------|
| routeName | `TP1001` | Must exist and be active for company on departure date |
| aircraftReg | `CS-TP02` | Active, same company |
| pilotUsername | `pilot1` | Active roster pilot |
| departure | `2026-06-22 10:00` | Wire format: `uuuu-MM-dd HH:mm` |
| arrival | `2026-06-22 13:00` | After departure |
| fuelValue | `13000` | Positive |
| fuelUnit | `kg` or `l` | |
| paxCount | `200` | For simulator JSON only |
| paxWeightKg | `10000` | |
| cargoWeightKg | `5000` | |
| suffix | `` or `A` | Optional — new flight on same route |
| confirmReplace | `false` or `true` | `true` after user confirms replacing an existing plan |

Codec: `PilotCreateFlightPlanPayload.encode/decode`.

Client UI: `CreateFlightPlanRemoteUI` (accepts flexible dates locally; encodes strict wire format).

### Server handler

```java
createFlightPlan.createFlightPlan(parsedRequest);
```

`PilotCommandHandler` calls `CreateFlightPlanController` (authz + session); the controller delegates to `CreateFlightPlanService` — same stack as the backoffice console. List opcodes **47** / **48** and aircraft list **43** follow the same controller → service path.

### Success response

```
OK|<designator>|DRAFT|CREATED
OK|<designator>|DRAFT|REPLACED|<previousStatus>
```

Examples:

- `OK|TP1001A|DRAFT|CREATED`
- `OK|TP1001|DRAFT|REPLACED|DRAFT`
- `OK|TP1001|DRAFT|REPLACED|SIM_REJECTED`

`<previousStatus>` is the plan status before replacement (`DRAFT`, `SIM_REJECTED`, `SIM_APPROVED`, …).

### Confirmation required

When the flight exists and the current plan is **SUBMITTED_FOR_SIMULATION** or **SIM_APPROVED**:

- Response code: `NEEDS_CONFIRMATION` (-24)
- Payload: human-readable message (designator + current status)
- Client re-sends the **same** payload with `confirmReplace=true` after user confirms

### Error response

- `BAD_REQUEST` (-22) with message from `CreateFlightPlanResult.errorMessage()`
- `UNAUTHORIZED` / `FORBIDDEN` if session invalid or wrong role

---

## Related

- Remote UI flow: [design.md](design.md) + [us086-sd.puml](us086-sd.puml)
- Local equivalent: [US080/design.md](../US080/design.md)
