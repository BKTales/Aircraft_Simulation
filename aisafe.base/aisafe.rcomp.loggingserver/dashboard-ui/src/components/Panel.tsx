import type { ReactNode } from "react";

interface PanelProps {
  title: string;
  count: number;
  isFetching: boolean;
  children: ReactNode;
}

export function Panel({ title, count, isFetching, children }: PanelProps) {
  return (
    <section className="panel">
      <div className="panel-head">
        <h2>{title}</h2>
        <span className="count-badge">{count}</span>
      </div>
      <div className="panel-body">
        {isFetching && count === 0 ? (
          <p className="panel-body--fetching">FETCHING...</p>
        ) : (
          children
        )}
      </div>
    </section>
  );
}
