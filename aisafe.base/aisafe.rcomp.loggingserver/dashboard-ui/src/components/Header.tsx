import { NavLink } from "react-router-dom";
import { useClock } from "../hooks/useClock";

export function Header() {
  const clock = useClock();

  return (
    <header className="app-header">
      <div className="header-left">
        <div className="logo" aria-hidden="true">
          AI
        </div>
        <div className="header-titles">
          <h1>Remote Access Monitor</h1>
          <p>Live session &amp; event stream</p>
          <nav className="header-nav" aria-label="Main">
            <NavLink to="/" end>
              Dashboard
            </NavLink>
            <NavLink to="/events">Events</NavLink>
            <NavLink to="/active-users">Active users</NavLink>
          </nav>
        </div>
      </div>
      <div className="header-right">
        <span className="live-indicator">
          <span className="live-dot" aria-hidden="true" />
          Live
        </span>
        <span className="last-update">Last update: {clock}</span>
      </div>
    </header>
  );
}
