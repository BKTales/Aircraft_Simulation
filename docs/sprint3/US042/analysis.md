# US042 — Analysis

## Goal

Import weather data in bulk from files using an extensible Strategy + Factory model, so new formats (CSV today, XML later) can be supported without changing controller orchestration.

## Scope (Sprint 3)

- Target module: `aisafe.core`
- Main entry point: `BulkWeatherDataController#importFromFile`
- Actor/role: `WEATHER_PERSON`
- Input: weather data file path (CSV supported)
- Output: persisted weather sections (`WeatherData`) list

## Functional decisions

- **Strategy** contract and DTO:
  - `WeatherDataBulkReader`
  - `WeatherDataBulkRecord`
  - `CsvWeatherDataBulkReader` (CSV implementation)
- **Factory** for reader selection:
  - `WeatherDataBulkReaderFactory`
  - `UnsupportedWeatherDataFormatException` when no reader supports the file
- Controller performs authorization and delegates to `WeatherDataService.importFromFile`.
- Controller returns `BulkImportWeatherResult`; UI switches on outcomes.

## Traceability to implementation

- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/BulkWeatherDataController.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/bulk/WeatherDataBulkReader.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/bulk/WeatherDataBulkRecord.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/bulk/WeatherDataBulkReaderFactory.java`
- `aisafe.core/src/main/java/eapli/aisafe/weatherdata/application/bulk/csv/CsvWeatherDataBulkReader.java`
