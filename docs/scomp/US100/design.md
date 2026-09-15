# US100 — Design (Sprint 2)

## Objective

Multi-process architecture: parent orchestrates, children simulate individual flights.

## Sequence diagram

See [simulator-sd.puml](../simulator-sd.puml) (Sprint 2 — pipes).

## Components

| Component | File | Responsibility |
|-----------|------|----------------|
| Entry point | `flight_simulator.c` | `main()`, loop, shutdown |
| Initialization | `init.c` | Load JSON, validate plans |
| Spawn | `process_spawn.c` | `fork_flights()` |
| Child | `flight_process.c` | Per-flight simulation loop |
| Report | `flight_report.c` | US109 |

## `main()` flow (Sprint 2 — conceptual)

```
main()
├── init_program()           # load plans
├── fork_flights()           # N × fork()
├── run_simulation()         # lock-step loop US103
│   ├── broadcast tick (pipe)
│   ├── collect FlightUpdate (pipe)
│   ├── detect_violations()  # US102
│   └── deactivate done flights
├── wait_all_children()
└── generate_report()        # US109
```

## Current flow (Sprint 3)

Same skeleton, IPC replaced:

```
main()
├── init_program()
├── sim_shm_create() + sim_sem_create()    # US105
├── fork_flights()
├── sim_dedicated_threads_start()          # US106/110
├── while (active)
│   ├── try_advance_time()                 # US108
│   ├── sim_step_publish() / sim_step_collect()
│   └── check_flight_status()
├── sim_dedicated_threads_stop()
└── handle_report_output()                 # US109
```

## Main structures

```c
typedef struct program {
    aircraft_list_t     aircraft_list;
    flight_plan_list_t *flight_plans_info;
    flight_process_t   *flight_processes;
    network_t           network;
} program_t;

typedef struct flight_process {
    pid_t         pid;
    int           slot_index;
    flight_plan_t plan;
} flight_process_t;
```

## Design decisions

| Decision | Rationale |
|----------|-----------|
| One process per flight | Fault isolation; SCOMP requirement |
| Single parent coordinates | Avoids races between children |
| Validate before fork | Children only for `is_valid` plans |
| `FS_FLIGHT_PLANS_DIR` | Java and tests inject plans |

## Java integration (Sprint 3)

`FlightSimulationService` exports JSON to a temp directory and invokes:

```java
ProcessBuilder pb = new ProcessBuilder(simulatorPath);
pb.environment().put("FS_FLIGHT_PLANS_DIR", plansDir);
pb.environment().put("FS_REPORTS_DIR", reportsDir);
pb.environment().put("FS_NON_INTERACTIVE", "1");
```

See [US100 design Sprint 3](../../sprint3/US100/design.md).
