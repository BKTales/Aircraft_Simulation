import { useCallback, useEffect, useRef, useState } from "react";
import { fetchActiveUsers, fetchEvents } from "../api/logApi";

export type PollMode = "all" | "users" | "events";

export interface LogPollingState {
  users: string[];
  events: string[];
  isFetching: boolean;
  apiError: boolean;
  lastFetchAt: Date | null;
  refresh: () => void;
}

export function useLogPolling(mode: PollMode): LogPollingState {
  const [users, setUsers] = useState<string[]>([]);
  const [events, setEvents] = useState<string[]>([]);
  const [isFetching, setIsFetching] = useState(false);
  const [apiError, setApiError] = useState(false);
  const [lastFetchAt, setLastFetchAt] = useState<Date | null>(null);
  const initialLoad = useRef(true);

  const refresh = useCallback(async () => {
    if (initialLoad.current) setIsFetching(true);
    try {
      const calls: Promise<void>[] = [];
      if (mode === "all" || mode === "users") {
        calls.push(
          fetchActiveUsers().then((data) => {
            setUsers(data);
          })
        );
      }
      if (mode === "all" || mode === "events") {
        calls.push(
          fetchEvents().then((data) => {
            setEvents(data);
          })
        );
      }
      await Promise.all(calls);
      setApiError(false);
      setLastFetchAt(new Date());
    } catch {
      setApiError(true);
    } finally {
      setIsFetching(false);
      initialLoad.current = false;
    }
  }, [mode]);

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 2000);
    return () => clearInterval(id);
  }, [refresh]);

  return { users, events, isFetching, apiError, lastFetchAt, refresh };
}
