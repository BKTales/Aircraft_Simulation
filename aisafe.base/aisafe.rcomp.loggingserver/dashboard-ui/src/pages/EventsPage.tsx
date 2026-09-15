import { EventLogPanel } from "../components/EventLogPanel";
import type { LogPollingState } from "../hooks/useLogPolling";

interface EventsPageProps {
  polling: LogPollingState;
}

export function EventsPage({ polling }: EventsPageProps) {
  return (
    <div className="dashboard-grid dashboard-grid--single">
      <EventLogPanel
        events={polling.events}
        isFetching={polling.isFetching}
        emptyMode="nodata"
      />
    </div>
  );
}
