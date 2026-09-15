## REQUIREMENTS

### Functional

1. **R1 — Register weather data**  
   An authorised user can register weather data for a selected air control area.

2. **R2 — Area existence**  
   The selected air control area must exist.

3. **R3 — Section bounds**  
   The weather section polygon must be inside the selected air control area.

4. **R4 — Meteorological and time validation**  
   Temperature, humidity, pressure, wind direction/speed, and date interval must satisfy domain validation rules.

5. **R5 — Authorisation**  
   Only **Admin** or **Backoffice Operator** may invoke `RegisterWeatherDataController.registerWeatherData`.

### Non-functional

6. **R6 — Persistence**  
   Weather data is persisted through `WeatherDataRepository`.

---

## ACCEPTANCE CRITERIA

| ID | Criterion (English) |
|----|---------------------|
| AC1 | Given a valid area and valid weather inputs, when registration is executed, then weather data is persisted and linked to that area. |
| AC2 | Given an unknown area code, when registration is executed, then the operation fails and nothing is persisted. |
| AC3 | Given a weather section outside the area boundary, when registration is executed, then `WeatherSectionOutOfBoundsException` is raised. |
| AC4 | Given invalid weather/date values, when registration is executed, then domain validation fails before save. |
| AC5 | Given a user who is neither Admin nor Backoffice Operator, when the controller method is invoked, then authorisation fails. |
