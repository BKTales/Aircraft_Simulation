## ANALYSIS

**Client Clarification / Product Owner Session**

---

## BUSINESS RULES

* The manufacturer referenced must exist in the system
* The combination of aircraft model name and manufacturer must be unique
* The aircraft model must include at least one certified engine model
* Aircraft model name and model ID must not be blank
* Aircraft type is an enum (passenger, cargo, mixed)
* MTOW, MZFW and empty weight must be positive values, and satisfy MTOW > MZFW > empty weight
* Wing area and wing span must be positive values
* Cd0 must be non-negative (Cl is accepted as provided)
* Service ceiling, cruise speed, max range, and fuel capacity must be positive values
* Number of engines must be a positive integer
* Maximum passenger seats must be a positive integer
* Engine model references must exist in the system
* EngineConfiguration is an owned entity (OneToMany) containing references to EngineModel by EngineModelId
* Engine configuration list must contain at least one engine model
* This registration must also be achievable through a bootstrap process

---

## UNIT TESTS

* `EnsureManufacturerExists`
* `EnsureModelNameIsNotBlank`
* `EnsureModelNameAndManufacturerCombinationIsUnique`
* `EnsureAtLeastOneEngineModelIsProvided`
* `EnsureWeightsAreValidAndOrdered`
* `EnsureWingGeometryIsPositive`
* `EnsureAerodynamicCd0IsNonNegative`
* `EnsurePerformanceSpecIsPositive`
* `EnsureNumberOfEnginesIsPositive`
* `EnsureMaxPassengerSeatsIsPositive`
* `EnsureAircraftModelIsPersistedThroughRepository`
* `EnsureConstructorRejectsNullFields`
* `EnsureEngineConfigurationUniqueness` — validates no duplicate engine configurations
