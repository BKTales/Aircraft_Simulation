## ANALYSIS

**Client Clarification / Product Owner Session**

The use case is to add an already existing and certified engine model reference to an already existing aircraft model. This operation does not create engine models and does not edit aircraft model technical data; it only extends the aircraft model certified engine configuration list. Authorised roles: **Admin** or **Backoffice Operator**.

---

## BUSINESS RULES

* Aircraft model must exist (`AircraftModelRepository.findByID`)
* Engine model must exist (`EngineModelRepository.findByModelId`)
* The same engine model cannot be added twice to the same aircraft model
* On success, the updated aircraft model is persisted through `AircraftModelRepository.save`
* The operation is protected by role-based authorisation in controller level

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/aircraftmodelmanagement/`:

* Application: `AddEngineModelToAircraftModelControllerTest`, `AircraftModelServiceTest`
* Domain support: `AircraftModelTest` (engine configuration behavior)

---

## UNIT TESTS (service / controller scenarios)

* `EnsureAddEngineRequiresAuthorization`
* `EnsureAircraftModelMustExist`
* `EnsureEngineModelMustExist`
* `EnsureDuplicateEngineCertificationIsRejected`
* `EnsureUpdatedAircraftModelIsPersisted`
