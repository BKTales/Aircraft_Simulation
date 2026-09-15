# US121 — Analysis

## Client Clarification / Product Owner Session

* Sprint 2 **US081** delivered file selection, DSL validation, and optional JSON export to `flight_simulator/` — **no database persistence**.
* Sprint 3 **US121** completes the story: valid plans become **`Flight` entities** usable by US100 (area simulation) and US085 (pilot validation).
* The Pilot performs import (not backoffice admin); aircraft is chosen from aircraft already registered in AISafe.
* JSON in `FlightPlan.jsonContent` is the **in-memory translation** of validated DSL, not a separate user-facing export step.

## Current State (Gap Analysis)

| Area | Sprint 2 (US081) | Target (US121) |
|------|------------------|----------------|
| Parse / validate | `ImportFlightPlanFromFileUI` + `FlightDslParser` | Same parser (US120) |
| Persist `Flight` | No | `FlightRepository.save` |
| `dslContent` on plan | No field | New `@Lob` on `FlightPlan` |
| `jsonContent` on plan | Optional file write only | Set via `FlightPlanJsonExporter` at import |
| Pilot / route / aircraft | Not set | Session pilot, derived route, UI aircraft pick |
| Menu label | "US081" | "US121" (or combined label) |

## Business Rules

* **Validation first:** `ParseResult.isValid()` must be true before any repository call.
* **Single designator:** `FlightDesignator` from DSL `flight` id must be unique.
* **Pilot ownership:** `pilotId` = authenticated user identity (role Pilot enforced in controller/UI).
* **Route name:** `{firstLeg.departureAirport}-{lastLeg.arrivalAirport}` (e.g. `OPO-LIS`).
* **Schedule:** `FlightSchedule` from minimum departure and maximum arrival across legs.
* **Load:** `FlightLoad` from first leg `load` (passengers + cargo weight).
* **Fuel:** `FuelLoad` from first leg fuel (convert litres to kg if needed, consistent with exporter).
* **Plan status:** `FlightPlanStatus.DRAFT` on creation.
* **Flight Profile in JSON:** Not in DSL; hardcoded in exporter per SCOMP examples (see [jsonMapping.md](jsonMapping.md)).

## Domain Model (Relevant Parts)

```
Flight (@Entity)
├── FlightDesignator (id)
├── FlightType
├── FlightSchedule
├── FlightLoad
├── weatherData (@ManyToOne, optional, null on import)
├── routeName, aircraftRegistration, pilotId
└── FlightPlan (@Entity)
    ├── FlightPlanStatus
    ├── FuelLoad
    ├── dslContent (@Lob)
    └── jsonContent (@Lob) — simulator JSON
```

See [DomainModel.puml](../global_artifacts/DomainModel.puml) — Flight aggregate section.

## Out of Scope (US121)

* DSL grammar and semantic rule definitions → **US120**
* US085 validation/simulation workflow → **US085**
* TCP remote import → **US086**
* Manual flight plan creation without file → **US080**


## Unit Tests (Planned)

* `ensureInvalidFileDoesNotSave`
* `ensureValidImportPersistsFlightWithDslAndJson`
* `ensureDuplicateDesignatorRejected`
* `ensureUnknownAircraftRejected`
* `ensureJsonContentContainsFlightProfile` *(Phase 3)*
