# Flight Description Language — Informal Specification (US120)

This document describes the informal lexical and syntactic specification of the **Core Flight DSL** used to define flight plans in the AISafe system. It aligns with Project Requirements §3.4.2–3.4.3.

The formal grammar is in `aisafe.core/src/main/antlr4/eapli/aisafe/dsl/FlightPlan.g4`.

---

## 1. Lexical Specification

The lexical specification defines the basic units (tokens) of the language.

### 1.1. Keywords

Keywords are reserved and define the structure of the language. They are **case-insensitive**.

- `flight` — defines the start of a flight plan
- `type` — defines the flight regime (`regular` or `charter`)
- `leg` — defines a non-stop flight segment between two airports
- `departure` — defines the departure data of a leg
- `arrival` — defines the arrival data of a leg
- `route` — opens the trajectory definition block
- `segment` — defines an individual route segment
- `alt` — prefix for an altitude slot value
- `width` — prefix for the width of an altitude slot
- `wind` — prefix for wind direction and speed
- `fuel` — defines the fuel quantity for a leg
- `load` — defines the passenger count and cargo weight for a leg

### 1.2. Identifiers

- Used to assign names or codes to flights
- Are **case-sensitive** — `TP123` is different from `tp123`
- Must start with a letter and may contain letters, digits, and underscores

### 1.3. Airport Codes

- **IATA** codes — exactly 3 uppercase letters (e.g. `OPO`, `LIS`, `LHR`)
- **ICAO** codes — exactly 4 uppercase letters (e.g. `LPPR`, `LPPT`, `EGLL`)
- ICAO codes take lexer precedence over IATA codes due to length

### 1.4. Literals and Units

- **Numbers** — integers or decimals using `.` as the decimal separator (e.g. `4500`, `10.5`)
- **Units** are mandatory alongside technical values:
  - Distance / Altitude: `m` (metres)
  - Speed: `m/s` (metres per second)
  - Fuel: `kg` (kilograms) or `l` (litres)

### 1.5. Date and Time Formats

- **Date** — `YYYY-MM-DD` (e.g. `2026-05-20`)
- **Time** — `HH:MM` (e.g. `14:30`)
- Combined departure/arrival in grammar: `YYYY-MM-DD HH:MM`

### 1.6. Symbols and Delimiters

- `{ }` — delimit hierarchical blocks
- `( )` — group geographic coordinates
- `;` — terminates instructions within blocks
- `,` — separates values (e.g. latitude and longitude)

Whitespace and line breaks have no semantic meaning.

---

## 2. Syntactic Specification

The language follows a **hierarchical block-based** organisation.

### 2.1. Global Structure (Flight Block)

Every file must contain a single `flight` block:

1. Declaration: `flight` followed by an `ID` and `{`
2. Type: `type regular;` or `type charter;` (case-insensitive keyword, normalised to `REGULAR` / `CHARTER` in the model)
3. One or more `leg` blocks

### 2.2. Leg Block

Each leg must contain, in order:

- **Departure** — airport code, date, time
- **Arrival** — airport code, date, time
- **Route** — sub-block with one or more `segment` definitions
- **Fuel** — quantity and unit
- **Load** — passenger count and cargo weight

### 2.3. Segment and Route

Each segment defines:

- Start and end coordinates `(latitude, longitude)`
- One or more altitude slots: `alt <value> m width <value> m`
- Wind: direction (0–360°) and speed in `m/s`

### 2.4. Coordinate Format

Decimal degrees: latitude ∈ [-90, 90], longitude ∈ [-180, 180].

---

## 3. Semantic Validation Rules

After successful syntactic parsing, the following rules are enforced (`FlightPlanDescriptor.validateSemantics()`):

| Rule | Description |
|------|-------------|
| Flight type | Must be `REGULAR` or `CHARTER` |
| Leg times | Arrival after departure on same leg |
| Multi-leg continuity | Leg N+1 departs from leg N arrival airport |
| Multi-leg schedule | Leg N+1 departs after leg N arrives |
| Fuel | Strictly positive per leg |
| Load | Passenger count and cargo weight ≥ 0 |
| Airports | No airport repeated in the **flight** *(implementation choice; enunciado mentions repetition within a **route** — see [analysis.md](analysis.md) G3)* |
| Segment geometry | Start ≠ end; coordinates in range |
| Wind | Direction 0–360; speed > 0 |
| Altitude slots | Altitude and width > 0 |

---

## 4. Example of Valid Input

```text
flight Test_Flight {
    type REGULAR ;

    leg {
        departure LPPT 2026-04-21 14:30 ;
        arrival EGLL 2026-04-21 17:15 ;

        route {
            segment (38.77, -9.13) (51.47, -0.45)
                alt 30000 m width 500 m ;
                wind 25 10.5 m/s ;

            segment (39.27, -1.13) (71.47, -1.45)
                alt 3020 m width 505 m ;
                wind 90 75.5 m/s ;
        }

        fuel 4500.50 kg ;
        load 150 1200.0 ;
    }
}
```

---

## 5. Formal Processing (ANTLR)

See [design.md](design.md) and [grammar.md](grammar.md). Processing uses lexer, parser, visitor (`FlightPlanBuilder`), listener (`FlightPlanTreeShapeListener`), and semantic validation on descriptors.

---

## History

| Version | US | Notes |
|---------|-----|-------|
| Sprint 2 | US083 | Initial informal spec in `DSL/docs/` |
| Sprint 3 | US120 | Canonical copy under `docs/sprint3/US120/` |
