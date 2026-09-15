# US042 — Tests

## Automated Tests

- `BulkWeatherDataControllerTest`
  - imports all reader records via service delegation
  - returns `IO_ERROR` outcome on reader failure
  - handles empty file payload
  - returns `UNSUPPORTED_FORMAT` outcome for unsupported extensions
  - constructor dependency guards
- `WeatherDataServiceTest`
  - verifies register failures map to `RegisterWeatherResult` outcomes
  - verifies day query returns `ConsultWeatherResult`
- `CsvWeatherDataBulkReaderTest`
  - parses data rows and skips CSV header
  - supports `.csv` extension
- `WeatherDataBulkReaderFactoryTest`
  - resolves CSV reader for `.csv` files
  - rejects unsupported extensions (e.g. `.xml`, `.txt`)

## Execution

```bash
cd aisafe.base
mvn -q -pl aisafe.core -am test
```

## Notes

- Import logic is covered at controller/reader/factory unit level.
- Domain validation during import reuses existing weather service/domain tests.
