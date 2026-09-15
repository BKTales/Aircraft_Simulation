## REQUIREMENTS

### Functional

1. **R1 — Register air transport company**  
   An authorised user can register a company with name, 2-letter IATA designator, and 2–3 letter ICAO designator.

2. **R2 — Uniqueness and format**  
   IATA and ICAO must satisfy format rules and global uniqueness among companies.

3. **R3 — Name**  
   Company name must be non-empty per `CompanyName` rules.

4. **R4 — Authorisation**  
   Only **Admin** or **Backoffice Operator** may call `RegisterAirTransportCompanyController.registerCompany`.

5. **R5 — Collaborators out of scope**  
   This use case does not create collaborator accounts; those are added in collaborator user stories.

6. **R6 — Bootstrap**  
   Companies can be seeded via `AirTransportCompaniesBootstrapper`.

### Non-functional

7. **R7 — Persistence**  
   The aggregate is persisted through `AirTransportCompanyRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given valid unique IATA/ICAO and a non-empty name, when registration runs, then the company is persisted with normalised uppercase codes. |
| AC2 | Given an IATA already registered, when registration runs, then `IATACodeAlreadyExistsException` is raised and no new row is created. |
| AC3 | Given an ICAO already registered, when registration runs, then `ICAOCodeAlreadyExistsException` is raised and no new row is created. |
| AC4 | Given invalid IATA length or non-letters, when registration runs, then validation fails before persistence. |
| AC5 | Given invalid ICAO length (not 2–3 letters), when registration runs, then validation fails before persistence. |
| AC6 | Given a user who is neither Admin nor Backoffice Operator, when the controller is invoked, then authorisation fails. |
| AC7 | Given bootstrap execution, when companies are seeded, then the same uniqueness and format rules apply. |
