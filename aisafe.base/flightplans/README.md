# Flight DSL fixtures (US120 / US081 / US121)

Each file name (without extension) matches the `flight <id>` inside the file.

| File | Expected |
|------|----------|
| `valid.txt` | Valid REGULAR plan (LPPT → EGLL) |
| `valid_charter.dsl` | Valid CHARTER plan |
| `invalid_syntax.txt` | Syntax error (missing `;`) |
| `invalid_semantic_wind.txt` | Wind direction > 360° |
| `invalid_semantic_wind_speed.txt` | Wind speed ≤ 0 |
| `invalid_semantic_coords.txt` | Latitude/longitude out of range |
| `invalid_semantic_segment_endpoints.txt` | Segment start equals end |
| `invalid_semantic_altitude.txt` | Altitude ≤ 0 |
| `invalid_semantic_fuel.txt` | Fuel ≤ 0 |
| `invalid_semantic_negative_passengers.txt` | Negative passenger count |
| `invalid_semantic_invalid_datetime.txt` | Unparseable departure datetime |
| `invalid_semantic_arrival_before_departure.txt` | Arrival not after departure (same leg) |
| `invalid_semantic_leg_gap_time.txt` | Departure before previous leg arrival |
| `invalid_semantic_route.txt` | Airport discontinuity between legs |
| `invalid_semantic_airport.txt` | Repeated airport in itinerary |

Copy these into `aisafe.base/flightplans/` for console / remote import demos.

**Segment syntax:** after each `alt … width …` pair you must write `;` before `wind` (see `valid.txt`).
