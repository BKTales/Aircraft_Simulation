# US120 — Analysis

## Client Clarification / Product Owner Session

* US120 is the Sprint 3 evolution of Sprint 2 work (**US081** informal import, **US083** DSL specification and parser).
* The parser is a **shared validation gate** for any feature that accepts Flight DSL text (file import US121, stored DSL re-validation US085).
* US120 stops at producing a validated `FlightPlanDescriptor` (or a failure `ParseResult`); it does not create `Flight` aggregates.

## Current Implementation (Baseline)

| Component | Location | Status |
|-----------|----------|--------|
| Informal spec (US120) | `docs/sprint3/US120/informalSpecification.md` | Done |
| ANTLR grammar | `aisafe.core/src/main/antlr4/eapli/aisafe/dsl/FlightPlan.g4` | Done |
| Parser API | `eapli.aisafe.dsl.api.FlightDslParser` | Done |
| Syntax errors | `eapli.aisafe.dsl.parse.FlightPlanErrorListener` | Done — line/column |
| Visitor (build model) | `eapli.aisafe.dsl.parse.FlightPlanBuilder` | Done |
| Listener (shape check) | `eapli.aisafe.dsl.parse.FlightPlanTreeShapeListener` | Done |
| Semantic rules | `eapli.aisafe.dsl.model.FlightPlanDescriptor.validateSemantics()` | Done |
| Descriptor model | `FlightPlanDescriptor`, `LegDescriptor`, `RouteDescriptor`, `SegmentDescriptor`, `AltitudeSlotDescriptor` | Done |
| Unit tests | `aisafe.core/src/test/java/.../FlightDslParserTest.java` | Done |



## Business Rules (Semantic Validation)

Implemented in `FlightPlanDescriptor.validateSemantics()`:

* Flight type must be `REGULAR` or `CHARTER`.
* Per leg: arrival datetime after departure; fuel > 0; load ≥ 0.
* Multi-leg: departure airport equals previous leg arrival; departure after previous arrival.
* No repeated airports across the flight (current implementation).
* Per segment: valid coordinate ranges; start ≠ end; wind direction 0–360; wind speed > 0; altitude and width > 0.



## Unit Tests (Planned / Existing)

* `ensureValidPlanParsesAndValidates`
* `ensureSyntaxErrorReportsLineAndColumn`
* `ensureSemanticErrorForInvalidFuel`
* `ensureSemanticErrorForRouteDiscontinuity`
* `ensureSemanticErrorReportsLineAndColumn` *(Phase 2)*
