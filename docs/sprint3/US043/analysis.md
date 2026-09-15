# US043 — Analysis

## Goal

Consult weather data by day and air control area.

## Scope (Sprint 3)

- Target module: `aisafe.core`
- Query constraints:
  - filter by area code
  - intersect with selected day window
- Actor/role: `WEATHER_PERSON`

## Functional decisions

- Day query is implemented as interval intersection:
  - start: `00:00:00`
  - end: `23:59:59.999999999`
- New service endpoint:
  - `WeatherDataService#consultWeatherDataForDay`
- Exposed via:
  - `BulkWeatherDataController#consultWeatherDataForDay`
- Repository-level support added in both JPA and in-memory implementations.

## Traceability to implementation

- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/WeatherDataService.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/BulkWeatherDataController.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/repositories/WeatherDataRepository.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/repositories/JpaWeatherDataRepository.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/repositories/InMemoryWeatherDataRepository.java`

