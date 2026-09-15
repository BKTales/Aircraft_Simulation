## REQUIREMENTS

### Functional

1. **R1 — List company fleet**  
   A company collaborator can list all aircraft in their company’s fleet, including decommissioned aircraft.

2. **R2 — Session company**  
   Results are scoped to the collaborator’s company IATA from the session.

3. **R3 — Filter by model (US072a)**  
   The system offers aircraft models present in the fleet; after selection, only aircraft of that model are listed.

4. **R4 — Filter by maker (US072b)**  
   The system offers manufacturers of models used in the fleet; after selection, only aircraft whose model belongs to that maker are listed.

5. **R5 — Filter by capacity (US072c)**  
   After the user enters a seat count, they choose to list aircraft with more than, less than, or exactly that total passenger capacity.

6. **R6 — Filter by age (US072d)**  
   After the user enters an age in years, they choose to list aircraft older than, younger than, or exactly that age (from year of manufacture).

7. **R7 — Authorisation**  
   Only **Air Transport Company Collaborator** users may list the fleet.

### Non-functional

8. **R8 — Presentation**  
   Results show registration, model, status, passenger count, and computed age (`CompanyFleetAircraftPrinter`).

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given a collaborator of company TP, when listing the full fleet, then only TP aircraft are returned (active and decommissioned). |
| AC2 | Given fleet contains models A320 and B737, when filtering by A320, then only A320 aircraft are returned. |
| AC3 | Given two manufacturers in the fleet, when filtering by one maker, then only aircraft of models from that maker are returned. |
| AC4 | Given aircraft with 100, 150, and 180 seats, when filtering capacity “greater than 140”, then only aircraft with more than 140 seats are returned. |
| AC5 | Given aircraft manufactured in 2008 and 2018, when filtering age “less than 15 years” (with current year 2026), then only the younger aircraft match. |
| AC6 | Given no aircraft match the filter, when listing, then an empty result is shown with an appropriate message. |
