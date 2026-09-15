#include "simulator_includes.h"

int fork_flights(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    simulation_report_t *sim_report = ctx->sim_report;
    int forked = 0;

    if (!prog || !prog->flight_processes || !prog->flight_plans_info) {
        sim_log_error("fork_flights: program not initialized");
        return 0;
    }

    for (int i = 0; i < n_flights; i++) {
        if (!prog->flight_processes[i].plan.is_valid) {
            sim_log_skip_flight(i);
            continue;
        }

        pid_t cpid = fork();
        if (cpid < 0) {
            sim_log_error("Fork failed");
            perror("fork");
            simulation_report_free(sim_report);
            exit(4);
        }
        if (cpid > 0) {
            prog->flight_processes[i].pid = cpid;
            forked++;
        } else {
            int res = flight_process_main(i, prog->flight_processes[i].plan);
            exit(res);
        }
    }
    return forked;
}
