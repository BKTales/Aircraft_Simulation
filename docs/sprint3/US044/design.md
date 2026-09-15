# US044 — Design

## Goal

Same **AISafe Remote App** as US078/US086, with a **Weather** login profile exposing US041–US043 over TCP.

## Authentication

LOGIN payload (opcode 10):

| Form | Role |
|------|------|
| `username;password;WEATHER` | Weather Person (`WEATHER_PERSON`) |

Parser: [`LoginCredentialsParser`](../../../aisafe.base/aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/LoginCredentialsParser.java)

Session routing in [`ClientHandler`](../../../aisafe.base/aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/ClientHandler.java):

- Opcodes 20–39 → ATCC session only
- Opcodes 40–49 → Pilot session only
- Opcodes 50–59 → Weather session only
- Wrong block → `FORBIDDEN`

Remote access logging service id: `US44` (see `ClientHandler.resolveService()`).

## Weather opcodes (50–59)

| Code | Name | US | Payload | Success body |
|------|------|-----|---------|--------------|
| 50 | LIST_AREAS | helper | empty | `areaCode\|x1:y1,x2:y2,...` per line |
| 51 | REGISTER_WEATHER | US041 | see [weather-tcp.md](weather-tcp.md) | `OK\|id\|area\|start\|end\|temp\|hum\|press\|dir\|speed` |
| 52 | BULK_IMPORT | US042 | `fileName;base64Content` | `Imported N weather record(s).` |
| 53 | CONSULT_BY_DAY | US043 | `areaCode;dd-MM-yyyy` | weather lines (same pipe format as register) |

Weather record line format (`WeatherResponseFormatter.formatWeather`):

```
id|area|start|end|temp|hum|press|windDir|windSpeed
```

Dates: `dd-MM-yyyy HH:mm`.

## Server handler

[`WeatherCommandHandler`](../../../aisafe.base/aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/weather/WeatherCommandHandler.java) delegates to controllers and switches on Result outcomes (same pattern as `AtccCommandHandler.removePilot()`):

| Opcode | Controller method | Return type |
|--------|-------------------|-------------|
| 50–51 | `RegisterWeatherDataController.registerWeatherData(...)` | `RegisterWeatherResult` |
| 52 | `BulkWeatherDataController.importFromFile(path)` | `BulkImportWeatherResult` |
| 53 | `BulkWeatherDataController.consultWeatherDataForDay(...)` | `ConsultWeatherResult` |

Bulk import writes a temp file server-side (same pattern as Pilot flight-plan import) before calling `importFromFile(Path)`.

## Client

[`RemoteClientApp`](../../../aisafe.base/aisafe.rcomp.tcpclient/src/main/java/eapli/aisafe/rcomp/tcpclient/RemoteClientApp.java):

1. Choose profile **3 = Weather (US044)** before login
2. Weather → `CollaboratorMenus.weatherRemoteRootMenu()` with three items matching backoffice `MainMenu.buildWeatherData()`

Remote UIs (`aisafe.rcomp.tcpclient/.../presentation/weather/`):

| UI | US | Opcode(s) |
|----|-----|-----------|
| `RegisterWeatherRemoteUI` | US041 | 50, 51 |
| `BulkImportWeatherRemoteUI` | US042 | 52 |
| `ConsultWeatherRemoteUI` | US043 | 50, 53 |

## Network (DEI Cloud)

| Component | Internal (vs353) | External (workstation) |
|-----------|------------------|------------------------|
| TCP server | `2225` | `vsgate-s2.dei.isep.ipp.pt:10353` |

Local loopback: `AISAFE_RCOMP_HOST=127.0.0.1`, `AISAFE_RCOMP_TCP_PORT=2225`.

## Test users

| Profile | User | Password |
|---------|------|----------|
| Weather | `weather` | `Password1` |

Negative tests: `atcc1` / `password123` (ATCC), `pilot1` / `password123` (Pilot) must fail Weather login or receive `FORBIDDEN` on opcodes 50–59.
