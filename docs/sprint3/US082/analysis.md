# US082 — Analysis

## Goal

Attach weather data to a flight and invalidate any prior simulation test when conditions change.

## Scope (Sprint 3)

- Target module: `aisafe.core`
- Entry point: `InsertWeatherInFlightController#attachWeatherToFlight`
- Actor/role: `PILOT` (backoffice and US086 remote client)
- Preconditions:
  - flight exists
  - flight has a flight plan
  - weather data exists

## Functional decisions

- Weather assignment is a domain operation on the aggregate root:
  - `Flight#assignWeatherData(WeatherData)` — JPA `@ManyToOne` on `FLIGHT.WEATHER_DATA_ID`
- **Void prior test:** `assignWeatherData` calls `FlightPlan.resetTestOnWeatherChange()`, which sets status to `DRAFT` when the plan was `SIM_APPROVED` or `SIM_REJECTED` (Sprint 3 planning + US085 R8).
- Controller performs AuthZ, identity lookup, and persistence save.

## Traceability to implementation

- `aisafe.core/src/main/java/eapli/aisafe/flightmanagement/application/InsertWeatherInFlightController.java`
- `aisafe.core/src/main/java/eapli/aisafe/flightmanagement/domain/Flight.java`
- `aisafe.core/src/main/java/eapli/aisafe/flightmanagement/domain/FlightPlan.java`
