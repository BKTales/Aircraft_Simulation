# US080 — Analysis

## Business goal

Allow a **Pilot** to register a flight plan for an active company route: aircraft, departure/arrival schedule, fuel, assigned pilot, and load data for the simulator. The plan is created in **DRAFT** status and is intended for later validation (US085) and area simulation (US100).

---

## Scope (US080 only)

- Local backoffice console (`./run-aisafe.sh`), Pilot menu **Flights > Create flight plan**.
- Select an **active route** operated by the pilot's air transport company (US073).
- Select **aircraft**, **pilot** (company roster), **departure/arrival** datetimes, **fuel** (kg or litres), and **load** (passengers/cargo for simulator JSON).
- Build a **single-leg** plan from route endpoints via `DirectRouteFlightPlanComposer`.
- Export Core Flight DSL text and simulator JSON; persist `Flight` + embedded `FlightPlan`.
- **Designator** from route name; optional **operational suffix** for a new flight on the same route; **same designator** replaces the existing plan.

## Out of scope

| Item | US |
|------|-----|
| TCP remote UI | [US086](../US086/requirements.md) |
| DSL file import | [US121](../US121/requirements.md) |
| Validation / simulation | [US085](../US085/requirements.md) |
| Route definition / deactivation | [US073](../US073/requirements.md) / [US074](../US074/requirements.md) |
| Pilot roster management | [US075](../US075/requirements.md) |

---

## Assumptions

1. **Designator** is derived from the selected route name (`FlightDesignator.fromRoute`), with an optional single-letter operational suffix for a **separate flight** on the same route.
2. **One flight, one plan** — JPA `@OneToOne`; re-create on the same designator **replaces** the embedded plan (no version history).
3. **One implicit leg** connects route origin and destination; climb/cruise/descend segments are **synthetic** (fixed cruise altitude, default wind) — not user-edited waypoints.
4. **Load** (passengers, cargo) is captured for simulator JSON at plan **root** (`Load` block); it is **not** persisted on the `Flight` aggregate (`flightLoad` remains `null`).
5. **Fuel** is stored on `FlightPlan` via `FuelLoad`; leg-level semantics (positive fuel, time ordering) are ensured by the composer and checked by **US120/US085**, not duplicated in ad-hoc Java checks.
6. Authenticated user must have role **PILOT**; company context comes from the logged-in pilot profile.

---

## Business rules

1. Only users with role **PILOT** may create a flight plan (`CreateFlightPlanController` enforces authz; service performs business operations).
2. Selected route must exist and be **active** on the departure date (`Route.isActiveOn`, `assertUsableForFlight`).
3. Departure must **match the route schedule** for that date (`assertDepartureMatchesSchedule` — charter datetime or regular recurring day).
4. Assigned pilot must belong to the **same air transport company** as the route.
5. Assigned pilot must be **active** and **certified** for the selected aircraft model (US075).
6. Selected aircraft must be **active** and owned by the route's company (`assertAssignableToNewFlight`).
7. Arrival must be **after** departure (enforced in composed DSL semantics).
8. Each **flight designator** identifies at most one `Flight`; creating again with the same designator **replaces** the flight plan.
9. Replacement is **silent** when current plan status is **DRAFT** or **SIM_REJECTED**.
10. Replacement requires **user confirmation** when status is **SUBMITTED_FOR_SIMULATION** or **SIM_APPROVED**.
11. New and replacement plans start in status **DRAFT**; attached weather is cleared on replace.
12. Exported DSL conforms to US120 grammar; **parse validation** is executed at US085 before simulation.

---

## Domain decisions

| Decision | Detail |
|----------|--------|
| New flight | `Flight.createDraftForRoute` — validates company, certification, schedule, aircraft |
| Replace plan | `Flight.replaceFlightPlan` — same flight, new DRAFT plan; clears weather |
| Silent replace | `FlightPlanStatus.allowsSilentReplacement()` → `DRAFT` or `SIM_REJECTED` |
| In-place plan update | `FlightPlan.replaceDraftContent` — avoids JPA orphan issues when replacing `@OneToOne` |
| Route reference | `Flight.routeNameKey` → column `routename` (string); no JPA FK to `Route` |
| Stored payloads | `FlightPlan.dslContent` (canonical DSL) + `FlightPlan.jsonContent` (simulator JSON) |
| Composition | `DirectRouteFlightPlanComposer` → descriptor + JSON; `FlightPlanDslExporter` → DSL text |
| JSON load shape | Root-level `Load` with separate passenger/cargo weights (simulator parser #192) |

---

## Gap analysis (requirements vs implementation)

| Area | Requirement | Implementation |
|------|-------------|----------------|
| Register plan (aircraft, schedule, fuel, pilot) | Required | Done — local console + US086 TCP |
| Status **DRAFT** on create/replace | Required | `FlightPlanStatus.DRAFT` |
| Multi-step validation | US085 | Not part of US080 create flow |
| Persist DSL + JSON | Required | `@Lob` fields on `FlightPlan` |
| Plan replacement rules | R10 | `CreateFlightPlanService` + UI confirmation |
| Success summary | R13 | `FlightPlanCreationSummary` |
| Remote access | US086 | Same controller/service over TCP |

No open gaps for US080 local create; remote parity documented under US086.

---

## Implementation status

| Layer | Status | Notes |
|-------|--------|-------|
| Domain | Done | `Flight`, `FlightPlan`, replacement, `allowsSilentReplacement` |
| Application | Done | Thin `CreateFlightPlanController`; `CreateFlightPlanService` owns repositories and listings |
| Composition / export | Done | Composer, DSL/JSON exporters |
| Console UI | Done | Wizard + detailed summary |
| Remote TCP | Done | US086 — same application layer |
| Unit tests | Done | See [tests.md](tests.md) |

---

## Traceability

| Related US | Relationship |
|------------|--------------|
| [US073](../US073/requirements.md) | Routes used as flight-plan basis |
| [US075](../US075/requirements.md) | Pilot roster and certification |
| [US085](../US085/requirements.md) | Post-create validation and simulation |
| [US086](../US086/requirements.md) | TCP remote create |
| [US120](../US120/requirements.md) | DSL grammar and parser |
| [US121](../US121/requirements.md) | Alternative create via DSL file |
