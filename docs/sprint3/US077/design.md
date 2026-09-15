## DESIGN

* Follow the standard layered application architecture (same pattern as US071 Decommission Aircraft and US075–076 pilot roster).

**Alignment with implemented model (US075/US076):**

* Roster aggregate: `PilotUser` (not `CompanyCollaboratorUser`).
* Active/inactive: `SystemUser.active` via `SystemUser.deactivate(Calendar)` — there is **no** `CollaboratorStatus` value object.
* Deactivate input: pilot **email** (`String` in controller, `EmailAddress` in service).
* Deactivate output: **`DeactivatePilotResult`** (`outcome` enum + optional `PilotUser`), not a DTO.

---

## DTOs and mapping (as implemented)

US077 **does not** introduce new DTO classes or mapper classes. It reuses US076.

| Operation | Controller return type | How data reaches the UI |
|-----------|------------------------|-------------------------|
| List active roster | `Iterable<ResponsePilotCollaboratorDTO>` | See below |
| Deactivate pilot | `DeactivatePilotResult` | UI switches on `outcome()` to display success/failure message; no pilot data shown |

### List — `ResponsePilotCollaboratorDTO`

* **DTO class:** [`ResponsePilotCollaboratorDTO`](../../../aisafe.base/aisafe.core/src/main/java/eapli/aisafe/companycollaboratormanagment/dto/ResponsePilotCollaboratorDTO.java) (`@DTO`, Lombok) — created in US075/US076.
* **Mapping:** on the aggregate, not in application service. `PilotUser` implements `DTOable<ResponsePilotCollaboratorDTO>` and defines `toDTO()`:

```java
@Override
public ResponsePilotCollaboratorDTO toDTO() {
    return new ResponsePilotCollaboratorDTO(
        systemUser().email().toString(),
        systemUser().name().firstName(),
        systemUser().name().lastName(),
        phoneNumber.toString(),
        securityClearance.skillsAssessmentDate().toString(),
        securityClearance.expiryDate().toString(),
        pilotCertifications.size());
}
```

* **Who instantiates the DTO:** `PilotUser.toDTO()` executes `new ResponsePilotCollaboratorDTO(...)` on the aggregate (see sequence diagram: `PilotUser` → `<<create>>` → `ResponsePilotCollaboratorDTO`).
* **Who calls `toDTO()`:** [`PilotCollaboratorUserService.findActivePilotsByCompany`](../../../aisafe.base/aisafe.core/src/main/java/eapli/aisafe/companycollaboratormanagment/application/PilotCollaboratorUserService.java) (US076), reused by US077 controller:

```java
for (PilotUser pilot : pilotRepository.findPilotByCompanyAndActive(company)) {
    activePilotsDTOs.add(pilot.toDTO());
}
```

* **No** `RemovePilotCollaboratorService` list method, **no** `*DTOMapper` / `*RepresentationBuilder` for this use case (same convention as US076 [`ListPilotUsersController`](../../../aisafe.base/aisafe.core/src/main/java/eapli/aisafe/companycollaboratormanagment/application/ListPilotUsersController.java)).

**DDD note:** mapping for **read/list** stays on `PilotUser.toDTO()` (presentation boundary). `RemovePilotCollaboratorService` works only with domain types and repositories — it does not create or map DTOs.

### Deactivate — `DeactivatePilotResult`

* Controller parses `String emailRaw` → `EmailAddress.valueOf(...)`.
* Service loads `PilotUser` with **pessimistic write lock**, applies business rules, and returns a `DeactivatePilotResult` (never throws business exceptions).
* `DeactivatePilotResult.outcome()` is one of: `SUCCESS`, `NOT_FOUND`, `NOT_IN_ROSTER`, `ALREADY_INACTIVE`, `HAS_ACTIVE_FLIGHTS`.
* On `SUCCESS`, `result.pilot()` holds the persisted `PilotUser` (available for logging/auditing; UI does not display it).

---

## Domain

`PilotUser` extends `AISafeUser`, implements `AggregateRoot<AISafeUserId>`, `DTOable<ResponsePilotCollaboratorDTO>`

* Method added:
    - `deactivateFromRoster(Calendar when)`: calls `systemUser().deactivate(when)`; throws `IllegalStateException` if already inactive
* Existing: `toDTO()` for list (US076/US077 UI)
* Existing fields: `airTransportCompany`, `systemUser`

---

## JPA / repository contracts

`PilotUserRepository` (existing + new):

* `findPilotByCompanyAndActive(AirTransportCompany company)` — US076 / US077 list
* `findByEmail(EmailAddress email)`
* `findByEmailWithLock(EmailAddress email)` — same lookup as `findByEmail` within a transaction (in-memory/JPA impl)

`FlightRepository` (new):

* `existsActiveFlightForPilot(SystemUser pilotSystemUser, LocalDateTime asOf): boolean` — pending flight with plan in `DRAFT` / `SUBMITTED_FOR_SIMULATION` / `SIM_APPROVED` and `schedule.scheduledDeparture >= asOf`

**Note:** `Flight.pilot` is typed as `PilotUser` (see `Flight.java` field `private PilotUser pilot`). The active-flight guard checks `f.pilot.systemUser()` against the roster pilot's `SystemUser` (R3).

---

## Application exceptions

`companycollaboratormanagment.application`: `PilotNotFoundException`, `PilotNotInCompanyRosterException`, `PilotAlreadyInactiveException`, `PilotHasActiveFlightPlansException`

---

## Controller: `RemovePilotCollaboratorController` (`@UseCaseController`)

* `listActivePilotsForCompany(): Iterable<ResponsePilotCollaboratorDTO>`
    - Auth: `AIR_TRANSPORT_COMPANY_COLLABORATOR`
    - `currentCompany()` via `CompanyCollaboratorUserService.findATCC` + session username
    - Delegates to **`PilotCollaboratorUserService.findActivePilotsByCompany`** (not `RemovePilotCollaboratorService`)

* `deactivatePilot(String emailRaw): DeactivatePilotResult`
    - Auth + `currentCompany()` as above
    - Delegates to **`RemovePilotCollaboratorService.deactivatePilot(email, company, txCtx)`**
    - Returns `DeactivatePilotResult` (outcome enum + optional `PilotUser`); UI switches on `outcome()`

---

## Service: `RemovePilotCollaboratorService`

* `deactivatePilot(EmailAddress email, AirTransportCompany company, TransactionalContext txCtx)` → delegates with `LocalDateTime.now()`
* `deactivatePilot(EmailAddress email, AirTransportCompany company, LocalDateTime referenceTime, TransactionalContext txCtx)` → `DeactivatePilotResult`:
    1. Optional `txCtx.beginTransaction()`
    2. `findByEmailWithLock` (pessimistic write) → `NOT_FOUND` result if absent
    3. Company check → `NOT_IN_ROSTER` result if mismatch
    4. Active check → `ALREADY_INACTIVE` result if already inactive
    5. `existsActiveFlightForPilot(systemUser, referenceTime)` → `HAS_ACTIVE_FLIGHTS` result
    6. `deactivateFromRoster` + `save` → `SUCCESS` result with persisted `PilotUser`
    7. Optional `commit` / `close`

**No DTO logic in this service.**

---

## UI

* [`RemovePilotCollaboratorUI`](../../../aisafe.base/aisafe.app.backoffice.console/src/main/java/eapli/aisafe/app/backoffice/console/presentation/companycollaborator/RemovePilotCollaboratorUI.java) + `RemovePilotCollaboratorAction` — menu item 6 under Pilot Collaborators (`MainMenu`)
* List: `controller.listActivePilotsForCompany()` → `SelectWidget` on `ResponsePilotCollaboratorDTO` (`PilotCollaboratorUserDTOPrinter`)
* Deactivate: `controller.deactivatePilot(chosen.getEmail())` — success message only (returned `PilotUser` not shown)

---

## RCOMP (US078)

* `LIST_PILOT_ROSTER` (32): `ListPilotUsersController` / `formatPilot`
* `REMOVE_PILOT` (29): payload = pilot email → `RemovePilotCollaboratorController.deactivatePilot` → `Pilot deactivated.`
* TCP client: `removePilot()` lists roster then prompts email (`RemoteClientApp`)

---

## Reference implementations

* US076: list + `PilotUser.toDTO()` + `ResponsePilotCollaboratorDTO`
* US071: deactivate returns domain entity (`Aircraft`)
* eCafeteria: `DeactivateUserController` → `SystemUser` (no business DTO)
