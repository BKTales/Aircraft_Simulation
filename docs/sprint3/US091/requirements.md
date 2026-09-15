# US091 — Requirements

## User Story

As an operator monitoring remote access, I want to visualize remote access logs via HTTP and AJAX, so that I can inspect events and active users in near real time.

## Requirements

### Functional

- The logging server shall expose an HTTP dashboard endpoint.
- The dashboard shall fetch updates using AJAX.
- The server shall provide JSON endpoints for:
  - log events
  - active users
- UDP log ingestion shall feed the same visualization data source.

## Acceptance Criteria

- Accessing the dashboard shows current active users and logged events.
- AJAX refresh updates lists without page reload.
- API responses are valid JSON.
- Active users list changes consistently on login/logout-related events.

