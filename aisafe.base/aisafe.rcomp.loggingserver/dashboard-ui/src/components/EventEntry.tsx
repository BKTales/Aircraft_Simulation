import { classifyEvent, formatTime, parseEventLine } from "../utils/classifyEvent";

interface EventEntryProps {
  line: string;
}

export function EventEntry({ line }: EventEntryProps) {
  const { time, message } = parseEventLine(line);
  const cls = classifyEvent(message);
  const kindClass =
    cls.kind === "logout"
      ? "event-entry--logout"
      : cls.kind === "error"
        ? "event-entry--error"
        : "event-entry--login";

  const badgeClass =
    cls.badge === "LOGOUT"
      ? "event-badge--logout"
      : cls.badge === "ERROR"
        ? "event-badge--error"
        : "event-badge--udp";

  return (
    <li className={`event-entry ${kindClass}`}>
      <div className="event-top">
        <span className={`event-badge ${badgeClass}`}>{cls.badge}</span>
        <span className="event-time">{formatTime(time)}</span>
      </div>
      <div className="event-raw">{message}</div>
    </li>
  );
}
