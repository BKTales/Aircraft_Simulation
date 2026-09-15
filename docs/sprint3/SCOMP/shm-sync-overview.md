# Flight Simulator v2 — Data Sharing & Synchronization (US105 + US108)

**Sprint 2:** parent and children talked through **pipes** — a tick message to start a step, a position update sent back.

**Sprint 3:** **shared memory** holds positions and step info; **named semaphores** make every active flight (and the parent) finish the same step before the simulation clock moves on. **SIGUSR1** still signals a safety violation from parent to child (US107).

---

## Why the change

Pipes meant one read/write pair per message, per flight, every step — and no easy way for the parent to inspect a flight's state without a round trip. Shared memory removes that overhead: each flight has a fixed slot that the parent can read directly once the flight signals it's done. Semaphores replace the pipe's implicit "message arrived" signal with an explicit two-phase handshake (*start step* / *step done*), which is what makes the lock-step barrier in US108 possible.

---

## Three ways processes communicate

| What | How | Who talks to whom | What is sent |
|------|-----|--------------------|--------------|
| Flight & global state | Shared memory (`shm_open` + `mmap`) | Parent ↔ each child | Step number, simulation time, dt, active flight count, environment (wind), each flight's position update |
| Same step together | Two named semaphores per flight | Parent ↔ each child | "Start step" / "I finished this step" |
| Safety problem | Signal `SIGUSR1` | Parent → child | Violation warning (child terminates gracefully on its next loop check) |

---

## Run isolation (`run_id`)

The shared memory segment and every semaphore are namespaced by a `run_id`, derived from the **parent's own PID** (`getpid()`) and exported via `setenv` so children inherit it. This means:

- Multiple simulations (e.g. parallel test scenarios) never collide on the same SHM/semaphore names.
- All IPC objects created for a run are easy to identify and clean up — `sim_shm_destroy()` and `sim_sem_destroy()` are called once, after every child has been `waitpid`-ed, on every exit path (including error paths during initialization).

---

## What lives in shared memory

The segment is a single struct with two parts:

**Global part — `sim_global.*`, parent writes, children read:**

- `sim_time_s` — current simulation clock (absolute seconds)
- `dt_s` — step length in seconds (`DT_S`, currently **1 s** per step)
- `active_flights` — how many flights are being stepped this tick
- `shutdown` — set during teardown so any child still waiting on a semaphore can exit cleanly
- `environment` — wind snapshot published once per tick by the environment thread (US110), used as the fallback wind source for children without their own weather file or per-segment wind

**One slot per flight — `slots[i]`, that child writes its own slot only:**

- `flight_id`, `slot_index`, `active`
- `pending_step` — step number the parent wants this flight to compute
- `update_ready` — flag the child sets *after* writing its update, cleared by the parent *before* posting "start step"
- `update` (`flight_update_t`) — latitude, longitude, altitude, speed, fuel, phase (climb/cruise/descend), `done`, `no_fuel`, remaining distance

Before `fork()`, the parent assigns each child **a fixed slot index** (flight 0 → slot 0, flight 1 → slot 1, …). A child only ever reads/writes `slots[its_own_index]` and only reads the global part — there is no cross-flight write access.

---

## Synchronization without a shared lock

There is **no mutex inside the shared memory segment**. Instead, correctness comes from a strict *ownership handoff* enforced by the semaphore pair `step_start[i]` / `step_done[i]`:

- While the parent is writing `pending_step` and clearing `update_ready` for slot *i*, child *i* is guaranteed to be blocked on `sem_wait(step_start[i])` — it hasn't been told to run yet.
- Once the parent calls `sem_post(step_start[i])`, the parent **does not touch slot *i* again** until it later calls `sem_wait(step_done[i])`.
- The child only writes `slot->update` and sets `update_ready = 1` *between* waking up from `step_start` and posting `step_done`.

So at any point in time, exactly one side (parent or child *i*) "owns" slot *i* for writing — the semaphores enforce that handoff, which is what makes an explicit lock unnecessary.

---

## One simulation step (US108)

1. Parent writes `sim_time_s` and `dt_s` into the global section.
2. For every flight that is **active** and whose `departure_time_s` has been reached, the parent: writes `pending_step`, clears `update_ready`, marks the flight as *departed*, and counts it toward `active_flights`.
3. If at least one flight is active this tick, the parent calls `sem_post(step_start[i])` **only for the flights it just counted** — flights not yet departed are skipped entirely (see below).
4. Each departed child wakes from `sem_wait(step_start)`, reads `sim_time_s` / `dt_s` / `pending_step` / `environment`, runs one physics step, writes its `update`, sets `update_ready = 1`, and calls `sem_post(step_done)`.
5. The parent calls `sem_wait(step_done[i])` for every flight it dispatched — this is the **barrier**: the parent blocks until every active flight has finished this step.
6. The parent reads each slot's `update`:
    - If `update_ready` is still 0 after the wait, the parent treats the flight as **"communication lost"** — it `SIGKILL`s the child, marks the flight inactive, and records a `"COMMUNICATION LOST"` outcome (with departure time and current time as "arrival" time).
    - Otherwise, it stores the update, logs telemetry if due, and increments that flight's step counter.
7. The safety thread checks for collisions on the freshly collected updates (US107); the parent then checks each flight's overall status (success / out of fuel / low altitude / etc.).
8. The parent advances `sim_time_s` by `dt_s` and moves to step **N+1**.

### Edge case: nobody has departed yet

If no flight's `departure_time_s` has been reached yet, step 2 finds zero active flights and the parent **skips steps 3–7 entirely** for this tick — no semaphore is posted or waited on. The parent simply advances the clock and checks again next tick. This means the semaphore barrier only "activates" once the first flight departs.

### Edge case: simulation shutdown

On shutdown, the parent sets `global.shutdown = 1` and wakes any children still blocked on `step_start` so they can exit their loop instead of waiting forever, before `waitpid`-ing on all of them and destroying the SHM/semaphores.

---

## Who implements what

| Piece | Owner | User story |
|-------|-------|------------|
| Create / destroy shared memory (`sim_shm_create`/`sim_shm_destroy`) | João | US105 |
| Shared memory layout (`sim_shared_t`, global + per-slot) | João | US105 |
| Step semaphores (`sim_sem_create`, open per slot, wait/post) | Vitor | US108 |
| Child loop: wait → read step → run physics → write slot → signal done | Vitor | US108 |
| Parent step loop: `sim_step_publish` / `sim_step_collect`, barrier, comm-lost handling | Vitor | US108 |

---

## Diagrams

- [simulator-global-sd.puml](simulator-global-sd.puml) — full run
- [us108-step-barrier-sd.puml](us108-step-barrier-sd.puml) — one step in detail