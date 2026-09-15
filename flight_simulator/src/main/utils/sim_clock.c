#include "simulator_includes.h"

useconds_t sim_wall_sleep_us(long long sim_seconds)
{
    if (sim_seconds <= 0 || getenv("FS_NO_WALL_SLEEP"))
        return 0;
    return (useconds_t)((unsigned long long)sim_seconds
            * (unsigned long long)STEP_WALL_MS_PER_30_SIM_S * 1000ULL / 30ULL);
}

static void sync_departure_times_to_processes(program_t *prog, int n_flights)
{
    if (!prog || !prog->flight_processes)
        return;

    for (int i = 0; i < n_flights; i++)
        prog->flight_processes[i].plan.departure_time_s =
            prog->flight_plans_info->plans[i].departure_time_s;
}

void init_departure_times(program_t *prog, int n_flights)
{
    long long min_departure_s = LLONG_MAX;

    for (int i = 0; i < n_flights; i++) {
        flight_plan_t *fp = &prog->flight_plans_info->plans[i];
        fp->departure_time_s = datetime_to_epoch_s(fp->departure_time);
        if (!fp->is_valid)
            continue;
        if (fp->departure_time_s < min_departure_s)
            min_departure_s = fp->departure_time_s;
    }
    global_sim_time_s = (min_departure_s == LLONG_MAX) ? 0 : min_departure_s;
    sync_departure_times_to_processes(prog, n_flights);
}

int try_advance_time(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *departed = ctx->departed;

    for (int i = 0; i < n_flights; i++)
        if (active[i] && departed[i])
            return 0;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || departed[i])
            continue;
        if (prog->flight_plans_info->plans[i].departure_time_s <= global_sim_time_s)
            return 0;
    }

    long long next_departure = LLONG_MAX;
    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || departed[i])
            continue;
        long long dep = prog->flight_plans_info->plans[i].departure_time_s;
        if (dep < next_departure)
            next_departure = dep;
    }

    if (next_departure == LLONG_MAX)
        return -1;

    return 1;
}
