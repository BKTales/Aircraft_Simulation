# US120 — Formal Grammar Reference

## Source File

`aisafe.base/aisafe.core/src/main/antlr4/eapli/aisafe/dsl/FlightPlan.g4`

## Generated Artifacts (build time)

Under `target/generated-sources/antlr4/eapli/aisafe/dsl/`:

| Generated class | Purpose |
|-----------------|---------|
| `FlightPlanLexer` | Tokenisation |
| `FlightPlanParser` | Parsing |
| `FlightPlanBaseVisitor` / `FlightPlanVisitor` | Visitor API |
| `FlightPlanBaseListener` / `FlightPlanListener` | Listener API |

## Top-Level Rules (summary)

| Rule | Description |
|------|-------------|
| `flightPlan` | Single `flight` block |
| `flight` | `flight` ID `{` `type` `leg+` `}` |
| `leg` | `departure` `arrival` `route` `fuel` `load` |
| `route` | `segment+` |
| `segment` | Coordinates, `alt`/`width` slots, `wind` |

## Lexer Highlights

* Keywords: case-insensitive (`REGULAR`, `regular`, etc.)
* `AIRPORT_CODE`: 4-letter ICAO preferred over 3-letter IATA
* `DATETIME`: date + time pattern for leg schedule
* Comments: `//` to end of line

## Project Extensions

Document any team extensions to the Core DSL in this file and in `informalSpecification.md`, with justification per Project Requirements §3.4.7.

## Related Documentation

* [informalSpecification.md](informalSpecification.md) — human-readable spec
* [design.md](design.md) — processing pipeline
