#include "simulator_includes.h"

void wake_children_on_shutdown(parent_sim_ctx_t *ctx)
{
    sim_ipc_ctx_t *ipc = ctx->ipc;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *departed = ctx->departed;

    (void)n_flights;
    ipc->shm->global.shutdown = 1;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || !departed[i])
            continue;
        sem_post(ipc->step_start[i]);
    }
}

void kill_remaining(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;

    for (int i = 0; i < n_flights; i++)
        if (active[i])
            kill(prog->flight_processes[i].pid, SIGKILL);
}

void report_aborted(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *reported = ctx->reported;
    flight_update_t *latest_updates = ctx->latest_updates;
    simulation_report_t *sim_report = ctx->sim_report;

    for (int i = 0; i < n_flights; i++) {
        if (!prog->flight_processes[i].plan.is_valid)
            continue;
        if (!reported[i]){
            long long dep_time = prog->flight_plans_info->plans[i].departure_time_s;
            long long plan_arr_time = prog->flight_plans_info->plans[i].arrival_time_s;


            simulation_report_record_flight(sim_report,
                prog->flight_plans_info->plans[i].legs, "ABORTED",
                prog->flight_plans_info->plans[i].aircraft_ptr,
                &latest_updates[i], dep_time, plan_arr_time);
        }
    }
}
