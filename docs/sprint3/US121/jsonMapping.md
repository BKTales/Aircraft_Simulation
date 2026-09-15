# US121 — DSL to Simulator JSON Mapping

## Purpose

`FlightPlan.jsonContent` stores JSON understood by the **SCOMP** `flight_simulator` component. The DSL does not define all JSON fields; the Java exporter maps DSL descriptors to JSON and fills gaps with defaults.

**Authoritative examples:** `flight_simulator/src/data/flight_plans/` (e.g. [flight_plan0.json](../../../flight_simulator/src/data/flight_plans/flight_plan0.json)).


---

## Root Object

| JSON field | Source | Notes |
|------------|--------|-------|
| `ID` | Hash/stable id from DSL flight id | Integer, see `FlightPlanJsonExporter.stableId` |
| `Type` | DSL flight type | Lowercase: `regular`, `charter` |
| `Route` | DSL flight id or derived route name | Align with team decision |
| `DepartureTime` | First leg departure | `HH:MM` extracted from `YYYY-MM-DD HH:MM` |
| `AircraftId` | Selected aircraft at import | Model id or registration per simulator contract |
| `Mass` | First leg `load` cargo weight (kg) | As in `flight_plan1.json` |
| `Leg` | Array of legs | One per DSL `leg` |

---

## Leg Object

| JSON field | Source | Notes |
|------------|--------|-------|
| `Departure` | DSL departure airport | Object: `{ "Airport": "OPO", "AreaCode": "PT-N" }` |
| `Arrival` | DSL arrival airport | Object: `{ "Airport": "MAD", "AreaCode": "ES-M" }` |
| `Fuel` | DSL `fuel` | `{ "Quantity": n, "Unit": "kg" }` — convert `l` → kg if needed |
| `Flight Profile` | **Hardcoded** | Not in DSL; see below |
| `Segments` | DSL `route` / `segment` | See segment mapping |

### AreaCode placeholder

Until airport→air control area mapping exists (US050/US073), use placeholders consistent with bootstrap or SCOMP samples (`PT-N`, `ES-M`, or `AREA-0` / `AREA-1`).


---

## Pipeline

```
.txt file
  → dslContent (stored verbatim)
  → FlightDslParser (US120)
  → FlightPlanDescriptor
  → FlightPlanJsonExporter.toSimulatorJson(descriptor, aircraftId, massKg)
  → jsonContent (stored on FlightPlan)
```


---

## Example Fragment (Flight Profile)

```json
"Flight Profile": {
  "Climb": [
    { "Altitude": 0, "IAS": 200 },
    { "Altitude": 1000, "IAS": 210 },
    { "Altitude": 2000, "IAS": 220 },
    { "Altitude": 3000, "IAS": 230 },
    { "Altitude": 4000, "IAS": 245 },
    { "Altitude": 5000, "IAS": 255 },
    { "Altitude": 6000, "IAS": 265 },
    { "Altitude": 7000, "IAS": 275 },
    { "Altitude": 8000, "IAS": 285 },
    { "Altitude": 9000, "IAS": 295 },
    { "Altitude": 10000, "IAS": 300 },
    { "Altitude": 11000, "IAS": 300 }
  ],
  "Descend": [
    { "Altitude": 0, "IAS": 145 },
    { "Altitude": 1000, "IAS": 170 },
    { "Altitude": 2000, "IAS": 200 },
    { "Altitude": 3000, "IAS": 230 },
    { "Altitude": 4000, "IAS": 260 },
    { "Altitude": 5000, "IAS": 280 },
    { "Altitude": 6000, "IAS": 285 },
    { "Altitude": 7000, "IAS": 290 },
    { "Altitude": 8000, "IAS": 295 },
    { "Altitude": 9000, "IAS": 300 },
    { "Altitude": 11000, "IAS": 300 }
  ]
}
```

Full leg structure: see `flight_plan0.json` lines 9–83.
