# US090 — Analysis

## Goal

Capture and transmit remote access events (login, logout, disconnect) from all remote access services (US78/ATCC, US86/Pilot, US44/Weather) to a centralized logging server via UDP.

## Scope (Sprint 3)

- **Module**: `aisafe.rcomp.server`
- **Log transmission**: UDP datagrams to configured remote logging server
- **Event types**: LOGIN_OK, LOGIN_FAIL, LOGOUT, DISCONNECT
- **Integration points**:
  - `ClientHandler` — captures session lifecycle events
  - `RemoteAccessLogger` — constructs and dispatches log events
  - `UdpClient` — sends UDP datagrams to logging server

## Functional Decisions

### Event Capture Strategy

- All remote access lifecycle events are captured at the session level (`ClientHandler`).
- Each event includes contextual information: timestamp, authenticated username, client IP/port, and service identifier.
- Four event types are recognized:
  - **LOGIN_OK**: Successful authentication
  - **LOGIN_FAIL**: Failed authentication attempt
  - **LOGOUT**: User-initiated session termination
  - **DISCONNECT**: Unintended session termination (network loss, timeout, etc.)

### Log Message Format

Log events are transmitted as pipe-delimited UTF-8 strings:

```
timestamp|username|clientIp|clientPort|service|eventType
```

**Example:**
```
2026-06-10T14:35:22.123456Z|pilot1|192.168.1.100|54321|US86|LOGIN_OK
```

### Service Identifier Resolution

The service identifier (US44, US78, or US86) is determined at runtime based on the authenticated user's role:
- **ATCC role** → service = `US78`
- **Pilot role** → service = `US86`
- **Weather Person role** → service = `US44`

### UDP Configuration

The logging server is configured via system properties (with fallback defaults):
- **Host**: `AISAFE_RCOMP_LOG_HOST` (default: `vs387.dei.isep.ipp.pt`)
- **Port**: `AISAFE_RCOMP_LOG_UDP_PORT` (default: `2227`)

Alternative properties for gateway routing:
- `AISAFE_RCOMP_LOG_UDP_GATE_HOST`
- `AISAFE_RCOMP_LOG_UDP_GATE_PORT`

### Error Handling

UDP transmission is **fire-and-forget**:
- Send failures are logged to stderr as warnings but do NOT prevent session operations.
- No retry mechanism; transient network failures are silently ignored.

## Traceability to Implementation

- **RemoteAccessLogger** (`aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/RemoteAccessLogger.java:15-34`)
  - Static method `log(Event, username, clientIp, clientPort, service, operation)` constructs and submits events.
  - Supports both named events (LOGIN_OK, etc.) and custom operation names.

- **UdpClient** (`aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/UdpClient.java:24-34`)
  - Static method `send(String message)` transmits UTF-8 datagram.
  - Handles socket creation, packet construction, and exception suppression.

- **ClientHandler** (`aisafe.rcomp.server/src/main/java/eapli/aisafe/rcomp/server/ClientHandler.java`)
  - Calls `logAction(RemoteAccessLogger.Event.LOGIN_OK, null)` on successful login.
  - Calls `logAction(RemoteAccessLogger.Event.LOGIN_FAIL, null)` on failed login.
  - Calls `logAction(RemoteAccessLogger.Event.LOGOUT, null)` on user logout.
  - Calls `logAction(RemoteAccessLogger.Event.DISCONNECT, null)` on session disconnect.
  - Helper method `resolveService()` determines service ID from authenticated user's role.

- **UdpServer** (`aisafe.rcomp.loggingserver/src/main/java/eapli/aisafe/rcomp/loggingserver/UdpServer.java`)
  - Listens on port 2227 (configurable) and receives UDP datagrams.
  - Parses UTF-8 messages and forwards to `LogEventStore`.

- **LogEventStore** (`aisafe.rcomp.loggingserver/src/main/java/eapli/aisafe/rcomp/loggingserver/LogEventStore.java`)
  - Stores events in a bounded deque (max 500 events).
  - Tracks active users from LOGIN_OK/LOGOUT/DISCONNECT events.

## Data Flow

```
[User Authentication] 
    ↓
[ClientHandler session lifecycle]
    ↓
[logAction() call]
    ↓
[RemoteAccessLogger.log() formats event]
    ↓
[UdpClient.send() transmits datagram]
    ↓
[Logging Server UDP receiver stores event]
```

## Risk Assessment

- **Network loss**: UDP datagrams may be lost in transit. Logging is not guaranteed but best-effort.
- **Clock synchronization**: Timestamp accuracy depends on system clock synchronization.
- **Logging server unavailability**: If the remote logging server is down, events are silently dropped (no blocking).
