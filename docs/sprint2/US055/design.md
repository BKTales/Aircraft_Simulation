## DESIGN

* Follow the standard layered application architecture

**Domain Classes:**

`AircraftModel` (Aggregate Root)
* @Entity with @Id AircraftModelId
* Embeds: ModelName, AircraftType, WeightSpecification, WingGeometry, AerodynamicCoefficients, PerformanceSpec, NumberOfEngines, NumberOfSeats
* References (cross-aggregate, no cascade):
  - ManufacturerId (identifies Manufacturer)
* Owns (OneToMany): List<EngineConfiguration> with CascadeType.ALL, orphanRemoval=true
  - Relationship: @JoinColumn(name = "AIRCRAFT_MODEL_ID")

`AircraftModelId` (Value Object)
* @Embeddable identifier stored as string model code
* Format: user-provided modelId string

`EngineConfiguration` (Owned Entity, DomainEntity<Long>)
* @Entity with auto-generated @Id (Long)
* Embeds: EngineModelId (reference to engine, no cascade)
* Represents a certified engine for this aircraft model
* Lifecycle: managed by parent AircraftModel
  - CascadeType.ALL ensures creation/deletion with parent
  - orphanRemoval=true removes configurations when removed from list
* Cross-aggregate reference to EngineModel via EngineModelId

`ModelName`, `WeightSpecification`, `WingGeometry`, `AerodynamicCoefficients`, `PerformanceSpec`, `NumberOfEngines`, `NumberOfSeats` (Value Objects)
* All @Embeddable
* Implement business rule validations in constructors
* Use factory methods: valueOf(...)

`AircraftType` (enum)
* Values: passenger, cargo, mixed
* Persisted as @Enumerated(EnumType.STRING)

**Repository Specifications:**

`AircraftModelRepository`
* `findByModelId(AircraftModelId)`: Retrieves aircraft model with engine configurations
* `existsByNameAndManufacturer(ModelName, ManufacturerId)`: Uniqueness check
* Lazy loading: EngineConfiguration list loaded on demand via OneToMany relationship
* JPA Join Strategy: EngineConfiguration entities loaded with parent via cascade
  - Supports efficient validation of engine count and type

**Application Layer:**

`AircraftModelService`
* `createAircraftModel(...)`: Orchestrates aircraft model creation
  - Validates manufacturer exists (ManufacturerRepository.findById)
  - Validates uniqueness (AircraftModelRepository.existsByNameAndManufacturer)
  - Validates at least one engine model provided
  - Validates max passenger seats > 0
  - Validates all engine models exist (EngineModelRepository.findByModelId)
  - Creates AircraftModel with empty EngineConfiguration list
  - Adds certified EngineConfigurations one by one
  - Validates: ensureCertifiedEngineCount() — at least 1 configuration
  - Persists via AircraftModelRepository.save()
* Validation Sequence:
  1. Manufacturer existence
  2. Name+Manufacturer uniqueness
  3. At least 1 engine model specified
  4. Max passenger seats > 0
  5. Each engine model exists in repository
  6. Engine configurations created successfully
  7. Final count validation: numCertified >= 1

**Controller:** `CreateAircraftModelController`

**Service:** `AircraftModelService`

**Repository:** `AircraftModelRepository`, `ManufacturerRepository`, `EngineModelRepository`

**UI:** `RegisterAircraftModelUI`
