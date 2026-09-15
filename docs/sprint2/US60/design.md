## DESIGN

* Follow the standard layered application architecture

**Domain classes:**
`AirTransportCompany`, `CompanyName`, `IATACode`, `ICAOCode`

`AirTransportCompany` is the Aggregate Root; **identity** is the embedded `IATACode` (`@EmbeddedId`).

`CompanyName`, `ICAOCode` are value objects embedded in the entity.

There is **no embedded collaborator collection** on this aggregate in the current model; collaborators reference the company IATA in their own aggregate.

**Console UI:** `RegisterAirTransportCompanyUI`

**Controller:** `RegisterAirTransportCompanyController`

**Service:** `AirTransportCompanyService`

**Repository:** `AirTransportCompanyRepository` (uniqueness checks via `existsByIataCode` / `existsByIcaoCode`)
