# US120 — Tests

## Test Location

* `aisafe.core/src/test/java/eapli/aisafe/dsl/FlightDslParserTest.java`
* `aisafe.core/src/test/java/eapli/aisafe/dsl/FlightDslParserFileTest.java`
* Fixtures: `aisafe.core/src/test/resources/dsl/` (`valid.txt`, `invalid_syntax.txt`, `invalid_semantic_*.txt`)

## Unit Tests — FlightDslParser

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureValidPlanParsesSuccessfully` | `valid.txt` | `ParseResult.isValid()`, descriptor with flight id, type, legs |
| `ensureSyntaxErrorFailsWithLineReference` | `invalid_syntax.txt` | Invalid result; errors contain `Line` |
| `ensureSemanticErrorInvalidFuel` | `invalid_semantic_fuel.txt` | Invalid; message mentions fuel |
| `ensureSemanticErrorRouteDiscontinuity` | `invalid_semantic_route.txt` | Invalid; discontinuity message |
| `ensureSemanticErrorRepeatedAirport` | `invalid_semantic_airport.txt` | Invalid; repeated airport |
| `ensureNonTxtFileRejected` | Path ending `.json` | Failure without parse |
| `ensureEmptyInputFails` | Empty string | Failure |
| `ensureSemanticErrorReportsLineAndColumn` | `invalid_semantic_wind.txt` | Errors match `Line L:C:` |

## Unit Tests — FlightPlanBuilder / Descriptor (optional)

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureTreeShapeListenerDetectsMismatch` | Mock tree/descriptor mismatch | Internal error message |

## Integration Tests

US120 has **no database integration**. File-based tests read classpath or `src/test/resources/dsl/` only.

## Manual / Console Verification

* Backoffice menu: import flight plan (validates via `FlightDslParser` before any US121 save).
* Standalone: `DSL` module `DslImportApp` (if used for demos).

## Coverage Target

Align with Sprint 2 LPROG coverage goal (>90% on parser and domain descriptor packages) — see project `LPROG-Coverage.md` if present.

## Regression Checklist (before closing US120)

- [ ] All existing `FlightDslParserTest` tests pass
- [ ] New semantic line/column tests pass *(Phase 2)*
- [ ] `informalSpecification.md` matches `FlightPlan.g4` behaviour
- [ ] No breaking change to `ParseResult` public API without updating US121 docs
