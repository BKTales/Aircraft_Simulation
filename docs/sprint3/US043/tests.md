# US043 — Tests

## Automated Tests

- `WeatherDataServiceTest`
  - verifies day query returns `ConsultWeatherResult` with repository results
- `BulkWeatherDataControllerTest`
  - verifies authorization and `ConsultWeatherResult` delegation for day consultation

## Execution

```bash
cd aisafe.base
mvn -q -pl aisafe.core -am test
```

## Notes

- JPA and in-memory repositories now share the same overlap semantics for this query.

