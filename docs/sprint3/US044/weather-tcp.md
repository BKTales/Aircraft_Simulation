# US044 — Weather TCP contracts (US041–US043)

Domain rules: [US041](../../sprint2/US041/requirements.md), [US042](../US042/requirements.md), [US043](../US043/requirements.md).

## Helper opcode — LIST_AREAS

Opcode: `WeatherOpcodes.LIST_AREAS` (50)

### Request

Empty payload.

### Success response

One line per air control area:

```
areaCode|x1:y1,x2:y2,x3:y3
```

Example:

```
AREA-0|38.0:-9.0,39.0:-9.0,38.0:-8.0
```

## REGISTER_WEATHER (US041)

Opcode: `WeatherOpcodes.REGISTER_WEATHER` (51)

### Request payload (semicolon-separated)

```
areaCode;temp;hum;press;dir;speed;start;end;x1:y1,x2:y2,...
```

| Field | Example | Notes |
|-------|---------|--------|
| areaCode | `AREA-0` | Must exist |
| temp | `20.5` | °C |
| hum | `50` | % |
| press | `1013` | hPa |
| dir | `180` | 0–360° |
| speed | `10` | m/s |
| start | `01-06-2026 10:00` | `dd-MM-yyyy HH:mm` |
| end | `01-06-2026 12:00` | After start |
| coordinates | `1:1,2:1,1:2` | Min. 3 points; inside area boundary |

Codec: `WeatherRegisterPayload.encode/decode`.

### Server handler

```java
registerController.registerWeatherData(areaCode, coordinates, temp, direction, speed, hum, press, start, end);
```

### Success response

```
OK|id|area|start|end|temp|hum|press|dir|speed
```

### Failure

- Invalid payload → `BAD_REQUEST` (-22) with message (protocol/payload parsing only)
- Business rejection mapped from `RegisterWeatherResult` / `BulkImportWeatherResult` / `ConsultWeatherResult`:

| Outcome | Response code |
|---------|---------------|
| `AREA_NOT_FOUND` | `NOT_FOUND` (-25) |
| `SECTION_OUT_OF_BOUNDS`, `INVALID_INPUT`, `INVALID_HUMIDITY`, `UNSUPPORTED_FORMAT` | `BAD_REQUEST` (-22) |
| `IO_ERROR`, `ERROR` | `INTERNAL_ERROR` (-27) |

`WeatherCommandHandler` switches on `result.outcome()` — no global `catch (Exception)` for business logic.

## BULK_IMPORT (US042)

Opcode: `WeatherOpcodes.BULK_IMPORT` (52)

### Request payload

```
fileName;base64Content
```

Same transport pattern as `PilotFlightPlanPayload.encodeFile` (US121).

Codec: `WeatherCsvPayload.encodeFile/decodeFile`.

### Server handler

1. Decode base64 → temp `.csv` file
2. `bulkController.importFromFile(path)`

### Success response

```
Imported 3 weather record(s).
```

### Failure

- Unsupported format → `BAD_REQUEST`
- IO / parse errors → `INTERNAL_ERROR`
- Per-row validation failures → `BAD_REQUEST` or `NOT_FOUND` (see outcome table above)

## CONSULT_BY_DAY (US043)

Opcode: `WeatherOpcodes.CONSULT_BY_DAY` (53)

### Request payload

```
areaCode;dd-MM-yyyy
```

Example: `AREA-0;01-06-2026`

Codec: `WeatherConsultPayload.encode/decode`.

### Server handler

```java
bulkController.consultWeatherDataForDay(areaCode, day.atStartOfDay());
```

### Success response

Zero or more lines (newline-separated):

```
7|AREA-0|01-06-2026 08:00|01-06-2026 12:00|18.0|55.0|1012.0|200|8.0
```

Empty payload = no records for that area/day.

## Response codes (shared)

| Code | Constant | Meaning |
|------|----------|---------|
| 0 | `OK` | Success |
| -11 | `FAILED_LOGIN` | Invalid user/password for requested role |
| -12 | `INVALID_CREDENTIALS` | Malformed login payload |
| -20 | `UNAUTHORIZED` | Command without login |
| -21 | `FORBIDDEN` | Wrong role for opcode block |
| -22 | `BAD_REQUEST` | Invalid payload or business rejection |
