# aisafe.rcomp.loggingserver

UDP log receiver and HTTP dashboard for **US091** (Remote Access Monitor).

## Dashboard UI (React)

Source: `dashboard-ui/` (Vite + React + TypeScript).

Maven runs `npm ci` and `npm run build` during `generate-resources`, outputting static files to `src/main/resources/dashboard/`.

### Prerequisites

- **Node.js** is downloaded automatically by Maven (`frontend-maven-plugin`), or use local Node for development.

### Development (hot reload)

```bash
# Terminal 1 — logging server (HTTP :2224, UDP per AISAFE_RCOMP_UDP_PORT)
cd aisafe.base && ./run-rcomp-server.sh

# Terminal 2 — Vite dev server (proxies /api to :2224)
cd aisafe.base/aisafe.rcomp.loggingserver/dashboard-ui
npm install
npm run dev
```

Open http://localhost:5173/

### Production build

```bash
cd aisafe.base
mvn -pl aisafe.rcomp.loggingserver package
```

Open http://localhost:2224/ (or `AISAFE_RCOMP_HTTP_PORT`).

## HTTP endpoints

| Path | Description |
|------|-------------|
| `GET /` | React SPA (dashboard) |
| `GET /events`, `/active-users` | SPA routes |
| `GET /api/events` | JSON event lines |
| `GET /api/active-users` | JSON usernames |
