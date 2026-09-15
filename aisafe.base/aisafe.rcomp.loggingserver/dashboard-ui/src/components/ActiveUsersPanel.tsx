import { Panel } from "./Panel";

export type UsersEmptyMode = "dashboard" | "nodata";

interface ActiveUsersPanelProps {
  users: string[];
  isFetching: boolean;
  emptyMode: UsersEmptyMode;
}

export function ActiveUsersPanel({
  users,
  isFetching,
  emptyMode,
}: ActiveUsersPanelProps) {
  return (
    <Panel title="Active users" count={users.length} isFetching={isFetching}>
      {!isFetching && users.length === 0 && emptyMode === "nodata" && (
        <div className="empty-state empty-state--nodata">No data</div>
      )}
      {!isFetching && users.length === 0 && emptyMode === "dashboard" && (
        <div className="empty-state">
          <div className="empty-state__ring" aria-hidden="true" />
          <strong>No active sessions</strong>
          <span>Waiting for login events...</span>
        </div>
      )}
      {users.length > 0 && (
        <ul className="user-list">
          {users.map((u) => (
            <li key={u} className="user-row">
              {u}
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
}
