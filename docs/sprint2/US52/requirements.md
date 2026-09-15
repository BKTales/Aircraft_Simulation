## REQUIREMENTS

### Functional

1. **R1 — Register airport**  
   A backoffice operator can register an airport by supplying IATA code, ICAO code, latitude, longitude, and elevation (m above sea level).

2. **R2 — Automatic air control area**  
   The system assigns the airport to the unique air control area whose geographic boundary contains the given coordinates. The operator does not select an area manually.

3. **R3 — Codes and location validation**  
   IATA and ICAO formats, uniqueness, normalisation to uppercase, and coordinate/elevation ranges are enforced as described in `analysis.md`.

4. **R4 — Authorisation**  
   Only authenticated users with role **Backoffice Operator** may execute the register-airport use case via the controller.

5. **R5 — Bootstrap**  
   The same registration rules can be satisfied by loading airports through the bootstrap process (`AirportsBootstrapper`).

### Non-functional

6. **R6 — Persistence**  
   The new airport is persisted through `AirportRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given coordinates inside an existing air control area polygon, when a valid unique IATA/ICAO pair is submitted, then the airport is saved and stores the correct area code. |
| AC2 | Given coordinates outside every air control area, when the user submits registration, then the operation fails with a clear error (no airport persisted). |
| AC3 | Given an IATA code already used by another airport, when registration is attempted, then the operation fails and no duplicate IATA row exists. |
| AC4 | Given an ICAO code already used by another airport, when registration is attempted, then the operation fails and no duplicate ICAO row exists. |
| AC5 | Given mixed-case or spaced IATA/ICAO input, when the airport is saved, then codes are persisted in uppercase after trim. |
| AC6 | Given latitude/longitude/elevation outside allowed ranges (including negative elevation), when registration is attempted, then validation fails before persistence. |
| AC7 | Given a user without the Backoffice Operator role, when `CreateAirportController.createAirport` is invoked, then authorisation fails. |
| AC8 | Given bootstrap execution after areas exist, when airports are seeded, then the same validation and auto-area rules apply as in the interactive flow. |
