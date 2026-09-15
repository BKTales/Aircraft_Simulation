# US080 — Create a Flight Plan

## User story

As **Pilot**, I want to register a flight plan for an active company route (aircraft, schedule, fuel, assigned pilot, and load for simulation), so that the plan is stored in **DRAFT** status and can be validated later (US085).

---

## Requirements

### Functional

1. **R1 — Create draft plan**
   An authenticated Pilot can create a flight plan in **DRAFT** status for a selected company route.

2. **R2 — Route selection**
   Only **active** routes of the pilot's air transport company are selectable (US073).

3. **R3 — Flight data**
   The pilot selects **aircraft** (company fleet), **pilot** (company roster), **departure** and **arrival** datetimes, **fuel** (kg or litres), and **load** (passenger count/weight and cargo weight for simulator JSON).

4. **R4 — Load not on Flight aggregate**
   Load values are used for simulator JSON export only; `Flight.flightLoad` remains **null** on US080 create.

5. **R5 — Designator**
   Designator defaults to the route name. An optional **operational suffix** (one letter) creates a **separate flight** on the same route (e.g. `TP1001A`).

6. **R6 — Plan replacement**
   Creating again with the **same designator** replaces the embedded flight plan on that flight (one flight, one plan; no history). See R10.

7. **R7 — Pilot and aircraft rules**
   Assigned pilot must be **active**, belong to the route's company, and be **certified** for the selected aircraft model (US075). Aircraft must be **active** and owned by the route's company.

8. **R8 — Route schedule**
   Route must not be **deactivated** on the departure date; departure must match the route schedule (charter datetime or regular recurring day).

9. **R9 — Persisted payloads**
   System persists `Flight` + embedded `FlightPlan` with canonical **`dslContent`** (US120 format) and simulator **`jsonContent`**.

10. **R10 — Replacement confirmation**
    - **Silent replace** when current plan status is **DRAFT** or **SIM_REJECTED**.
    - **User confirmation** required when status is **SUBMITTED_FOR_SIMULATION** or **SIM_APPROVED**.
    - Replacement resets plan to **DRAFT** and clears attached weather.

11. **R11 — Authorization**
    Only users with role **PILOT** may execute this use case.

12. **R12 — Console entry point**
    Pilot menu: **Flights > Create flight plan (US080)** (`CreateFlightPlanAction` / `CreateFlightPlanUI`).

13. **R13 — Success feedback**
    After successful create or replace, the console shows a summary (designator, route, aircraft, pilot, schedule, fuel, load, and replacement note when applicable).

### Non-functional

14. **R14 — DSL validation timing**
    US080 **exports** DSL text from the composed descriptor; full **`FlightDslParser`** validation runs at **US085** (not re-parsed on every US080 save). Invalid composition is prevented by domain/route rules and the composer.

15. **R15 — Architectural consistency**
    Layered architecture: UI → controller (authz) → service (repositories + orchestration) → domain → repositories; controller does not access repositories directly (same pattern as `AddUserController`). Shared with US086 over TCP.

### Implementation mapping (code)

| Concern | Class / artifact |
|---------|------------------|
| Aggregate | `Flight`, `FlightPlan`, `FlightDesignator`, `FlightPlanStatus` |
| Application | `CreateFlightPlanController` (authz only), `CreateFlightPlanService` (listings + create), `CreateFlightPlanRequest`, `CreateFlightPlanResult` |
| Composition | `DirectRouteFlightPlanComposer`, `FlightPlanDslExporter`, `FlightPlanJsonExporter` |
| DSL (US120) | `FlightDslParser` (used at US085; export format aligned at create) |
| Persistence | `FlightRepository`, `RouteRepository`, `AircraftRepository`, `PilotUserRepository` |
| Console UI | `CreateFlightPlanAction`, `CreateFlightPlanUI`, `FlightPlanCreationSummary` |
| Remote (US086) | `CreateFlightPlanRemoteUI`, `PilotCommandHandler` — see [create-flight-plan-tcp.md](../US086/create-flight-plan-tcp.md) |

---

## Acceptance criteria

| ID | Criterion |
|----|-----------|
| AC1 | Given a valid Pilot session and valid inputs, when the plan is created, then `Flight` + `FlightPlan` are persisted in **DRAFT** with `dslContent` and `jsonContent`. |
| AC2 | Given a route inactive on the departure date, when creation is attempted, then it fails and nothing is persisted. |
| AC3 | Given departure not matching route schedule, when creation is attempted, then it fails. |
| AC4 | Given a pilot not certified for the aircraft model, when creation is attempted, then it fails. |
| AC5 | Given an operational suffix, when creating on the same route, then a **new** flight designator is used (e.g. `TP1001A`). |
| AC6 | Given an existing designator with plan **DRAFT** or **SIM_REJECTED**, when creating again, then the plan is **replaced silently** and status returns to **DRAFT**. |
| AC7 | Given an existing designator with plan **SIM_APPROVED** (or **SUBMITTED**), when creating without prior confirmation, then the system asks for confirmation; on **n**, nothing changes. |
| AC8 | Given user confirms replace on AC7, when creation completes, then the plan is replaced and status is **DRAFT**. |
| AC9 | Given successful create/replace, when the flow completes, then the console displays the detailed summary (`FlightPlanCreationSummary`). |
| AC10 | Given a user without PILOT role, when creation is attempted, then authorization fails. |
| AC11 | After create, `Flight.flightLoad` is **null**; load appears only in stored simulator JSON. |

---

## Out of scope (this US)

| Item | Covered by |
|------|------------|
| TCP remote UI | [US086](../US086/requirements.md) — [create-flight-plan-tcp.md](../US086/create-flight-plan-tcp.md) |
| DSL file import | [US121](../US121/requirements.md) |
| Multi-step validation / simulation | [US085](../US085/requirements.md) |

---

## Related documentation

| Document | Content |
|----------|---------|
| [analysis.md](analysis.md) | Business rules, assumptions, domain decisions |
| [design.md](design.md) | Components, flows, sequence diagram |
| [tests.md](tests.md) | Automated and manual test coverage |

## Related user stories

| US | Relationship |
|----|--------------|
| [US073](../US073/requirements.md) | Route definition and schedule |
| [US075](../US075/requirements.md) | Pilot certification and company roster |
| [US085](../US085/requirements.md) | Post-create DSL validation and simulation |
| [US086](../US086/requirements.md) | Remote create over TCP (same business rules) |
| [US120](../US120/requirements.md) | DSL grammar and parser |
| [US121](../US121/requirements.md) | Create from DSL file instead of wizard |
