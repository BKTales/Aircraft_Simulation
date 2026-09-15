## DESIGN

* Follow the standard layered application architecture

**Domain classes:**  
`Aircraft`, `AircraftRegistration`, `RegistrationCountry`, `OperationalStatus`, `CabinConfiguration`, `NumberOfFlightCrew`, `YearOfManufacture`

`Aircraft` is the aggregate root (`@EmbeddedId` = `AircraftRegistration`).

| Value object / embeddable | Role |
|---------------------------|------|
| `AircraftRegistration` | Identity (tail number) |
| `RegistrationCountry` | ISO-2 registration country |
| `OperationalStatus` | `ACTIVE` or `DECOMMISSIONED` |
| `CabinConfiguration` | Economy / business / first seats (`@OneToOne`, cascade ALL) |
| `NumberOfFlightCrew` | Crew count |
| `YearOfManufacture` | Year built; `ageInYears()` = current calendar year − year |

**Cross-aggregate references (by id only):** `AircraftModelId`, `EngineModelId`, `IATACode` (owner company).

**Console UI:** `RegisterAircraftUI` — selects model and certified engine from lists; prompts year of manufacture; does not prompt operational status.

**Controller:** `RegisterAircraftController` — authorisation + `CompanyCollaboratorSession` → company IATA → delegates to service.

**Service:** `AircraftService.registerAircraft(...)`

**Repositories:** `AircraftRepository`, `AircraftModelRepository`

**Shared application helper:** `CompanyCollaboratorSession.requireAirTransportCompanyCollaborator(...)`
