# US090 — Tests

## Automated Tests

### Unit Tests

#### LoginCredentialsParserTest
Tests the parsing of login credentials with optional role specification.

- **Test**: `parseTwoFieldsDefaultsToAtcc` (line 12-16)
  - Verifies that credentials without explicit role default to ATCC role.
  - Input: `user1;password123`
  - Expected: role = AIR_TRANSPORT_COMPANY_COLLABORATOR

- **Test**: `parseExplicitAtcc` (line 20-22)
  - Verifies explicit ATCC role specification.
  - Input: `user1;password123;ATCC`
  - Expected: role = AIR_TRANSPORT_COMPANY_COLLABORATOR

- **Test**: `parseExplicitPilot` (line 26-29)
  - Verifies explicit Pilot role specification.
  - Input: `pilot1;password123;PILOT`
  - Expected: username = `pilot1`, role = PILOT

- **Test**: `parseRejectsUnknownRoleToken` (line 33-34)
  - Verifies that unknown role tokens are rejected.
  - Input: `user;pass;ADMIN`
  - Expected: parse returns empty Optional

- **Test**: `parseRejectsMissingPassword` (line 38-39)
  - Verifies that incomplete credentials are rejected.
  - Input: `user` (no password)
  - Expected: parse returns empty Optional

### Integration Tests

#### HttpServerTest
Tests HTTP server endpoints and event store integration.

- **Test**: `extractPathStripsQueryString` (line 18-19)
  - Verifies that query parameters are stripped from HTTP request path.
  - Input: `GET /api/events?limit=10 HTTP/1.1`
  - Expected: extracted path = `/api/events`

- **Test**: `toJsonArrayEscapesValues` (line 23-25)
  - Verifies JSON encoding and proper escaping of special characters.
  - Input: `["abc", "x\"y"]`
  - Expected: `["abc","x\"y"]`

- **Test**: `eventStoreTracksActiveUsersFromMessages` (line 29-36)
  - Verifies that `LogEventStore` correctly tracks active users from LOGIN_OK/LOGOUT events.
  - Scenario:
    1. Send LOGIN_OK event for `user-a`
    2. Send LOGIN_OK event for `user-b`
    3. Send LOGOUT event for `user-a`
  - Expected: active users = [`user-b`] (user-a removed)

- **Test**: `eventStoreRecognizesDisconnect` (line 40-44)
  - Verifies that DISCONNECT events remove user from active list.
  - Scenario:
    1. Send LOGIN_OK event for `user-xpto`
    2. Send DISCONNECT event for `user-xpto`
  - Expected: active users list does not contain `user-xpto`

- **Test**: `eventStoreIgnoresLoginFail` (line 48-51)
  - Verifies that LOGIN_FAIL events do NOT add user to active list.
  - Scenario: Send LOGIN_FAIL event for `user-fail`
  - Expected: active users list does not contain `user-fail`

- **Test**: `spaIndexContainsRootElement` (line 55-60)
  - Verifies that the SPA (Single Page Application) dashboard is available.
  - Expected: Dashboard HTML contains `id="root"` and `Remote Access Monitor` text
  - Note: Requires `npm build` via Maven to generate dashboard assets

- **Test**: `spaRoutesServeIndex` (line 64-66)
  - Verifies that SPA routes (`/events`, `/active-users`) serve the dashboard index.
  - Expected: Both routes return static resources
