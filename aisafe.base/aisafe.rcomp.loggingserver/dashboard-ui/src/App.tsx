import { Route, Routes, useLocation } from "react-router-dom";
import { ErrorBanner } from "./components/ErrorBanner";
import { Header } from "./components/Header";
import { useLogPolling, type PollMode } from "./hooks/useLogPolling";
import { ActiveUsersPage } from "./pages/ActiveUsersPage";
import { DashboardPage } from "./pages/DashboardPage";
import { EventsPage } from "./pages/EventsPage";

function pollModeFromPath(pathname: string): PollMode {
  if (pathname.startsWith("/events")) return "events";
  if (pathname.startsWith("/active-users")) return "users";
  return "all";
}

export default function App() {
  const { pathname } = useLocation();
  const mode = pollModeFromPath(pathname);
  const polling = useLogPolling(mode);

  return (
    <>
      <Header />
      {polling.apiError && <ErrorBanner />}
      <main className="app-main">
        <Routes>
          <Route path="/" element={<DashboardPage polling={polling} />} />
          <Route path="/events" element={<EventsPage polling={polling} />} />
          <Route
            path="/active-users"
            element={<ActiveUsersPage polling={polling} />}
          />
        </Routes>
      </main>
      <footer className="app-footer">
        US091 · Remote access log visualization · Auto-refresh every 2s
      </footer>
    </>
  );
}
