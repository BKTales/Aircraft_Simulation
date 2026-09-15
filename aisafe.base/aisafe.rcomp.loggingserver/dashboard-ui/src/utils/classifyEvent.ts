export type EventKind = "login" | "logout" | "error" | "udp";

export interface ClassifiedEvent {
  kind: EventKind;
  badge: string;
}

export function classifyEvent(raw: string): ClassifiedEvent {
  const upper = raw.toUpperCase();
  if (upper.includes("|LOGIN_FAIL")) {
    return { kind: "error", badge: "ERROR" };
  }
  if (upper.includes("|LOGIN_OK")) {
    return { kind: "login", badge: "UDP" };
  }
  if (upper.includes("|LOGOUT") || upper.includes("|DISCONNECT")) {
    return { kind: "logout", badge: "LOGOUT" };
  }
  if (raw.toLowerCase().includes("[udp")) {
    return { kind: "udp", badge: "UDP" };
  }
  return { kind: "udp", badge: "UDP" };
}

export function parseEventLine(line: string): { time: string; message: string } {
  const sep = line.indexOf(" | ");
  if (sep > 0) {
    return { time: line.substring(0, sep), message: line.substring(sep + 3) };
  }
  return { time: "", message: line };
}

export function formatTime(isoOrRaw: string): string {
  if (!isoOrRaw) return "—";
  const d = new Date(isoOrRaw);
  if (Number.isNaN(d.getTime())) return isoOrRaw;
  const day = String(d.getDate()).padStart(2, "0");
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const h = String(d.getHours()).padStart(2, "0");
  const m = String(d.getMinutes()).padStart(2, "0");
  const s = String(d.getSeconds()).padStart(2, "0");
  return `${day}/${month}, ${h}:${m}:${s}`;
}
