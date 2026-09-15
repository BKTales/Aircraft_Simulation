# US042 — Design

## Flow

```mermaid
sequenceDiagram
    participant User as WeatherPerson
    participant Ctrl as BulkWeatherDataController
    participant Service as WeatherDataService
    participant Factory as WeatherDataBulkReaderFactory
    participant Reader as WeatherDataBulkReader
    participant Repo as WeatherDataRepository

    User->>Ctrl: importFromFile(path)
    Ctrl->>Ctrl: ensure role WEATHER_PERSON
    Ctrl->>Service: importFromFile(path, factory, txCtx)
    Service->>Factory: forFile(path)
    Factory-->>Service: WeatherDataBulkReader
    Service->>Reader: read(path)
    Reader-->>Service: List<WeatherDataBulkRecord>
    loop each record
        Service->>Service: doRegisterWeatherData(...)
        Service->>Repo: save(new WeatherData)
        Repo-->>Service: WeatherData
    end
    Service-->>Ctrl: BulkImportWeatherResult
    Ctrl-->>User: BulkImportWeatherResult
```

## Key design points

- **Strategy** (`WeatherDataBulkReader`): one implementation per file format (e.g. `CsvWeatherDataBulkReader`). New formats add a class without changing controller orchestration.
- **Factory** (`WeatherDataBulkReaderFactory`): selects the reader by file extension via `supports(Path)`.
- **DTO** (`WeatherDataBulkRecord`): format-neutral record passed from reader to service.
- Domain validation remains centralized in `WeatherDataService.doRegisterWeatherData`.
- Bulk import runs inside a **single transaction** — any row failure rolls back the entire import.
- Controller returns `BulkImportWeatherResult`; UI switches on `outcome()` (no `catch (Exception)`).
- XML and other formats can be added later by implementing `WeatherDataBulkReader` and registering it in the factory.

## Result types

`BulkImportWeatherResult` outcomes: `SUCCESS`, `UNSUPPORTED_FORMAT`, `IO_ERROR`, `AREA_NOT_FOUND`, `SECTION_OUT_OF_BOUNDS`, `INVALID_INPUT`, `INVALID_HUMIDITY`, `ERROR`.
