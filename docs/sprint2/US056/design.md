## DESIGN

* Follow the standard layered application architecture

**Domain classes:**
`EngineModel` (aggregate root), `EngineModelId`, `EngineName`, `ThrustProfile`, `TSFC`, `MotorizationType` (enum), `FuelType` (enum)

`EngineModel` embeds:

* `EngineName` — value object, non-blank name
* `ThrustProfile` — value object, static and cruise thrust in kN with ordering invariant
* `TSFC` — value object, strictly positive value
* `ManufacturerId` — cross-aggregate reference to `Manufacturer` (no cascade)

**Console UI:** `RegisterEngineModelUI`

**Controller:** `CreateEngineModelController`

**Service:** `EngineModelService` (validates manufacturer, uniqueness of name+manufacturer, builds `EngineModelId`, assembles aggregate)

**Repositories:** `EngineModelRepository`, `ManufacturerRepository` (via persistence context in production wiring)
