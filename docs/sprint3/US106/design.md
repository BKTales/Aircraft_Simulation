# US106 — Design

## Objective

Specialized threads in the parent process, coordinated by condition variables, without changing the multi-process child model.

## API

[`sim_dedicated_thread.h`](../../../flight_simulator/src/main/utils/sim_dedicated_thread.h)

```c
int  sim_dedicated_threads_start(sim_thread_arg_t *arg,
                                 pthread_t *t_env, pthread_t *t_safety, pthread_t *t_report);
void sim_dedicated_threads_stop(pthread_t t_env, pthread_t t_safety, pthread_t t_report, ...);
void sim_dedicated_threads_after_collect(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync);
void sim_environment_before_publish(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync);
```

## Threads

| Thread | Entry point | Trigger | Work |
|--------|-------------|---------|------|
| Environment | `environment_dedicated_thread_fn` | `env_pending` (US110) | Write wind to SHM |
| Safety | `safety_dedicated_thread_fn` | `step_pending` post-collect | `check_collisions_detect()` |
| Report | `report_dedicated_thread_fn` | `violation_cond` / shutdown | `sim_record_violation_batch()` |

## Synchronization (`sim_thread_sync_t`)

| Primitive | Use |
|-----------|-----|
| `step_mutex` + `step_cond` | Main signals safety after collect |
| `violation_mutex` + `violation_cond` | Safety → report (US107) |
| `env_mutex` + `env_cond` | Main → environment before publish |
| `done_mutex` + `done_cond` | Shutdown coordination |

## Lifecycle

```
main()
├── sim_thread_sync_init()
├── sim_dedicated_threads_start()   # pthread_create × 3
├── loop:
│   ├── sim_environment_before_publish()  # US110
│   ├── publish / collect
│   └── sim_dedicated_threads_after_collect()
├── sim_dedicated_threads_stop()    # broadcast sim_done, join
└── handle_report_output()
```

## Component diagram

See [us106-threads.puml](us106-threads.puml) / [US106_DedicatedThreads.svg](US106_DedicatedThreads.svg)

## Files

| File | Role |
|------|------|
| `utils/sim_dedicated_thread.c` | Thread implementation |
| `utils/parent_safety.c` | Detection invoked by safety thread |
| `utils/flight_report.c` | Mutex on `simulation_report_t` |

## Architectural note

Publish/collect remain on the **main thread** (US105 design) for IPC determinism. Dedicated threads process work **after** collect, in parallel with `check_flight_status` where safe.
