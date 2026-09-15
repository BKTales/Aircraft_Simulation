# US044 — Analysis

## Goal

Weather Persons access weather data management remotely via a dedicated TCP client profile, without direct database access.

## Scope (Sprint 3)

| US | Feature | Server opcode | Core controller | State |
|----|---------|---------------|-----------------|-------|
| US041 | List air control areas (helper) | 50 | `RegisterWeatherDataController.availableAreas()` | Implemented |
| US041 | Register weather data | 51 | `RegisterWeatherDataController` | Implemented |
| US042 | Bulk import CSV | 52 | `BulkWeatherDataController.importFromFile()` | Implemented |
| US043 | Consult weather by day | 53 | `BulkWeatherDataController.consultWeatherDataForDay()` | Implemented |

## Architecture

- **Client:** `aisafe.rcomp.tcpclient` → `RemoteClientApp` (profile `WEATHER`, menu option 3)
- **Protocol:** `aisafe.rcomp.protocol` — shared framing; Weather opcodes block **50–59** (`WeatherOpcodes`)
- **Server:** `aisafe.rcomp.server` → `ClientHandler` (multi-role login + dispatch) + `WeatherCommandHandler`
- **Domain:** existing `aisafe.core` weather controllers; AuthZ via `AuthzRegistry` session after login

## Actors

- Authenticated user with role `WEATHER_PERSON` (`AISafeRoles.WEATHER_PERSON`)
- Bootstrap test user:
  - `weather` / `Password1` (`MasterUsersBootstrapper`)

## Authentication decisions

1. **Login:** client sends explicit role token: `username;password;WEATHER` (opcode 10). Parser maps `WEATHER` → `WEATHER_PERSON`.
2. **Session role:** `ClientHandler` stores `sessionRole` for the TCP connection lifetime.
3. **Command dispatch:**
   - Opcodes `20–39` → `AtccCommandHandler` (ATCC only)
   - Opcodes `40–49` → `PilotCommandHandler` (Pilot only)
   - Opcodes `50–59` → `WeatherCommandHandler` (Weather only)
4. **Cross-role access:** wrong role for opcode block → `ResponseCodes.FORBIDDEN`.

## Local development (Windows / in-memory)

`RcompTcpServerApp` seeds demo data via `Bootstrapper` when `application-inmemory.properties` is active (`AISAFE_CONFIG`), so a separate bootstrap JVM is not required for quick local TCP tests.

## Traceability to implementation

- `aisafe.rcomp.server/.../ClientHandler.java` — login + dispatch + `US44` logging
- `aisafe.rcomp.server/.../weather/WeatherCommandHandler.java` — Weather opcodes
- `aisafe.rcomp.server/.../weather/WeatherResponseFormatter.java` — response lines
- `aisafe.rcomp.server/.../LoginCredentialsParser.java` — `WEATHER` token
- `aisafe.rcomp.protocol/.../WeatherOpcodes.java` — opcode constants
- `aisafe.rcomp.protocol/.../WeatherRegisterPayload.java` — US041 payload codec
- `aisafe.rcomp.protocol/.../WeatherConsultPayload.java` — US043 payload codec
- `aisafe.rcomp.protocol/.../WeatherCsvPayload.java` — US042 file transport
- `aisafe.rcomp.tcpclient/.../RemoteClientApp.java` — unified remote app
- `aisafe.rcomp.tcpclient/.../RemoteProfile.java` — `WEATHER` profile
- `aisafe.rcomp.tcpclient/.../presentation/weather/*` — remote UIs (US041–043)
- `aisafe.app.backoffice.console/.../collaborator/WeatherMenuActions.java` — shared menu contract
- `aisafe.core/.../RegisterWeatherDataController.java` — US041
- `aisafe.core/.../BulkWeatherDataController.java` — US042 / US043
