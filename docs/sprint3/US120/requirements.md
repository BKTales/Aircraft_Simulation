# US120 — Flight DSL Specification and Validation

## User Story

As a Project Manager, I want the team to specify and implement the Flight Description DSL, so that flight plans can be formally defined and validated.

---

## Requirements

### Functional

1. **R1 — Informal specification**
   The informal lexical and syntactic specification of the Core Flight DSL is documented (see [informalSpecification.md](informalSpecification.md) and Project Requirements §3.4.2–3.4.3).

2. **R2 — Formal grammar (ANTLR)**
   A formal grammar is defined using ANTLR, including lexer and parser rules (`FlightPlan.g4`).

3. **R3 — Lexical, syntactic, and semantic analysis**
   The system performs full analysis of flight plan text: tokenisation, parse tree construction, and semantic validation beyond grammar.

4. **R4 — Listener and visitor**
   ANTLR listeners and visitors are used in the processing pipeline (generated base classes + project-specific implementations).

5. **R5 — Internal representation**
   A structured internal representation is produced after successful parsing (descriptor model: `FlightPlanDescriptor` and related value objects).

6. **R6 — Error reporting**
   Invalid inputs produce clear, informative error messages. Syntax and lexical errors include line and column; semantic errors should also report position (planned improvement).

### Non-functional

7. **R7 — Scope boundary**
   US120 does **not** cover persistence of flight plans, JSON export for the C simulator, or UI import flows — those belong to **US121** and downstream stories (US085, US100).

8. **R8 — Compatibility**
   Extensions to the Core DSL must remain compatible with the formal grammar and documented specification (Project Requirements §3.4.7, NFR11).

---

## Acceptance Criteria

| ID | Criterion | Evidence |
|----|-----------|----------|
| AC1 | Informal lexical and syntactic spec is documented | `docs/sprint3/US120/informalSpecification.md` |
| AC2 | Formal ANTLR grammar exists | `aisafe.core/.../antlr4/.../FlightPlan.g4` |
| AC3 | Lexical, syntactic, and semantic validation run on input | `FlightDslParser.parse` |
| AC4 | Listeners and visitors are used | `FlightPlanBuilder`, `FlightPlanTreeShapeListener` |
| AC5 | Internal representation (descriptors) is produced on success | `FlightPlanDescriptor`, `ParseResult` |
| AC6 | Invalid input yields meaningful errors | `FlightPlanErrorListener`, `validateSemantics()` |
| AC7 | Syntax errors include line and column | `FlightPlanErrorListener` format `Line L:C: ...` |

---

## Relationship to Other User Stories

| US | Relationship |
|----|----------------|
| **US081 / US083** (Sprint 2) | Initial DSL grammar, parser, and console import prototype; US120 extends semantics and documentation |
| **US121** | Consumes `FlightDslParser` / `ParseResult`; must not reimplement validation |
| **US085** | Re-validates DSL stored in a flight plan using the same parser (US120) |
| **US080** | Manual flight plan creation; may store DSL without validation at creation time |

---

## References

- [Project_Requirements_V3.md](../../../../Project_Requirements_V3.md) — §3.4 Flight plans, §3.4.4 ANTLR
- [sprint_planning.md](../sprint_planning.md) — US120 scope (semantic rules, listener + visitor, error line/col)
- NFR11 — LPROG assessment (grammar, validation, error reporting)
