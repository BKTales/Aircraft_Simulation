## ANALYSIS

**Client Clarification / Product Owner Session**

Weather data is registered for one selected air control area. The weather section polygon must be fully inside that area boundary. Registration requires a start and end date-time interval and meteorological values (temperature, wind direction/speed, humidity, pressure). Authorised roles: **Weather person**.

---

## BUSINESS RULES

* Air control area must exist
* Weather section polygon must be valid and contained in the selected area
* Date interval must be valid (`end >= start`)
* Humidity, pressure, temperature, wind direction and wind speed must satisfy domain invariants
* On success, weather data is persisted and linked to the selected area
* Operation is protected by role-based authorisation in controller level

---

## TEST COVERAGE

Automated tests under `aisafe.core/src/test/java/eapli/aisafe/weatherdata/`:

* Application: `RegisterWeatherDataControllerTest`, `WeatherDataServiceTest`
* Result mapping: `RegisterWeatherResult`, `ConsultWeatherResult`, `BulkImportWeatherResult`
* Domain support: `WeatherDataTest`, `WeatherDateTest`, `WeatherSectionTest`, `HumidityTest`, `PressureTest`, `TemperatureTest`, `WindDataTest`, `WeatherComplianceServiceTest`, `GeographicCoordsTest`

---

## UNIT TESTS (service / controller scenarios)

* `EnsureRegisterWeatherRequiresAuthorization`
* `EnsureRegisterWeatherReturnsAreaNotFoundOutcome`
* `EnsureRegisterWeatherReturnsSectionOutOfBoundsOutcome`
* `EnsureAirControlAreaMustExist`
* `EnsureWeatherSectionMustBeInsideArea`
* `EnsureDateIntervalValidationIsApplied`
* `EnsureDomainValueValidationIsApplied`
* `EnsureValidWeatherDataIsPersisted`
