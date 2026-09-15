# US043 — Requirements

## User Story

As a Weather Person, I want to consult weather data by day and air control area, so that I can retrieve relevant weather sections for operational planning.

## Requirements

### Functional

- The system shall query weather data by:
  - air control area code
  - selected day
- The day query shall include weather intervals that overlap the selected day window.
- Only authenticated users with `WEATHER_PERSON` role shall execute this operation.

## Acceptance Criteria

- Querying by area + day returns only matching weather entries.
- Entries with interval overlap against the requested day are included.
- Empty result sets are returned safely when no data matches.
- Unauthorized requests are blocked.

