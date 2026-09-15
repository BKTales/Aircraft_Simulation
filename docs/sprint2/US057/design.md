## DESIGN

* Follow the standard layered application architecture

**Domain classes:**
`AircraftModel` (aggregate root), `AircraftModelId`, `EngineConfiguration`, `EngineModelId`

`AircraftModel` owns a collection of certified engine configurations and exposes:

* `addEngineConfiguration(EngineModelId)` — adds a certification entry
* duplicate engine ids are rejected with domain validation

**Console UI:** `AddEngineModelToAircraftModelUI`

**Controller:** `AddEngineModelToAircraftModelController`

**Service:** `AircraftModelService.addEngineModelToAircraftModel(...)`

Service responsibilities:

* Validate input ids
* Resolve aircraft model from `AircraftModelRepository`
* Resolve engine model from `EngineModelRepository`
* Delegate duplicate prevention to aggregate method
* Persist updated aggregate through `AircraftModelRepository.save`

**Repositories:** `AircraftModelRepository`, `EngineModelRepository`
