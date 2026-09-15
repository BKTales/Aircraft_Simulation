# US091 — Tests

## Automated Tests

- `HttpServerTest`
  - path extraction with query-string handling
  - JSON escaping in API serialization
  - active-users tracking from login/logout events

## Execution

```bash
cd aisafe.base
mvn -q -pl aisafe.rcomp.loggingserver -am test
```

## Manual Tests

1. Start logging server app.
2. Open `http://localhost:<httpPort>/`.
3. Send UDP log messages (including login/logout-like messages).
4. Validate:
   - dashboard updates events list
   - active users list updates consistently

