# NFR08 – Deploy PostgreSQL on vs233

## Server (DEI Private Cloud)

| Item                  | Value                                                              |
|:----------------------|:-------------------------------------------------------------------|
| VS ID                 | vs233                                                              |
| Hostname              | `vs233.dei.isep.ipp.pt`                                            |
| Private IP            | `10.9.20.233`                                                      |
| PostgreSQL (internal) | `vs233.dei.isep.ipp.pt:5432` / `10.9.20.233:5432` (VNET1 only)     |
| PostgreSQL (external) | `vsgate-s1.dei.isep.ipp.pt:10233` when **External access ENABLED** |
| SSH                   | `vs233.dei.isep.ipp.pt:2222` ← `vsgate-ssh.dei.isep.ipp.pt:10233`  |
| Adminer (optional)    | `https://vs-gate.dei.isep.ipp.pt:30233`                            |

## 1. Start the VS and get credentials

1. In the DEI panel, click **START** and wait until status is **RUNNING**.
2. Copy the initial password for user **`postgres`** from the DEI dashboard (not shown on the VS description page).
3. Note your **SSH login** for the VS (DEI documentation).

## 2. Network access from your machine

### On campus / ISEP network (preferred)

On the DEI/ISEP network (or VPN into `10.9.0.0/16`), connect directly to the VS private IP — **no SSH tunnel**:

```bash
export PGHOST=10.9.20.233
export PGPORT=5432
```

Set JDBC in `.env` (see section 4):

```properties
AISAFE_JDBC_URL=jdbc:postgresql://10.9.20.233:5432/aisafe
```

### External access (panel: External access ENABLED)

Works without VPN or tunnel:

```bash
export PGHOST=vsgate-s1.dei.isep.ipp.pt
export PGPORT=10233
```

```properties
jakarta.persistence.jdbc.url=jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10233/aisafe
```

Wait until the VS is **RUNNING** and PostgreSQL has finished first-boot init (connections refused until then).

### SSH tunnel (alternative)

```bash
export VS_SSH_USER=1240580          # your VS owner/login id
./aisafe.base/scripts/ssh-tunnel-vs233.sh
# default: vs233.dei.isep.ipp.pt:2222
# alt: VS_HOST=vsgate-ssh.dei.isep.ipp.pt VS_SSH_PORT=10233
```

Then `PGHOST=127.0.0.1` and `jdbc:postgresql://127.0.0.1:5432/aisafe`.

## 3. Create database

Via Adminer or `psql`:

```bash
psql -h "$PGHOST" -p "$PGPORT" -U postgres -c "CREATE DATABASE aisafe;"
```

Or run [create-aisafe-db.sql](../../../aisafe.base/scripts/create-aisafe-db.sql), or (external gate):

```bash
cd aisafe.base
export AISAFE_DB_PASSWORD='your_postgres_password'
export JDBC_URL=jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10233/postgres
javac -cp "$(mvn -q -pl aisafe.bootstrap dependency:build-classpath -Dmdep.outputFile=/dev/stdout)" scripts/CreateAisafeDb.java
java -cp scripts:$(mvn -q -pl aisafe.bootstrap dependency:build-classpath -Dmdep.outputFile=/dev/stdout) CreateAisafeDb
```

Verify:

```bash
./aisafe.base/scripts/verify-postgres.sh
```

## 4. Configure the application

```bash
cd aisafe.base
# Criar .env — ver AISAFE-config-fora-do-GitHub.md na raiz Sem4 (partilha privada com a equipa)
```

Edit `.env` (gitignored; **não** está no GitHub):

```properties
AISAFE_JDBC_URL="jdbc:postgresql://vsgate-s1.dei.isep.ipp.pt:10233/aisafe?sslmode=disable&connectTimeout=30&socketTimeout=60"
AISAFE_JDBC_USER=postgres
AISAFE_DB_PASSWORD=your_postgres_password
```

`application-postgres.properties` holds only non-secret settings (schema-generation, dialect).

## 5. Bootstrap and run

First run (creates tables + seed data):

```bash
./run-aisafe.sh --bootstrap
```

Then set in `application-postgres.properties`:

```properties
jakarta.persistence.schema-generation.database.action=none
```

Normal runs:

```bash
./run-aisafe.sh
```

## 6. Local development

```bash
./run-aisafe-inmemory.sh --bootstrap   # in-memory (tests only)
```

JDBC credentials are in `.env`; schema settings in `application-postgres.properties`. Both are loaded by `./run-aisafe.sh` and `AppSettings`.

## Troubleshooting

| Problem                          | Action                                                                    |
|:---------------------------------|:--------------------------------------------------------------------------|
| Connection refused after START   | Wait for PostgreSQL init; retry                                           |
| Timeout from home                | Start SSH tunnel or VPN                                                   |
| Auth failed                      | Re-copy password from DEI panel                                           |
| Wrong schema                     | Use `create` or `drop-and-create` only on empty DB; then switch to `none` |
