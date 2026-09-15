# RCOMP — Runbook

← [README](README.md)

All scripts live in `aisafe.base/` and use `scripts/load-env.sh` + `scripts/rcomp-defaults.sh` for configuration.  
A valid `.env` file and `application-*.properties` are required (see `AISAFE-config-fora-do-GitHub.md`).

---

## 1. Port reference

| Component | Environment variable | Local default | Cloud port | Cloud host |
|-----------|----------------------|---------------|------------|------------|
| TCP server — listen | `AISAFE_RCOMP_TCP_LISTEN_PORT` | 2225 | — (bound on vs353) | — |
| TCP server — client connect | `AISAFE_RCOMP_TCP_PORT` | 2225 (local) | **10353** | `vsgate-s2.dei.isep.ipp.pt` |
| UDP log receiver | `AISAFE_RCOMP_UDP_LISTEN_PORT` | 2227 | — (bound on vs387) | — |
| HTTP log dashboard | `AISAFE_RCOMP_HTTP_LISTEN_PORT` | 2224 | — (bound on vs387) | — |
| HTTP log dashboard — browser | `AISAFE_RCOMP_LOG_HTTP_PORT` | 2224 | **10387** | `vsgate-http.dei.isep.ipp.pt` |
| UDP log — internal target | `AISAFE_RCOMP_LOG_HOST` + `LOG_UDP_PORT` | 127.0.0.1:2227 | 10.9.21.131:2227 | vs387 VNET |
| UDP log — external test | `AISAFE_RCOMP_LOG_UDP_GATE_HOST/PORT` | — | 10387 | `vsgate-s3.dei.isep.ipp.pt` |
| PostgreSQL | `AISAFE_DB_HOST` (in .env) | localhost:5432 | 5432 | vs233 (internal) |

---

## 2. Scripts

### 2.1 Start the TCP server (Cloud A / local)

```bash
cd aisafe.base
./run-rcomp-server.sh
```

What it does:
1. Loads `.env` and `application-postgres.properties` (or `AISAFE_CONFIG` override).
2. Sets default ports via `rcomp-defaults.sh`.
3. Builds everything with `mvn clean install -DskipTests`.
4. Launches `RcompTcpServerApp` on `AISAFE_RCOMP_TCP_LISTEN_PORT`.

Override for local in-memory run (no Postgres needed):

```bash
AISAFE_RCOMP_LOCAL=1 AISAFE_CONFIG=./application-inmemory.properties ./run-rcomp-server.sh
```

### 2.2 Start the remote client

```bash
cd aisafe.base
./run-remote-app.sh
```

What it does:
1. Builds (skip tests).
2. Launches `RemoteClientApp` pointed at `AISAFE_RCOMP_HOST:AISAFE_RCOMP_TCP_PORT`.

Connect to local server:

```bash
AISAFE_RCOMP_LOCAL=1 ./run-remote-app.sh
```

Connect to cloud server (default, no overrides needed if `.env` is configured):

```bash
./run-remote-app.sh
```

### 2.3 Start the logging server (Cloud B / local)

```bash
cd aisafe.base
./run-rcomp-loggingserver.sh
```

What it does:
1. Builds everything.
2. Launches `LoggingServerApp` on three ports: UDP `$AISAFE_RCOMP_UDP_LISTEN_PORT`, TCP `$AISAFE_RCOMP_TCP_LISTEN_PORT` (forwarding port for optional chain), HTTP `$AISAFE_RCOMP_HTTP_LISTEN_PORT`.
3. Prints the HTTP URL to the console.

### 2.4 Send a test UDP log event

```bash
cd aisafe.base
./run-rcomp-udp-test-client.sh
```

Sends one synthetic log datagram to the configured UDP endpoint. Useful for checking if the logging server is up and the network path is clear.

---

## 3. Full system startup sequence

The order matters because the TCP server sends UDP logs immediately on first login.

```
1. vs233  — PostgreSQL must already be running (managed by ISEP infra, no action needed)
2. vs387  — Start logging server: ./run-rcomp-loggingserver.sh
3. vs353  — Start TCP server:     ./run-rcomp-server.sh
4. local  — Start remote client:  ./run-remote-app.sh
```

For a **fully local** demo (in-memory, no network needed):

```
1. Terminal 1: AISAFE_RCOMP_LOCAL=1 AISAFE_CONFIG=./application-inmemory.properties ./run-rcomp-loggingserver.sh
2. Terminal 2: AISAFE_RCOMP_LOCAL=1 AISAFE_CONFIG=./application-inmemory.properties ./run-rcomp-server.sh
3. Terminal 3: AISAFE_RCOMP_LOCAL=1 ./run-remote-app.sh
```

---

## 4. Configuration files

| File | Purpose | Location |
|------|---------|----------|
| `.env` | DB credentials, secrets | `aisafe.base/.env` — **not in git** |
| `application-postgres.properties` | Production JPA (PostgreSQL on vs233) | `aisafe.base/` |
| `application-inmemory.properties` | In-memory H2, for local dev | `aisafe.base/` |
| `application-remote.properties` | JPA for remote DB (NFR08) | `aisafe.persistence/src/main/resources/` |
| `scripts/rcomp-defaults.sh` | Default port / host env vars | `aisafe.base/scripts/` |
| `scripts/load-env.sh` | Loads `.env` into shell environment | `aisafe.base/scripts/` |

---

## 5. Environment variables (full list)

| Variable | Default | Description |
|----------|---------|-------------|
| `AISAFE_CONFIG` | `application-postgres.properties` | JPA config file path |
| `AISAFE_ENV` | `.env` | Secrets file |
| `AISAFE_RCOMP_LOCAL` | unset | Set to `1` for loopback mode (all components on one machine) |
| `AISAFE_RCOMP_TCP_LISTEN_PORT` | 2225 | Port the TCP server binds |
| `AISAFE_RCOMP_HOST` | cloud: `vsgate-s2.dei.isep.ipp.pt` | Host the TCP client connects to |
| `AISAFE_RCOMP_TCP_PORT` | cloud: 10353, local: 2225 | Port the TCP client connects to |
| `AISAFE_RCOMP_UDP_LISTEN_PORT` | 2227 | UDP port the logging server binds |
| `AISAFE_RCOMP_HTTP_LISTEN_PORT` | 2224 | HTTP port the logging server binds |
| `AISAFE_RCOMP_LOG_HOST` | cloud: `10.9.21.131` | UDP log target host (from TCP server) |
| `AISAFE_RCOMP_LOG_UDP_PORT` | 2227 | UDP log target port (from TCP server) |
| `AISAFE_RCOMP_LOG_HTTP_HOST` | `vsgate-http.dei.isep.ipp.pt` | HTTP dashboard host (for browser access) |
| `AISAFE_RCOMP_LOG_HTTP_PORT` | 10387 | HTTP dashboard port (for browser access) |
| `AISAFE_RCOMP_LOG_UDP_GATE_HOST` | `vsgate-s3.dei.isep.ipp.pt` | External UDP endpoint (for test client from dev machine) |
| `AISAFE_RCOMP_LOG_UDP_GATE_PORT` | 10387 | External UDP port |

---

## 6. Login credentials (bootstrap)

After bootstrapping (running `BootstrapApp`), these accounts are available:

| TCP login role | Username | Password |
|----------------|----------|----------|
| ATCC (opcode 10) | `atcc1` | `password123` |
| Pilot (opcode 11) | `pilot1` | `password123` |
| Weather (opcode 12) | `weather` | `password123` |

The client application prompts for username and password, then selects the correct login opcode based on the chosen menu profile.

---

## 7. Building from scratch

```bash
cd aisafe.base
mvn clean install -DskipTests      # Full build, skip tests
mvn clean install                  # Full build, run tests
mvn -pl aisafe.rcomp.server -am clean package -DskipTests  # Server module only
mvn -pl aisafe.rcomp.tcpclient -am clean package -DskipTests  # Client module only
```

The React dashboard inside `aisafe.rcomp.loggingserver` is built by the Maven `exec:exec` goal (calls `npm run build`). Node.js must be installed. The compiled bundle is embedded in the JAR as a classpath resource.

---

## 8. Testing

### 8.1 Unit and integration tests

```bash
cd aisafe.base
# Run all tests in the server module
mvn -pl aisafe.rcomp.server test

# Run all tests in the protocol module
mvn -pl aisafe.rcomp.protocol test
```

Tests use H2 in-memory and MockMvc / JUnit 5. No running server or database needed.

### 8.2 Manual end-to-end test (local)

1. Start loggingserver and TCP server in `AISAFE_RCOMP_LOCAL=1` mode (see §3).
2. Start the remote client in a third terminal.
3. Select **ATCC**, log in with `atcc1 / password123`.
4. Navigate to Fleet → List fleet. Verify the fleet list appears.
5. Open the browser at `http://localhost:2224` — the dashboard should show the `LOGIN_OK` and `LIST_FLEET` events.

### 8.3 Cloud smoke test

1. Confirm `vsgate-s2.dei.isep.ipp.pt:10353` is reachable:
   ```bash
   nc -zv vsgate-s2.dei.isep.ipp.pt 10353
   ```
2. Run the remote client (default config, no env overrides).
3. Log in, perform one operation, log out.
4. Check the dashboard at `http://vsgate-http.dei.isep.ipp.pt:10387`.

### 8.4 UDP log test

```bash
./run-rcomp-udp-test-client.sh
```

The logging server console (or `GET /events`) should show the test event within 1 second.

---

## 9. Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| `Connection refused` on TCP connect | Server not started, or wrong port/host | Check `AISAFE_RCOMP_HOST` and `AISAFE_RCOMP_TCP_PORT`; confirm server is running |
| `UNAUTHORIZED (-20)` on any command | Not logged in (sent domain opcode before login) | Client bug — always send login opcode first |
| `FORBIDDEN (-21)` | Logged in with wrong role for the opcode range | Disconnect, reconnect, use the correct login opcode |
| `BAD_REQUEST` on opcode 31 or 33 | Known gap — not implemented | Use `ADD_PILOT_NEW_USER (30)` instead; see [protocol.md §9.1](protocol.md#91-atcc-opcodes-31-and-33--defined-but-not-implemented) |
| `INTERNAL_ERROR (-27)` | Unhandled exception in handler | Check TCP server stdout/stderr for the stack trace |
| No events on log dashboard | UDP delivery failed or logging server down | Confirm logging server is running; test with `./run-rcomp-udp-test-client.sh` |
| `MockitoInitializationException` in tests | Mockito inline mock maker can't self-attach JVM | Run Maven with `MAVEN_OPTS="-Djdk.attach.allowAttachSelf=true"` |
| React dashboard 404 | `npm run build` was not run | Run `mvn clean install` in `aisafe.rcomp.loggingserver` (Node.js required) |
| `JPA PersistenceException` on server start | PostgreSQL unreachable or wrong credentials | Check `.env`, confirm `vs233:5432` is reachable; use `application-inmemory.properties` for local dev |
