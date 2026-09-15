# US044 — Tests

## Automated

| Test class | Scope |
|------------|-------|
| `ProtocolFrameTest` | Frame round-trip (regression) |
| `WeatherRegisterPayloadTest` | US041 payload encode/decode + validation |
| `WeatherConsultPayloadTest` | US043 payload encode/decode + validation |
| `WeatherCsvPayloadTest` | US042 file transport encode/decode |
| `LoginCredentialsParserTest` | `WEATHER` token → `WEATHER_PERSON` |
| `WeatherCommandHandlerTest` | Session/role guards, opcodes 50–53, error paths |
| `WeatherResponseFormatterTest` | Area and weather line formatting |
| `RegisterWeatherDataControllerTest` | US041 business rules (existing) |
| `BulkWeatherDataControllerTest` | US042 / US043 business rules (existing) |

Run:

```bash
cd aisafe.base
mvn -pl aisafe.rcomp.protocol,aisafe.rcomp.server,aisafe.core test
```

### Unit tests — LoginCredentialsParserTest

| Test | Scenario | Expected |
|------|----------|----------|
| `parseExplicitWeather` | `weather;Password1;WEATHER` | `WEATHER_PERSON` role |
| `parseRejectsUnknownRoleToken` | `user;pass;ADMIN` | empty / rejected |

### Unit tests — WeatherCommandHandlerTest

| Test | Scenario | Expected |
|------|----------|----------|
| `ensureRequiresSession` | No login | `UNAUTHORIZED` |
| `ensureRequiresWeatherRole` | Non-Weather session | `FORBIDDEN` |
| `ensureListAreasReturnsCodes` | Weather session + opcode 50 | `OK`, area codes in body |
| `ensureRegisterWeatherSucceeds` | Valid `WeatherRegisterPayload` | `OK\|...` |
| `ensureConsultByDayReturnsEmptyList` | Valid `WeatherConsultPayload` | `OK` |
| `ensureConsultByDayReturnsRecords` | Valid `WeatherConsultPayload` | `OK` with weather lines |
| `ensureRegisterFailureReturnsInternalError` | Service returns `RegisterWeatherResult(ERROR)` | `INTERNAL_ERROR` |
| `ensureBulkImportFailureReturnsInternalError` | Service returns `BulkImportWeatherResult(ERROR)` | `INTERNAL_ERROR` |

---

## Manual

### Setup

1. **Server (vs353 or local):** `cd aisafe.base && ./run-rcomp-server.sh` (PostgreSQL + bootstrap).

   **Local in-memory (quick test, no PostgreSQL):**

   ```bash
   export AISAFE_CONFIG=./application-inmemory.properties
   ./run-rcomp-server.sh
   ```

   Server prints `In-memory persistence: loading demo data (bootstrap)...` then `RCOMP TCP server listening on port 2225`.

2. **Client:** `cd aisafe.base && ./run-remote-app.sh`

   Local loopback:

   ```bash
   export AISAFE_RCOMP_LOCAL=1
   ./run-remote-app.sh
   ```

   **Windows (PowerShell)** — client to local server:

   ```powershell
   cd aisafe.base
   mvn -q -pl aisafe.rcomp.tcpclient dependency:build-classpath "-Dmdep.outputFile=target/cp.txt"
   $cp = Get-Content "aisafe.rcomp.tcpclient\target\cp.txt" -Raw
   $jar = "aisafe.rcomp.tcpclient\target\aisafe.rcomp.tcpclient-1.0.0-SNAPSHOT.jar"
   java "-DAISAFE_RCOMP_HOST=127.0.0.1" "-DAISAFE_RCOMP_TCP_PORT=2225" -cp "$cp;$jar" eapli.aisafe.rcomp.tcpclient.RemoteClientApp
   ```

3. Ensure bootstrap created weather user (`weather` / `Password1`).

### Login and authorization

| Step | Expected |
|------|----------|
| Profile **3**, login `weather` / `Password1` | `code=11` LOGIN SUCCESSFUL |
| Profile **3**, login `atcc1` / `password123` | `code=-11` LOGIN FAILED |
| Profile **1**, login `weather` / `Password1` | `code=-11` LOGIN FAILED (wrong profile token) |
| Send Weather opcode without login | `code=-20` UNAUTHORIZED |
| Login as ATCC, send opcode 51 via raw client | `code=-21` FORBIDDEN |
| Login as Pilot, send opcode 52 via raw client | `code=-21` FORBIDDEN |

### Register weather (US041)

| Step | Expected |
|------|----------|
| Menu option 1 → list areas (opcode 50) | Area codes with boundary coordinates |
| Register valid weather section (opcode 51) | Success message; record persisted |
| Register with coordinates outside area | `BAD_REQUEST` / error message |
| Register with invalid date format | `BAD_REQUEST` |

### Bulk import (US042)

| Step | Expected |
|------|----------|
| Place `.csv` in `data_weather/` | File listed in remote UI |
| Import valid CSV (opcode 52) | `Imported N weather record(s).` |
| Import unsupported file | `BAD_REQUEST` |

### Consult by day (US043)

| Step | Expected |
|------|----------|
| Select area + day `dd-MM-yyyy` (opcode 53) | Table of matching records |
| Consult day with no data | Empty result message |
| Consult invalid date | Client-side format error or `BAD_REQUEST` |

### Logout

| Step | Expected |
|------|----------|
| Menu option 98 (logout) | `code=13` LOGOUT SUCCESSFUL |

### US090

| Step | Expected |
|------|----------|
| Successful Weather login | UDP event with service `US44` |
| `REGISTER_WEATHER` / `BULK_IMPORT` / `CONSULT_BY_DAY` | Operation name in UDP payload |
| Failed login | UDP event logged |
| Logout / disconnect | UDP event logged |

---

## Coverage (JaCoCo, line)

Target: **≥ 90%** on US044 RCOMP code (`aisafe.rcomp.protocol` Weather payloads + `aisafe.rcomp.server` weather package + `LoginCredentialsParser`).

```bash
cd aisafe.base/aisafe.rcomp.protocol && mvn test jacoco:report
cd ../aisafe.rcomp.server && mvn test jacoco:report
```

Reports: `target/site/jacoco/index.html` per module.

| Class | Line coverage |
|-------|---------------|
| `WeatherRegisterPayload` | ≥ 96% |
| `WeatherConsultPayload` | ≥ 93% |
| `WeatherCsvPayload` | ≥ 93% |
| `WeatherCommandHandler` | ≥ 94% |
| `WeatherResponseFormatter` | 100% |
| `LoginCredentialsParser` (WEATHER branch) | ≥ 94% |
| **Aggregate (US044 RCOMP classes)** | **≥ 95%** |

Not counted: TCP client UIs (`aisafe.rcomp.tcpclient`) — same policy as US086; domain logic covered in `aisafe.core` tests.

---

## Regression

- US078 ATCC client: profile 1 + opcodes 20–39 unchanged.
- US086 Pilot client: profile 2 + opcodes 40–49 unchanged.
- `ProtocolFrameTest` remains green.
- Domain/controller coverage ≥ 90% (JaCoCo, NFR03).
- TCP client JAR has no JDBC / persistence dependencies.

---

## Test data

- Weather user: `weather` / `Password1`
- ATCC user (negative): `atcc1` / `password123`
- Pilot user (negative): `pilot1` / `password123`
- CSV fixture: place under `aisafe.base/data_weather/`
- Air control areas: created by bootstrap (`AREA-0`, …)
