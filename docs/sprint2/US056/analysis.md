## ANALYSIS

**Client Clarification / Product Owner Session**

Engine models are registered with a thrust profile used by simulation (linear interpolation between static and cruise). Fuel is one of a fixed set of enum values chosen in the UI (not free text). Authorised roles: **Admin** or **Backoffice Operator**.

---

## BUSINESS RULES

* The manufacturer referenced must exist in the system
* The combination of engine model name and manufacturer must be unique
* Engine model name must not be blank
* Motorization type must be one of the accepted values: turboprop, turbofan, turbojet, ramjet, electric propeller (matched case-insensitively by label)
* Thrust profile includes both thrust at static and thrust at cruise, both expressed in kN
* Both thrust values must be greater than zero
* thrust at static must be greater than or equal to thrust at cruise
* Thrust follows linear behaviour between static and cruise speed in the domain model (`ThrustProfile.thrustAtSpeed`), consistent with simulator expectations
* Fuel type must be one of the `FuelType` enum values: `JET_A1`, `JET_A`, `JET_B`, `AVGAS_100LL`, `ELECTRICITY`, `HYDROGEN` (selected via UI; persisted as enum)
* TSFC (Thrust Specific Fuel Consumption) must be a positive scalar; the UI labels the unit as N/N/s for operator guidance
* The aggregate identity `EngineModelId` is derived from manufacturer id and engine name in the application service
* This registration must also be achievable through a bootstrap process

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/enginemodelmanagement/`:

* Domain: `EngineNameTest`, `MotorizationTypeTest`, `ThrustProfileTest`, `TSFCTest`, `FuelTypeTest`, `EngineModelIdTest`, `EngineModelTest`
* Application: `EngineModelServiceTest`, `CreateEngineModelControllerTest`, `ListEngineModelsControllerTest`
