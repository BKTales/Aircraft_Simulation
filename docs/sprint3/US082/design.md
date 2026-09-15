# US082 — Design

## Flow

```mermaid
sequenceDiagram
    participant User as Pilot
    participant Ctrl as InsertWeatherInFlightController
    participant FlightRepo as FlightRepository
    participant WeatherRepo as WeatherDataRepository
    participant Flight as FlightAggregate
    participant Plan as FlightPlan
    User->>Ctrl: attachWeatherToFlight(designator, weatherId)
    Ctrl->>Ctrl: ensure role PILOT
    Ctrl->>FlightRepo: findByDesignator
    Ctrl->>WeatherRepo: ofIdentity(weatherId)
    Ctrl->>Flight: assignWeatherData(weatherData)
    Flight->>Plan: resetTestOnWeatherChange()
    Note over Plan: SIM_APPROVED/SIM_REJECTED → DRAFT
    Flight->>Flight: weatherData = weatherData
    Ctrl->>FlightRepo: save(flight)
```

## Domain rules enforced

- No weather assignment without flight plan.
- Weather reference cannot be null.
- Attaching weather after a test voids the test result: `SIM_APPROVED` / `SIM_REJECTED` → `DRAFT`.
- `DRAFT` and `SUBMITTED_FOR_SIMULATION` statuses are unchanged.

## Remote access (US086)

- TCP opcode `44` (`ATTACH_WEATHER`) in `PilotCommandHandler` delegates to the same controller.
