import { ActiveUsersPanel } from "../components/ActiveUsersPanel";
import type { LogPollingState } from "../hooks/useLogPolling";

interface ActiveUsersPageProps {
  polling: LogPollingState;
}

export function ActiveUsersPage({ polling }: ActiveUsersPageProps) {
  return (
    <div className="dashboard-grid dashboard-grid--single">
      <ActiveUsersPanel
        users={polling.users}
        isFetching={polling.isFetching}
        emptyMode="nodata"
      />
    </div>
  );
}
