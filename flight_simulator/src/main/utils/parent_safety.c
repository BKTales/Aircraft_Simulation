#include "simulator_includes.h"


static void broadcast_flight_terminated(int flight_id, const char *status)
{
    char json[256];
    snprintf(json, sizeof(json),
        "{\"type\":\"terminated\",\"id\":%d,\"status\":\"%s\",\"t\":%lld}",
        flight_id, status, global_sim_time_s);
    sse_server_broadcast(json);
}

void mark_comm_lost_on_publish_failure(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *reported = ctx->reported;
    int *departed = ctx->departed;
    flight_update_t *latest_updates = ctx->latest_updates;
    simulation_report_t *sim_report = ctx->sim_report;

    for (int i = 0; i < n_flights; i++) {
        if (active[i] && departed[i]
            && global_sim_time_s >= prog->flight_plans_info->plans[i].departure_time_s
            && !reported[i]) {
            kill(prog->flight_processes[i].pid, SIGKILL);
            active[i] = 0;
            long long dep_time = prog->flight_plans_info->plans[i].departure_time_s;
            long long plan_arr_time = prog->flight_plans_info->plans[i].arrival_time_s;

            simulation_report_record_flight(sim_report,
                prog->flight_plans_info->plans[i].legs, "COMMUNICATION LOST",
                prog->flight_plans_info->plans[i].aircraft_ptr,
                &latest_updates[i], dep_time, plan_arr_time);
            broadcast_flight_terminated(latest_updates[i].flight_id, "COMMUNICATION LOST");
            reported[i] = 1;
        }
    }
}

void mark_comm_lost_on_collect_failure(parent_sim_ctx_t *ctx)
{
    mark_comm_lost_on_publish_failure(ctx);
}

void check_collisions_detect(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *updated_this_tick = ctx->updated_this_tick;
    int *reported = ctx->reported;
    flight_update_t *latest_updates = ctx->latest_updates;
    int sim_step = ctx->sim_step;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || !updated_this_tick[i])
            continue;
        for (int j = i + 1; j < n_flights; j++) {
            if (!active[j] || !updated_this_tick[j])
                continue;

            const flight_update_t ui = latest_updates[i];
            const flight_update_t uj = latest_updates[j];

            double dist_m = haversine_distance(
                ui.latitude, ui.longitude,
                uj.latitude, uj.longitude);
            double alt_diff = fabs(ui.altitude_m - uj.altitude_m);

            if (dist_m < CLOSE_PLANE_X && alt_diff < CLOSE_PLANE_Y) {
                
                sim_log_critical_collision(ui.flight_id, uj.flight_id, dist_m, alt_diff);

                kill(prog->flight_processes[i].pid, SIGUSR1);
                kill(prog->flight_processes[j].pid, SIGUSR1);

                active[i] = active[j] = 0;
                failure_count += 2;
                reported[i] = reported[j] = 1;

                ctx->latest_updates[i] = ui;
                ctx->latest_updates[j] = uj;

                char json[400];
                snprintf(json, sizeof(json),
                    "{\"type\":\"collision\",\"id1\":%d,\"id2\":%d,\"dist_m\":%.1f,\"alt_diff\":%.1f}",
                    ui.flight_id, uj.flight_id, dist_m, alt_diff);
                sse_server_broadcast(json);

                pthread_mutex_lock(&sync->violation_mutex);
                if (sync->violation_queue_len < VIOLATION_QUEUE_MAX) {
                    pending_violation_t *pv =
                        &sync->violations[sync->violation_queue_len++];
                    pv->flight_i = i;
                    pv->flight_j = j;
                    pv->sim_step = sim_step;
                    pv->dist_m = dist_m;
                    pv->alt_diff = alt_diff;
                    pv->update_i = ui;
                    pv->update_j = uj;
                    sync->violation_pending = 1;
                    pthread_cond_signal(&sync->violation_cond);
                }
                pthread_mutex_unlock(&sync->violation_mutex);
            }
        }
    }
}

void check_flight_status(parent_sim_ctx_t *ctx)
{
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *reported = ctx->reported;
    flight_update_t *latest_updates = ctx->latest_updates;
    simulation_report_t *sim_report = ctx->sim_report;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || reported[i])
            continue;

        int terminate = 0;
        const char *status = "UNKNOWN";

        if (latest_updates[i].no_fuel) {
            sim_log_warn_out_of_fuel(latest_updates[i].flight_id);
            terminate = 1;
            status = "OUT OF FUEL";
        } else if (latest_updates[i].altitude_m < LOW_ALTITUDE
                && latest_updates[i].phase == CRUISE
                && latest_updates[i].speed_kt > 50) {
            sim_log_warn_low_altitude(latest_updates[i].flight_id);
            terminate = 1;
            status = "LOW ALTITUDE";
        }

        if (!terminate && !latest_updates[i].done)
            continue;

        if (terminate) {
            kill(prog->flight_processes[i].pid, SIGUSR1);
            failure_count++;
            broadcast_flight_terminated(latest_updates[i].flight_id, status);
        } else {
            status = "SUCCESS";
        }

        active[i] = 0;

        long long dep_time = prog->flight_plans_info->plans[i].departure_time_s;
        long long plan_arr_time = prog->flight_plans_info->plans[i].arrival_time_s;

        simulation_report_record_flight(sim_report,
            prog->flight_plans_info->plans[i].legs, status,
            prog->flight_plans_info->plans[i].aircraft_ptr, &latest_updates[i],
            dep_time, plan_arr_time);

        if (strcmp(status, "SUCCESS") == 0)
            sim_log_flight_success(latest_updates[i].flight_id, latest_updates[i].step);
        reported[i] = 1;
    }
}
