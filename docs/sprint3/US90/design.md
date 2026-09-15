# US090 — Design

## Runtime View

```mermaid
flowchart LR
    subgraph RemoteAccessClient["Remote Access Services"]
        CH["ClientHandler<br/>(Session Lifecycle)"]
        RAL["RemoteAccessLogger<br/>(Event Construction)"]
    end
    
    subgraph LocalSystem["Local RCOMP System"]
        UDP["UdpClient<br/>(UDP Transmission)"]
    end
    
    subgraph RemoteLogging["Remote Logging Server (Cloud)"]
        UdpServer["UdpServer<br/>(UDP Receiver)"]
    end
    
    CH -->|logAction()| RAL
    RAL -->|send()| UDP
    UDP -->|UDP Datagram| UdpServer
```

## Event Flow Diagram

```
User Login Attempt
    ↓
[ClientHandler.authenticate()]
    ├─→ SUCCESS: logAction(LOGIN_OK, null)
    └─→ FAILURE: logAction(LOGIN_FAIL, null)
    ↓
[RemoteAccessLogger.log()]
    └─→ Formats: timestamp|username|ip|port|service|event
    ↓
[UdpClient.send()]
    └─→ Creates DatagramSocket
    └─→ Encodes message as UTF-8
    └─→ Sends to logging server
    ↓
User Session Active
    ↓
User Logout or Disconnect
    ├─→ LOGOUT: logAction(LOGOUT, null)
    └─→ DISCONNECT: logAction(DISCONNECT, null)
    ↓
[RemoteAccessLogger.log()]
    └─→ Transmits termination event
```

## Sequence Diagram

![US090 Sequence Diagram](us90-sd.svg)

## Message Format

### UDP Datagram Payload

```
timestamp|username|clientIp|clientPort|service|eventType
```

### Field Descriptions

| Field | Type | Format | Example |
|-------|------|--------|---------|
| `timestamp` | String | ISO 8601 | `2026-06-10T14:35:22.123456Z` |
| `username` | String | username or "unknown" | `pilot1` |
| `clientIp` | String | IPv4 address | `192.168.1.100` |
| `clientPort` | Integer | port number (string representation) | `54321` |
| `service` | String | US identifier | `US86` (or `US78`, `US44`) |
| `eventType` | String | event enum or operation name | `LOGIN_OK`, `LOGOUT`, `DISCONNECT` |

### Event Types

| Event | Description | Scenario |
|-------|-------------|----------|
| `LOGIN_OK` | Successful authentication | User credentials valid, role matched |
| `LOGIN_FAIL` | Failed authentication | Invalid credentials or role mismatch |
| `LOGOUT` | User-initiated disconnect | Explicit logout command from client |
| `DISCONNECT` | Unintended session end | Network loss, socket close, exception |

## Configuration

### System Properties

Logging server destination is configured via Java system properties (checked in order):

```java
AISAFE_RCOMP_LOG_UDP_GATE_HOST   // 1st priority (gateway)
AISAFE_RCOMP_LOG_UDP_HOST        // 2nd priority
AISAFE_RCOMP_LOG_HOST            // 3rd priority (default: vs387.dei.isep.ipp.pt)

AISAFE_RCOMP_LOG_UDP_GATE_PORT   // 1st priority (gateway)
AISAFE_RCOMP_LOG_UDP_PORT        // 2nd priority (default: 2227)
```

**Example:**
```bash
java -DAISAFE_RCOMP_LOG_HOST=logging.example.com \
     -DAISAFE_RCOMP_LOG_UDP_PORT=5555 \
     -jar aisafe.rcomp.server.jar
```

## Architecture Decisions

### 1. Fire-and-Forget UDP Transmission

**Decision**: Use UDP (datagram) instead of TCP (stream).

**Rationale**:
- Logging must not block remote access operations.
- UDP is connectionless and low-latency.
- Loss of individual log events is acceptable for audit purposes (best-effort).
- Reduces coupling between remote access server and logging server.

### 2. Structured Log Format

**Decision**: Pipe-delimited UTF-8 string format.

**Rationale**:
- Human-readable for manual inspection.
- Easy to parse by both receiving and analysis tools.
- No binary encoding overhead.
- Consistent with existing RCOMP protocol patterns.

### 3. Service Identifier Resolution

**Decision**: Service ID derived from user role at authentication time.

**Rationale**:
- Unambiguous mapping from role to service (ATCC→US78, Pilot→US86, Weather→US44).
- Enables filtering logs by service during analysis.
- Centralizes role-to-service mapping logic in `ClientHandler`.

### 4. Bounded Event Storage (Logging Server)

**Decision**: Store max 500 events in-memory FIFO queue.

**Rationale**:
- Prevents unbounded memory growth on logging server.
- Sufficient buffer for real-time monitoring.
- Oldest events are discarded when buffer is full.

## Error Handling Strategy

### Network Failures

- **UDP send failure** (socket creation, transmission error):
  - Exception caught and logged to stderr as warning.
  - Remote access operation continues unaffected.
  - No retry or fallback mechanism.

### Logging Server Unavailability

- **Server offline or unreachable**:
  - UDP packet is silently dropped (fire-and-forget).
  - Session continues normally.
  - No timeout mechanism (socket.send() has default OS timeout).

### Malformed Events

- **Null username or blank service**:
  - Message is still sent but with sanitized values (empty string or "unknown").
  - Logging server validates and may filter during storage.

## Security Considerations

- **No authentication** for UDP transmission (low-trust network assumed).
- **No encryption** of log messages (UDP datagram, plaintext).
- **IP spoofing risk**: Logging server must validate source IP if needed.
- **Recommendation**: Deploy logging server on restricted network or behind firewall.
