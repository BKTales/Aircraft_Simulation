# US078 — Tests

## Automated

- `ProtocolFrameTest` — frame round-trip
- `FleetCriteriaParserTest` — all US072 filter encodings
- `AtccCommandHandlerTest` — session/role guards, list eligible users (op 33), add pilot existing user (op 31), list active routes (op 39), deactivate route (op 28), unknown opcode

Run: `mvn -pl aisafe.rcomp.protocol,aisafe.rcomp.server -am test`

## Manual

### Setup

1. vs353: `./run-rcomp-server.sh`
2. Mac: `./run-remote-app.sh`

### Login

| Step                          | Expected |
|-------------------------------|----------|
| Login `atcc1` / `password123` | `code=11` LOGIN SUCCESSFUL |
| Login `admin` / (password)    | `code=-11` LOGIN FAILED |

### Fleet

| Step | Expected |
|------|----------|
| List models (register flow) | Lines `id\|name\|...` |
| Register aircraft | `code=0` with aircraft line |
| List fleet UNFILTERED | Company aircraft lines |
| Decommission | Status DECOMMISSIONED |

### Pilots

| Step | Expected |
|------|----------|
| List pilots | Pilot lines for company |
| Add pilot (new user) | `Pilot registered.` |
| Add pilot (existing user) | Eligible emails listed, then `Pilot registered.` |

### Routes

| Step | Expected |
|------|----------|
| Create route (REGULAR/CHARTER) | `code=0` with route line |
| Deactivate route | Active routes listed, then `code=0` with route line |

### Logout

| Step | Expected |
|------|----------|
| Option 0 | `code=13` LOGOUT SUCCESSFUL |
