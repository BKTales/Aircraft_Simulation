## DESIGN

* Follow the standard layered application architecture

**Domain Classes:**
`Airport`, `AirportIATACode`, `AirportICAOCode`, `Coordinates`

`Airport` is an Aggregate Root belonging to the `AirportAggregate`

`AirportIATACode` is a Value Object (embedded id) owned by `Airport`

`AirportICAOCode` is a Value Object owned by `Airport`

`Coordinates` is a Value Object owned by `Airport` — holds latitude, longitude, and elevation (metres above sea level) in one embeddable

`Airport` stores the air control area as a string **area code** only (`AIR_CONTROL_AREA_CODE`) — cross-aggregate reference to `AirControlArea`, no JPA association or cascade

**Console UI:** `CreateAirportUI`

**Controller:** `CreateAirportController`

**Service:** `AirportService` (resolves containing `AreaCode` from `AirControlAreaRepository` before persisting)

**Repositories:** `AirportRepository`, `AirControlAreaRepository`
