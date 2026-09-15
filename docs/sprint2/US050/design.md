## DESIGN

* Follow the standard layered application architecture

**Domain classes:**
`AirControlArea` (aggregate root), `AirControlAreaName`, `AreaCode`, `GeographicBoundary`, `GeographicCoords`, `MinFuelRequirement`

Creation flow in service:

* Convert raw coordinate input to `GeographicCoords`
* Build `GeographicBoundary`
* Build `AirControlAreaName` and `MinFuelRequirement`
* Compare against existing boundaries through `GeographicBoundaryService.checkCollision`
* If overlap exists, throw `OverlapBoundaryException`
* Create `AirControlArea` aggregate and persist via repository

**Console UI:** `RegisterAirControlAreaUI`

**Controller:** `RegisterAirControlAreaController`

**Service:** `AirControlAreaService`

**Repository:** `AirControlAreaRepository`
