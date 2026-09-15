## REQUIREMENTS

### Functional

1. **R1 — Register air control area**  
   An authorised user can register an air control area with name, boundary coordinates, and minimum fuel requirement.

2. **R2 — Boundary validation**  
   Boundary must be geometrically valid and have non-zero area.

3. **R3 — Non-overlap rule**  
   New area boundary cannot overlap existing registered areas.

4. **R4 — Domain value validation**  
   Name and minimum fuel requirement must satisfy domain constraints.

5. **R5 — Authorisation**  
   Only **Admin** or **Backoffice Operator** may invoke `RegisterAirControlAreaController.registerAirControlArea`.

### Non-functional

6. **R6 — Persistence**  
   New air control area is persisted through `AirControlAreaRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given valid area data and a non-overlapping boundary, when registration is executed, then the new air control area is persisted with a generated area code. |
| AC2 | Given an invalid boundary definition, when registration is executed, then validation fails before save. |
| AC3 | Given a boundary that overlaps an existing area, when registration is executed, then `OverlapBoundaryException` is raised and nothing is persisted. |
| AC4 | Given invalid domain values (for example name/fuel), when registration is executed, then validation fails before save. |
| AC5 | Given a user who is neither Admin nor Backoffice Operator, when the controller method is invoked, then authorisation fails. |
