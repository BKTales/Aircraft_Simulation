#include "simulator_includes.h"

volatile sig_atomic_t stop_flag = 0;
volatile sig_atomic_t safety_violation_flag = 0;

void on_sigusr1(int sig) {
    (void)sig;
    sigset_t mask, old;
    sigfillset(&mask);
    sigprocmask(SIG_BLOCK, &mask, &old);
    safety_violation_flag = 1;
    sigprocmask(SIG_SETMASK, &old, NULL);
}

void on_sigint(int sig) {
    (void)sig;
    stop_flag = 1;
}

void install_child_handlers(void) {
    struct sigaction sa = {0};
    sa.sa_handler = on_sigusr1;
    sigaction(SIGUSR1, &sa, NULL);
    sa.sa_handler = on_sigint;
    sigaction(SIGINT, &sa, NULL);
}

int flight_process_main(int slot_index, flight_plan_t plan) {
    install_child_handlers();

    int run_id = sim_ipc_run_id_from_env();
    if (run_id < 0 || !g_sim_ipc || !g_sim_ipc->shm) {
        fprintf(stderr, "[flight %d] IPC not initialized\n", slot_index);
        return 1;
    }

    sem_t *step_start = NULL;
    sem_t *step_done = NULL;
    if (sim_sem_open_for_slot(&step_start, &step_done, run_id, slot_index) != 0)
        return 1;

    sim_shared_t *shm = g_sim_ipc->shm;
    sim_flight_slot_t *slot = &shm->slots[slot_index];

    slot->flight_id = plan.id;
    slot->slot_index = slot_index;
    slot->active = 1;

    int steps_left = MAX_STEPS;
    {
        const char *v = getenv("FLIGHT_TICKS");
        if (v && *v) {
            int n = atoi(v);
            if (n > 0) steps_left = n;
        }
    }

    weather_child_init();

    flight_state_t state;
    physics_state_init(&state, &plan, plan.aircraft_ptr);

    int initial_sent = 0;

    while (!stop_flag && !safety_violation_flag) {
        if (sem_wait(step_start) != 0)
            break;

        if (shm->global.shutdown)
            break;

        int step = slot->pending_step;
        int dt_s = shm->global.dt_s;

        flight_update_t update;
        memset(&update, 0, sizeof(update));

        if (step == 0 && !initial_sent) {
            populate_flight_update(&update, &plan, &state, step);
            update.flight_id = plan.id;
            initial_sent = 1;
        } else {
            physics_update(&state, &plan, plan.aircraft_ptr, dt_s,
                           &shm->global.environment,
                           shm->global.sim_time_s);
            populate_flight_update(&update, &plan, &state, step);
            update.flight_id = plan.id;
        }

        if (--steps_left <= 0)
            update.done = 1;

        slot->update = update;
        slot->update_ready = 1;

        if (sem_post(step_done) != 0)
            break;

        if (update.done)
            break;
    }

    slot->active = 0;
    weather_child_shutdown();
    sem_close(step_start);
    sem_close(step_done);

    return 0;
}
