# US091 — Design

## Runtime view

```mermaid
flowchart LR
    udpClients[Remote clients sending UDP logs] --> udpServer[UdpServer]
    udpServer --> logStore[LogEventStore]
    httpClient[Browser] --> httpServer[HttpServer]
    httpServer --> logStore
    httpServer --> dashboard[HTML dashboard with AJAX]
    dashboard --> apiEvents[/api/events JSON]
    dashboard --> apiUsers[/api/active-users JSON]
```

## Endpoint behavior

- `GET /`, `/events`, `/active-users`
  - React SPA (`dashboard-ui/`, built into `classpath:/dashboard/`).
- `GET /assets/*`
  - Vite-built JS/CSS.
- `GET /api/events`
  - returns JSON array with recent log lines.
- `GET /api/active-users`
  - returns JSON array of current active users inferred from auth events.

## Data strategy

- Store keeps up to 500 events to avoid unbounded memory growth.
- Active users are maintained as a deduplicated set.

