# NFR08 – Database by configuration

## Source requirement

The system must support persistence either **in memory** or in a **relational database (RDBMS)**. In-memory solutions are used during development and testing; the final deployment must use a **remote persistent** relational database. The system must be able to **initialize default data** (bootstrap).

## Acceptance criteria (team)

| ID | Criterion |
|:---|:----------|
| AC1 | `persistence.repositoryFactory` can switch between `InMemoryRepositoryFactory` and `JpaRepositoryFactory` without code changes |
| AC2 | JPA targets PostgreSQL (remote vs233) via configuration; in-memory for local/CI |
| AC3 | JDBC URL, user, password, and dialect are not hardcoded in `persistence.xml` for production |
| AC4 | `./run-aisafe.sh --bootstrap` creates schema and seed data on remote PostgreSQL |
| AC5 | After bootstrap, `jakarta.persistence.schema-generation.database.action=none` for normal runs |
| AC6 | Database credentials are not committed to Git |
| AC7 | RCOMP TCP clients never connect to the database; only the Java server uses JDBC |

## Sprint assignment

Responsible:  Vitor · Deadline: 12/06/2026 · See [sprint_planning.md](../sprint_planning.md).
