# US100 — Analysis (Sprint 2)

## User story

As a PO, I want a flight simulation based on independent processes, so that each flight plan runs in isolation and in parallel.

## Context

US100 establishes the **architectural skeleton** of the C component delivered in Sprint 2:

- One **parent** process (`SimulationController`) orchestrates the simulation.
- One **child** process per flight plan (`FlightProcess`).
- Initial communication via **pipes** and **signals** (refactored to SHM in Sprint 3 — US105).

This US matches the SCOMP requirement to implement simulation with **processes and threads** (TP9/TP10).

## Actors and boundaries

| Actor | Interaction |
|-------|-------------|
| PO / SCOMP team | Defines requirements and accepts delivery |
| Parent process | Spawns children, main loop, report |
| Child process | Runs physics for one plan |
| Java (Sprint 3) | Invokes binary via `ProcessBuilder` (US100 EAPLI) |

## Business rules

1. Each valid plan spawns exactly one child process.
2. Invalid plans (`validate_flight_plan` fails) are skipped but logged.
3. Parent exits only when all children finished or simulation was aborted.
4. Parent produces aggregated report (US109).
5. Children exit with `exit(0)` after completing route or receiving stop signal.

## Dependencies

- Flight plan JSON format (Java export / fixtures under `src/data/`)
- Parser in `init.c` / `flight_plan_parser.c`
- US101–US103 add movement, safety, and synchronization on this base

## Sprint 3 evolution

Current implementation replaces pipes with **shared memory + semaphores** (US105/108). `fork()` and one child per plan **remain** — see [US105 design](../../sprint3/US105/design.md).

## Risks

| Risk | Mitigation |
|------|------------|
| Zombie children | `waitpid` on shutdown; `SIGCHLD` handlers |
| Memory leak on plans | `clean_finish()` on all error paths |
| Heterogeneous JSON | Validation in `init.c`; self-contained plans |

## Test traceability

See [TESTS.md](../TESTS.md) § US100 and [tests.md](../../sprint3/US100/tests.md) (Java integration).
