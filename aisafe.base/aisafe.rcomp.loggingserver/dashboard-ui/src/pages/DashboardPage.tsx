import { ActiveUsersPanel } from "../components/ActiveUsersPanel";
import { EventLogPanel } from "../components/EventLogPanel";
import type { LogPollingState } from "../hooks/useLogPolling";

interface DashboardPageProps {
  polling: LogPollingState;
}

export function DashboardPage({ polling }: DashboardPageProps) {
  return (
    <div className="dashboard-grid">
      <ActiveUsersPanel
        users={polling.users}
        isFetching={polling.isFetching}
        emptyMode="dashboard"
      />
      <EventLogPanel
        events={polling.events}
        isFetching={polling.isFetching}
        emptyMode="dashboard"
      />
    </div>
  );
}
