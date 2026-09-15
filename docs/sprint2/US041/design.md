## DESIGN

* Follow the standard layered application architecture

**Domain classes:**
`WeatherData` (aggregate root), `WeatherDate`, `WeatherSection`, `Temperature`, `Humidity`, `Pressure`, `WindData`, `WindDataDirection`, `WindDataSpeed`

`WeatherData` embeds meteorological value objects and references one `AirControlArea`.

Validation flow in service:

* Resolve area using `AirControlAreaRepository.ofIdentity(AreaCode)`
* Build section polygon from coordinates
* Validate polygon inclusion with `WeatherComplianceService`
* Build domain value objects and aggregate
* Persist through `WeatherDataRepository.save`

**Console UI:** `RegisterWeatherDataUI`

**Controller:** `RegisterWeatherDataController`

**Service:** `WeatherDataService` (`@ApplicationService`)

**Repositories:** `AirControlAreaRepository`, `WeatherDataRepository`

## Result types and error handling

| Operation | Controller return | Service method | Outcomes |
|-----------|-------------------|----------------|----------|
| Register (US041) | `RegisterWeatherResult` | `WeatherDataService.registerWeatherData(..., txCtx)` | `SUCCESS`, `AREA_NOT_FOUND`, `SECTION_OUT_OF_BOUNDS`, `INVALID_INPUT`, `INVALID_HUMIDITY`, `ERROR` |

* Domain/value objects continue to **throw** on invalid input.
* `WeatherDataService` catches at the transaction boundary and maps exceptions to `RegisterWeatherResult` via `mapRegisterException`.
* `RegisterWeatherDataUI` switches on `result.outcome()` — no `catch (Exception)` around the controller call.
* Input date parsing errors in the UI (`DateTimeParseException`) remain UI-local.

**Transactional context:** `RegisterWeatherDataController` creates `TransactionalContext` and passes transactional repositories to the service (same pattern as `AddPilotCollaboratorController`).
