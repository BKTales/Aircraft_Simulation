import { EventEntry } from "./EventEntry";
import { Panel } from "./Panel";

export type EventsEmptyMode = "dashboard" | "nodata";

interface EventLogPanelProps {
  events: string[];
  isFetching: boolean;
  emptyMode: EventsEmptyMode;
}

export function EventLogPanel({
  events,
  isFetching,
  emptyMode,
}: EventLogPanelProps) {
  const sorted = [...events].reverse();

  return (
    <Panel title="Event log" count={events.length} isFetching={isFetching}>
      {!isFetching && sorted.length === 0 && emptyMode === "nodata" && (
        <div className="empty-state empty-state--nodata">No data</div>
      )}
      {!isFetching && sorted.length === 0 && emptyMode === "dashboard" && (
        <div className="empty-state">
          <div className="empty-state__ring" aria-hidden="true" />
          <strong>No events yet</strong>
          <span>Send UDP logs to the logging server to populate this feed.</span>
        </div>
      )}
      {sorted.length > 0 && (
        <ul className="event-list">
          {sorted.map((line, i) => (
            <EventEntry key={`${i}-${line.substring(0, 40)}`} line={line} />
          ))}
        </ul>
      )}
    </Panel>
  );
}
