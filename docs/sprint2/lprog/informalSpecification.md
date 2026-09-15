# Flight Description Language – Specification (US083)

This document describes the informal lexical and syntactic specification of the DSL developed for defining flight plans in the AISafe system.

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
- ICAO codes take lexer precedence over IATA codes due to length — the lexer must match ICAO before IATA to avoid ambiguity

### 1.4. Literals and Units
- **Numbers** — integers or decimals using `.` as the decimal separator (e.g. `4500`, `10.5`)
- **Units** are mandatory alongside technical values:
  - Distance / Altitude: `m` (metres)
  - Speed: `m/s` (metres per second)
  - Fuel: `kg` (kilograms) or `l` (litres)

### 1.5. Date and Time Formats
- **Date** — `YYYY-MM-DD` (e.g. `2026-05-20`)
- **Time** — `HH:MM` (e.g. `14:30`)

### 1.6. Symbols and Delimiters
- `{ }` — delimit hierarchical blocks
- `( )` — group geographic coordinates
- `;` — terminates instructions within blocks
- `,` — separates values (e.g. latitude and longitude within coordinates)

---

## 2. Syntactic Specification

The syntactic specification defines how tokens are combined to form valid structures. The language follows a **hierarchical block-based** organisation.

### 2.1. Global Structure (Flight Block)
Every file must contain a single `flight` block encapsulating the entire flight plan:
1. Declaration: `flight` followed by an `ID` and opening brace `{`
2. Type definition: `type regular;` or `type charter;`
3. One or more `leg` blocks

### 2.2. Leg Block
Each leg represents a non-stop journey between two airports. Must contain, in order:
- **Departure** — origin airport code, date, and time
- **Arrival** — destination airport code, date, and time
- **Route** — a sub-block containing one or more `segment` definitions
- **Fuel** — total fuel quantity for the leg
- **Load** — passenger count and cargo weight for the leg

### 2.3. Segment and Route Rules
A route is composed of sequential segments. Each segment defines:
- Start and end geographic coordinates `(latitude, longitude)` — two coordinate pairs
- One or more altitude slots, each with an altitude value and a width (`alt ... width ...`)
- Wind information: direction in degrees relative to North (0–360) and speed in m/s

### 2.4. Coordinate Format
Coordinates are expressed as decimal degree pairs: `(latitude, longitude)` where latitude ranges from -90 to 90 and longitude from -180 to 180.

---

## 3. Semantic Validation Rules

After successful syntactic validation, the following semantic rules must be verified:

- The arrival airport of leg N must match the departure airport of leg N+1
- The arrival time of leg N must precede the departure time of leg N+1
- The same airport cannot appear twice within a single route
- Fuel quantity must be strictly positive
- Passenger count and cargo weight must be non-negative
- Altitude values must be strictly positive
- Wind direction must be between 0 and 360 degrees
- Coordinates must be within valid geographic ranges

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