# US080 — Design

Local console implementation for creating or replacing a draft flight plan. Business rules: [analysis.md](analysis.md).

---

## Architectural approach

Layered architecture consistent with the project:

- **UI** — `CreateFlightPlanUI` wizard and success summary
- **Application** — `CreateFlightPlanController` (authz + delegation), `CreateFlightPlanService` (repository access + orchestration)
- **Domain** — `Flight`, `FlightPlan`, route/aircraft/pilot invariants
- **Infrastructure** — repositories, DSL/JSON exporters

Remote TCP reuses the same application layer ([US086](../US086/create-flight-plan-tcp.md)).

---

## Sequence diagram

Simplified happy path and replacement branches (full wizard steps omitted):

See [us080-sd.puml](us080-sd.puml).

```text
Pilot → UI → Controller (authz) → Service → Repositories (list wizard data)
                              ↓
                    Controller → Service → Composer (descriptor + DSL + JSON)
                              ↓
                    findByDesignator?
                    ├─ new → createDraftForRoute → save
                    └─ exists → silent replace | confirm → replaceFlightPlan → save
                              ↓
                    UI → FlightPlanCreationSummary
```

---

## Main flow

1. Pilot opens **Flights > Create flight plan (US080)**.
2. UI collects departure/arrival datetimes; controller enforces **PILOT** role and delegates listing to the service (`listSelectableRoutes`, `listCompanyActiveAircraftRegistrations`, `listCompanyPilots`) using the session pilot's company.
3. Pilot selects route, **aircraft**, **pilot**, **fuel**, **load**, and optional **operational suffix**.
4. `CreateFlightPlanController.createFlightPlan` enforces authz and delegates to `CreateFlightPlanService` (same pattern as `AddUserController` → `AddUserService`).
5. Service resolves route, pilot, aircraft, and aircraft model/engine; builds **designator** (`FlightDesignator.fromRoute`).
6. `DirectRouteFlightPlanComposer.compose` builds one leg (origin → destination), **simulator JSON** (root `Load`, embedded aircraft/airports), and a **descriptor**.
7. `FlightPlanDslExporter.toDsl` produces canonical **dslContent**; `FlightPlan.forFlight` wraps DSL + JSON in **DRAFT** status.
8. If designator **exists**:
   - **DRAFT / SIM_REJECTED** → `Flight.replaceFlightPlan` (silent).
   - **SUBMITTED / SIM_APPROVED** → return `needsConfirmation`; UI re-submits with `confirmReplace=true` on **y**.
9. If designator **new** → `Flight.createDraftForRoute` + save.
10. UI prints **`FlightPlanCreationSummary`** (schedule, fuel, load, replacement note).

---

## Error flows

| Condition | Result |
|-----------|--------|
| Route not found / inactive on date | Failure message; no save |
| Departure does not match route schedule | Failure |
| Pilot not in company / not certified / inactive | Failure |
| Aircraft not found / inactive / wrong company | Failure |
| Arrival not after departure | Failure (domain/composer) |
| Replace on SIM_APPROVED without confirmation | `needsConfirmation`; UI prompts |
| User declines confirmation | Cancelled; no change |
| Unauthorized (non-Pilot) | Authorization error in controller |

DSL **syntax/semantic** errors are not expected on the compose path; invalid stored DSL is caught at **US085** (`FlightDslParser`).

---

## Components

| Component | Module | Role |
|-----------|--------|------|
| `CreateFlightPlanAction` | `aisafe.app.backoffice.console` | Menu entry (Pilot) |
| `CreateFlightPlanUI` | `aisafe.app.backoffice.console` | Interactive wizard; confirmation loop |
| `FlightPlanCreationSummary` | `aisafe.app.backoffice.console` | Post-create summary output |
| `CreateFlightPlanController` | `aisafe.core` | Authz (PILOT), session username, delegate to service only (no repositories) |
| `CreateFlightPlanService` | `aisafe.core` | Repository access, wizard listings, compose, create vs replace, persist |
| `CreateFlightPlanRequest` / `CreateFlightPlanResult` | `aisafe.core` | Input DTO; outcomes: success, replaced, needsConfirmation, failure |
| `DirectRouteFlightPlanComposer` | `aisafe.core` | Single-leg descriptor + JSON (synthetic segments) |
| `FlightPlanDslExporter` | `aisafe.core` … `dsl/api` | Descriptor → DSL text |
| `FlightPlanJsonExporter` | `aisafe.core` … `dsl/api` | Descriptor → self-contained simulator JSON |
| `FlightDslParser` | `aisafe.core` … `dsl/api` | US120 — used at US085, not on US080 save |
| `Flight` / `FlightPlan` | `aisafe.core` … `domain` | Aggregate + embedded plan entity |

---

## Validation split

| Concern | Layer | Mechanism |
|---------|-------|-----------|
| Role PILOT | Controller | Authorization service (no repository access) |
| Active routes / aircraft / pilots | Service | `RouteRepository`, `AircraftRepository`, `PilotUserRepository` |
| Route active / schedule | Domain | `Route.assertUsableForFlight`, `assertDepartureMatchesSchedule` |
| Company alignment | Domain | `Flight.createDraftForRoute` / `replaceFlightPlan` |
| Pilot certification | Domain | `PilotUser.isCertifiedFor` |
| Aircraft assignable | Domain | `Aircraft.assertAssignableToNewFlight` |
| Designator collision | Service | `findByDesignator` → replace or confirm |
| Leg times, fuel, segments | US120 / US085 | `FlightDslParser` at validation time |

Plan-level time/fuel rules are **not** duplicated in the composer; the exported DSL is structurally valid by construction.

---

## Persistence

| Field | US080 behaviour |
|-------|-----------------|
| `Flight.routeNameKey` | Route name string (e.g. `TP1001`); no JPA FK to `Route` |
| `Flight.flightLoad` | `null` — load only in JSON |
| `FlightPlan.dslContent` / `jsonContent` | `@Lob`; set via `FlightPlan.forFlight` or `replaceDraftContent` |
| `FlightPlan.status` | `DRAFT` after create or replace |
| `Flight.weatherData` | Cleared on replace |

---

## Menu

`LocalCollaboratorMenuActions.createFlightPlan()` → `CreateFlightPlanAction` (Pilot menu in `./run-aisafe.sh`).

---

## Remote access

TCP transport, payload format, and manual tests: [US086/create-flight-plan-tcp.md](../US086/create-flight-plan-tcp.md). Server handler calls the same `CreateFlightPlanController` → `CreateFlightPlanService` stack as this design.
