# RCOMP — Protocol Reference

← [README](README.md)

---

## 1. TCP frame format

Every message (request or response) uses the same binary envelope:

```
 0        1        2..5       6..N
┌────────┬────────┬──────────┬──────────────────┐
│version │ opcode │  length  │    payload       │
│1 byte  │ 1 byte │  4 bytes │  length bytes    │
│(=0x01) │ signed │ big-end. │  UTF-8 string    │
└────────┴────────┴──────────┴──────────────────┘
```

| Field | Type | Notes |
|-------|------|-------|
| version | `byte` | Always `1` (`ProtocolConstants.VERSION`) |
| opcode | `byte` | Signed; negative values are response codes |
| length | `int` (big-endian) | Byte length of the payload; 0 for empty payload |
| payload | UTF-8 string | Content depends on opcode; may be empty |

Frame is implemented in `ProtocolFrame` (`aisafe.rcomp.protocol`).

**Client sends** a frame with a domain opcode and its payload.  
**Server responds** with a frame whose opcode is a `ResponseCode` (see §3) and whose payload is the result body.

---

## 2. Common opcodes (login / session)

These are handled by `ClientHandler` directly, before any role check.

| Opcode | Constant | Direction | Required role | Payload (request) | Success payload |
|--------|----------|-----------|---------------|-------------------|-----------------|
| 10 | `ATCC_LOGIN` | client→server | `AIR_TRANSPORT_COMPANY_COLLABORATOR` | `username\npassword\n` | `"Logged in as ATCC."` |
| 11 | `PILOT_LOGIN` | client→server | `PILOT` | `username\npassword\n` | `"Logged in as Pilot."` |
| 12 | `WEATHER_LOGIN` | client→server | `WEATHER_PERSON` | `username\npassword\n` | `"Logged in as Weather."` |
| 13 | `LOGOUT` | client→server | any authenticated | empty | `"Goodbye."` |

> Note: opcode constants for login/logout live in `ClientHandler` rather than a separate class. They are `static final byte` fields in that class.

Login failure responses:

| Situation | Response code | Payload |
|-----------|---------------|---------|
| Wrong password or unknown user | `-11` (`FAILED_LOGIN`) | `"Authentication failed."` |
| Malformed payload (missing `\n`) | `-12` (`INVALID_CREDENTIALS`) | `"Malformed login payload."` |

---

## 3. Response codes

Defined in `ResponseCodes` (`aisafe.rcomp.protocol`). Always sent as the `opcode` field of a response frame.

| Value | Constant | Meaning |
|-------|----------|---------|
| `0` | `OK` | Request succeeded; payload is the result |
| `-11` | `FAILED_LOGIN` | Invalid username/password for the requested role |
| `-12` | `INVALID_CREDENTIALS` | Malformed login payload (missing separator, wrong format) |
| `-20` | `UNAUTHORIZED` | Opcode sent before login |
| `-21` | `FORBIDDEN` | Authenticated, but wrong role for this opcode block |
| `-22` | `BAD_REQUEST` | Invalid payload, unknown opcode, or business rule rejection |
| `-23` | `NOT_IMPLEMENTED` | Feature defined but not yet available on this server |
| `-24` | `NEEDS_CONFIRMATION` | Server requires the client to re-send with `confirmReplace=true` |
| `-25` | `NOT_FOUND` | Requested resource does not exist |
| `-26` | `CONFLICT` | State conflict (e.g. pilot already inactive, already in roster) |
| `-27` | `INTERNAL_ERROR` | Unhandled runtime exception; check server log |

---

## 4. ATCC opcodes (20–39) — US078

Requires `ATCC_LOGIN` (opcode 10) on the connection. Wrong role → `FORBIDDEN (-21)`.  
Detailed design: [`US078/design.md`](../US078/design.md).

### 4.1 Fleet management (US070–072)

| Opcode | Constant | Request payload | Success payload |
|--------|----------|-----------------|-----------------|
| 20 | `LIST_AIRCRAFT_MODELS` | empty | One model per line: `modelId\|manufacturer\|seats\|...` |
| 21 | `REGISTER_AIRCRAFT` | `registration;modelId;engineModelId;economy;business;first;country;crew;year[;CERT:modelId;engineId;...]` | `OK\|registration\|model\|...` |
| 22 | `DECOMMISSION_AIRCRAFT` | `registration` | `OK\|registration\|...` |
| 23 | `LIST_ACTIVE_AIRCRAFT` | empty | One aircraft per line |
| 24 | `LIST_FLEET` | filter criteria (see §4.1.1) | One aircraft per line |
| 25 | `LIST_FLEET_MODELS` | empty | One model ID per line |
| 26 | `LIST_FLEET_MANUFACTURERS` | empty | One manufacturer ID per line |
| 34 | `LIST_AIRCRAFT_MODEL_IDS` | empty | One model ID per line |
| 35 | `LIST_CERTIFIED_ENGINES` | `aircraftModelId` | One engine model ID per line |

#### 4.1.1 LIST_FLEET filter payload

Semicolon-separated fields — all optional:

```
[modelId];[manufacturerId];[registration]
```

Empty field = no filter for that dimension. Example: `;;CS-TP01` (registration filter only).

#### 4.1.2 REGISTER_AIRCRAFT payload detail

```
registration;modelId;engineModelId;economy;business;first;country;crew;year[;CERT:<modelId>;<engineId>[;CERT:...]]
```

`CERT:` tokens are stripped and parsed separately by `AtccPayloadParser.parseCertifications`. Engine certifications are optional; aircraft can be registered without them.

### 4.2 Route management (US073–074)

| Opcode | Constant | Request payload | Success payload |
|--------|----------|-----------------|-----------------|
| 27 | `CREATE_ROUTE` | `routeName;originIata;destIata;REGULAR\|CHARTER;<type-specific>` | `OK\|routeName\|origin\|dest\|type\|...` |
| 28 | `DEACTIVATE_ROUTE` | `routeName;uuuu-MM-dd` | `OK\|routeName\|...` |
| 36 | `ROUTE_COMPANY_CONTEXT` | empty | Company name string (e.g. `TAP Air Portugal`) |
| 37 | `VALIDATE_ROUTE_NAME` | `<1–4 digit suffix>` | Candidate route name (e.g. `TP1001`) |
| 38 | `LIST_ROUTE_AIRPORTS` | empty | One airport per line: `IATA\|name\|...` |
| 39 | `LIST_ACTIVE_ROUTES` | empty | One route per line: `routeName\|origin\|dest\|type\|...` |

**CREATE_ROUTE type-specific fields:**

- REGULAR: `routeName;originIata;destIata;REGULAR;MONDAY,TUESDAY,...`
- CHARTER: `routeName;originIata;destIata;CHARTER;yyyy-MM-dd;yyyy-MM-dd` (departure date; arrival date)

### 4.3 Pilot roster (US075–077)

| Opcode | Constant | Request payload | Success payload |
|--------|----------|-----------------|-----------------|
| 29 | `REMOVE_PILOT` | pilot email | `"Pilot deactivated."` |
| 30 | `ADD_PILOT_NEW_USER` | `username;password;firstName;lastName;email;securityDate;phone[;CERT:<modelId>;<engineId>...]` | `"Pilot registered."` |
| 31 | `ADD_PILOT_EXISTING_USER` | _(see §7 — not implemented)_ | — |
| 32 | `LIST_PILOT_ROSTER` | empty | One pilot per line: `username\|firstName\|lastName\|email` |
| 33 | `LIST_ELIGIBLE_USERS` | _(see §7 — not implemented)_ | — |

**ADD_PILOT_NEW_USER payload detail:**

```
username;password;firstName;lastName;email;securityClearanceDate(dd-MM-yyyy);phone[;CERT:<modelId>;<engineId>[;CERT:...]]
```

`CERT:` tokens follow the same stripping logic as REGISTER_AIRCRAFT.

---

## 5. Pilot opcodes (40–49) — US086

Requires `PILOT_LOGIN` (opcode 11). Wrong role → `FORBIDDEN (-21)`.  
Full contracts: [`US086/create-flight-plan-tcp.md`](../US086/create-flight-plan-tcp.md), [`US086/design.md`](../US086/design.md).

| Opcode | Constant | Description | Codec class |
|--------|----------|-------------|-------------|
| 40 | `CREATE_FLIGHT_PLAN` | Create a new flight plan (US080) | `PilotCreateFlightPlanPayload` |
| 41 | `PARSE_FLIGHT_PLAN_FILE` | Send DSL file bytes for parsing (US081/US121) | `PilotFlightPlanPayload` |
| 42 | `IMPORT_FLIGHT_PLAN` | Persist the validated DSL plan | `PilotFlightPlanPayload` |
| 43 | `LIST_IMPORT_AIRCRAFT` | Active aircraft list (wizard helper) | — |
| 44 | `ATTACH_WEATHER` | Attach weather data to a flight plan (US082) | — |
| 45 | `VALIDATE_FLIGHT_PLAN` | Trigger simulation validation (US085) | — |
| 46 | `LIST_MY_FLIGHTS` | List pilot's own flight plans | — |
| 47 | `LIST_CREATE_ROUTES` | Active routes for the creation wizard | — |
| 48 | `LIST_COMPANY_PILOTS` | Company pilots for the creation wizard | — |

### 5.1 CREATE_FLIGHT_PLAN payload

```
routeName;aircraftReg;pilotUsername;departure;arrival;fuelValue;fuelUnit;paxCount;paxWeightKg;cargoWeightKg;suffix;confirmReplace
```

- `departure` / `arrival`: `uuuu-MM-dd HH:mm`
- `fuelUnit`: `kg` or `l`
- `confirmReplace`: `false` initially; re-send with `true` after `NEEDS_CONFIRMATION (-24)` response

Success response:
```
OK|<designator>|DRAFT|CREATED
OK|<designator>|DRAFT|REPLACED|<previousStatus>
```

### 5.2 PARSE_FLIGHT_PLAN_FILE / IMPORT_FLIGHT_PLAN payload

```
fileName;base64EncodedFileContent
```

Codec: `PilotFlightPlanPayload.encodeFile / decodeFile`. Same transport as `WeatherCsvPayload.encodeFile` (US042).

---

## 6. Weather opcodes (50–59) — US044

Requires `WEATHER_LOGIN` (opcode 12). Wrong role → `FORBIDDEN (-21)`.  
Full contracts: [`US044/weather-tcp.md`](../US044/weather-tcp.md).

| Opcode | Constant | Description | Codec class |
|--------|----------|-------------|-------------|
| 50 | `LIST_AREAS` | List all air control areas (helper) | — |
| 51 | `REGISTER_WEATHER` | Register a single weather record (US041) | `WeatherRegisterPayload` |
| 52 | `BULK_IMPORT` | Import weather records from CSV (US042) | `WeatherCsvPayload` |
| 53 | `CONSULT_BY_DAY` | Consult weather records by area + day (US043) | `WeatherConsultPayload` |

### 6.1 REGISTER_WEATHER payload

```
areaCode;temp;hum;press;dir;speed;start;end;x1:y1,x2:y2,...
```

- `start` / `end`: `dd-MM-yyyy HH:mm`
- Coordinates: at least 3 points forming a polygon inside the area boundary

### 6.2 BULK_IMPORT payload

```
fileName;base64EncodedCsvContent
```

CSV format: one row per weather record; same fields as REGISTER_WEATHER payload, header optional.

### 6.3 CONSULT_BY_DAY payload

```
areaCode;dd-MM-yyyy
```

Success response: zero or more pipe-delimited lines:

```
<id>|<areaCode>|<start>|<end>|<temp>|<hum>|<press>|<dir>|<speed>
```

---

## 7. UDP log format — US090

The logging server receives raw UTF-8 datagrams on UDP port 2227. No framing — one event per datagram.

Format (pipe-delimited):

```
<ISO-8601 instant>|<username>|<clientIp>|<clientPort>|<accessService>|<event_or_opcode>
```

Example:

```
2026-06-13T15:42:01.123Z|atcc1|192.168.1.10|54321|RCOMP-TCP|LOGIN_OK
2026-06-13T15:42:12.456Z|atcc1|192.168.1.10|54321|RCOMP-TCP|LIST_FLEET
2026-06-13T15:43:00.789Z|atcc1|192.168.1.10|54321|RCOMP-TCP|LOGOUT
```

| Field | Source |
|-------|--------|
| ISO-8601 instant | `Instant.now()` at time of event |
| username | `ClientHandler.authenticatedUsername` (or `"unknown"`) |
| clientIp | `Socket.getInetAddress().getHostAddress()` |
| clientPort | `Socket.getPort()` |
| accessService | Always `"RCOMP-TCP"` in current implementation |
| event_or_opcode | `LOGIN_OK`, `LOGIN_FAIL`, `LOGOUT`, `DISCONNECT`, or operation name (e.g. `LIST_FLEET`) |

Delivery: **fire-and-forget** — no retransmission if the logging server is unreachable.

---

## 8. HTTP log API — US091

Base URL: `http://vsgate-http.dei.isep.ipp.pt:10387` (cloud)  
Local base: `http://localhost:2224`

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/events` | All received log events | JSON array of event objects |
| `GET` | `/active` | Currently active (logged-in) usernames | JSON array of strings |

Event object schema:

```json
{
  "timestamp": "2026-06-13T15:42:01.123Z",
  "username": "atcc1",
  "clientIp": "192.168.1.10",
  "clientPort": 54321,
  "accessService": "RCOMP-TCP",
  "event": "LOGIN_OK"
}
```

The React SPA (served from the same port at `/`) polls `/events` and `/active` to render the dashboard.

---

## 9. Known gaps and limitations

### 9.1 ATCC opcodes 31 and 33 — defined but not implemented

| Opcode | Constant | Protocol class | `AtccCommandHandler` |
|--------|----------|----------------|----------------------|
| 31 | `ADD_PILOT_EXISTING_USER` | defined in `AtccOpcodes.java` | **no `case` → falls to `default → BAD_REQUEST`** |
| 33 | `LIST_ELIGIBLE_USERS` | defined in `AtccOpcodes.java` | **no `case` → falls to `default → BAD_REQUEST`** |

These opcodes are used in the client-side wizard (`AddPilotRemoteUI`) and referenced in `US078/design.md`, but the corresponding server-side handler methods were not implemented. The backoffice equivalent (`AddPilotCollaboratorController.addExistingUser(...)`) exists in the core layer.

**Impact:** Attempting to add a pilot by selecting an existing system user via the remote client returns `BAD_REQUEST (-22): "Unknown ATCC opcode: 31"`. The workaround is to use `ADD_PILOT_NEW_USER (30)` to register the pilot as a new user.

### 9.2 SessionManager is in-process only

`SessionManager` stores authenticated usernames in a `static synchronized HashSet`. On server restart, all sessions are lost. There is no mechanism for distributed sessions or token-based auth.

### 9.3 No request idempotency

There is no request ID or deduplication mechanism. A retried `CREATE_FLIGHT_PLAN` with `confirmReplace=false` will return `NEEDS_CONFIRMATION` again (which is safe); a retried write with `confirmReplace=true` will trigger a second replace.

### 9.4 Payload encoding limitations

Payload fields use `;` as separator. Field values must not contain `;`. Route names, aircraft registrations, and usernames are constrained by domain validation — no escaping is implemented.
