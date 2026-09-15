#include "simulator_includes.h"

volatile sig_atomic_t failure_count = 0;
long long global_sim_time_s = 0;

static int should_open_browser(void)
{
    const char *skip = getenv("FS_NO_BROWSER");
    return !(skip && skip[0] != '\0' && skip[0] != '0');
}

static void open_browser(const char *url)
{
    if (!should_open_browser() || !url || !url[0])
        return;

#if defined(_WIN32)
    {
        char cmd[640];
        snprintf(cmd, sizeof(cmd), "start \"\" \"%s\"", url);
        system(cmd);
    }
#else
    pid_t pid = fork();
    if (pid != 0)
        return;

#if defined(__APPLE__)
    execlp("open", "open", url, (char *)NULL);
#else
    {
        char script[896];
        snprintf(script, sizeof(script),
            "xdg-open '%s' 2>/dev/null || "
            "wslview '%s' 2>/dev/null || "
            "sensible-browser '%s' 2>/dev/null || "
            "gio open '%s' 2>/dev/null || "
            "cmd.exe /c start \"\" \"%s\" 2>/dev/null",
            url, url, url, url, url);
        execl("/bin/sh", "sh", "-c", script, (char *)NULL);
    }
#endif
    _exit(1);
#endif
}

static const char *resolve_viewer_html_path(char *resolved, size_t resolved_size)
{
    const char *env_path = getenv("FS_VIEWER_PATH");
    if (env_path && env_path[0] && access(env_path, R_OK) == 0) {
        if (realpath(env_path, resolved))
            return resolved;
        snprintf(resolved, resolved_size, "%s", env_path);
        return resolved;
    }

    static const char *candidates[] = {
        "server/viewer.html",
        "src/main/server/viewer.html",
        "flight_simulator/src/main/server/viewer.html",
        NULL
    };

    for (int i = 0; candidates[i]; i++) {
        if (access(candidates[i], R_OK) != 0)
            continue;
        if (realpath(candidates[i], resolved))
            return resolved;
        snprintf(resolved, resolved_size, "%s", candidates[i]);
        return resolved;
    }

    return NULL;
}

static void launch_viewer_ui(int port, const char *viewer_path)
{
    char url[128];
    snprintf(url, sizeof(url), "http://localhost:%d/", port);

    if (!viewer_path) {
        char msg[256];
        snprintf(msg, sizeof(msg), "Viewer HTML not found; open manually: %s", url);
        sim_log_error(msg);
        return;
    }

    usleep(250000);
    if (should_open_browser()) {
        open_browser(url);
        dprintf(STDOUT_FILENO,
                C_CYAN "[VIEWER] Opened browser at %s (viewer: %s)\n" C_RESET,
                url, viewer_path);
    } else {
        dprintf(STDOUT_FILENO,
                C_CYAN "[VIEWER] Live map available at %s (viewer: %s)\n" C_RESET,
                url, viewer_path);
    }
}

int sim_step_publish(parent_sim_ctx_t *ctx)
{
    sim_ipc_ctx_t *ipc = ctx->ipc;
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int *active = ctx->active;
    int *departed = ctx->departed;
    int *flight_steps = ctx->flight_steps;
    int n_active = 0;

    ipc->shm->global.sim_time_s = global_sim_time_s;
    ipc->shm->global.dt_s = DT_S;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i])
            continue;
        if (global_sim_time_s < prog->flight_plans_info->plans[i].departure_time_s)
            continue;

        sim_flight_slot_t *slot = &ipc->shm->slots[i];
        slot->pending_step = flight_steps[i];
        slot->update_ready = 0;
        departed[i] = 1;
        n_active++;
    }

    if (n_active == 0)
        return 0;

    ipc->shm->global.active_flights = n_active;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || !departed[i])
            continue;
        if (global_sim_time_s < prog->flight_plans_info->plans[i].departure_time_s)
            continue;
        if (sem_post(ipc->step_start[i]) != 0)
            return -1;
    }

    return n_active;
}

int sim_step_collect(parent_sim_ctx_t *ctx)
{
    sim_ipc_ctx_t *ipc = ctx->ipc;
    program_t *prog = ctx->prog;
    int n_flights = ctx->n_flights;
    int n_active = ctx->n_active;
    int *active = ctx->active;
    int *departed = ctx->departed;
    int *flight_steps = ctx->flight_steps;
    int *reported = ctx->reported;
    int *updated_this_tick = ctx->updated_this_tick;
    flight_update_t *latest_updates = ctx->latest_updates;
    simulation_report_t *sim_report = ctx->sim_report;

    if (n_active <= 0)
        return 0;

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || !departed[i])
            continue;
        if (global_sim_time_s < prog->flight_plans_info->plans[i].departure_time_s)
            continue;
        if (sem_wait(ipc->step_done[i]) != 0)
            return -1;
    }

    for (int i = 0; i < n_flights; i++) {
        if (!active[i] || !departed[i])
            continue;
        if (global_sim_time_s < prog->flight_plans_info->plans[i].departure_time_s)
            continue;

        sim_flight_slot_t *slot = &ipc->shm->slots[i];

        if (!slot->update_ready) {
            kill(prog->flight_processes[i].pid, SIGKILL);
            active[i] = 0;
            long long dep_time = prog->flight_plans_info->plans[i].departure_time_s;
            long long arr_time = global_sim_time_s;

            simulation_report_record_flight(sim_report,
                prog->flight_plans_info->plans[i].legs, "COMMUNICATION LOST",
                prog->flight_plans_info->plans[i].aircraft_ptr, 
                &latest_updates[i], 
                dep_time, 
                arr_time);
            {
                char term_json[256];
                snprintf(term_json, sizeof(term_json),
                    "{\"type\":\"terminated\",\"id\":%d,\"status\":\"COMMUNICATION LOST\",\"t\":%lld}",
                    latest_updates[i].flight_id, global_sim_time_s);
                sse_server_broadcast(term_json);
            }
            reported[i] = 1;
            continue;
        }

        latest_updates[i] = slot->update;
        updated_this_tick[i] = 1;

        if (sim_should_log_telemetry(flight_steps[i], global_sim_time_s, &latest_updates[i]))
            sim_log_telemetry_tick(flight_steps[i], global_sim_time_s, latest_updates[i]);
        flight_steps[i]++;
    }

    return n_active;
}

void handle_report_output(parent_sim_ctx_t *ctx)
{
    simulation_report_t *sim_report = ctx->sim_report;
    int n_flights = ctx->n_flights;

    char txt_filename[512], csv_filename[512];
    if (reports_path_prepare(txt_filename, sizeof(txt_filename),
                             csv_filename, sizeof(csv_filename), n_flights) != 0) {
        sim_log_error("Could not create reports directory.");
        return;
    }

    if (simulation_report_write_files(sim_report, txt_filename, csv_filename) != 0) {
        sim_log_error("Could not write report files.");
        return;
    }

    sim_log_reports_generated(txt_filename, csv_filename);

    const char *non_interactive = getenv("FS_NON_INTERACTIVE");
    if (non_interactive && non_interactive[0] != '\0' && non_interactive[0] != '0') {
        sim_log_non_interactive_keep();
        return;
    }

    char view_choice;
    dprintf(STDOUT_FILENO, "\nView final report? (y/n): ");
    if (scanf(" %c", &view_choice) == 1 && (view_choice == 'y' || view_choice == 'Y')) {
        sim_log_report_header();
        simulation_report_print_to_stream(sim_report, stdout);
        sim_log_report_footer();
    }

    char save_choice;
    dprintf(STDOUT_FILENO, "\nKeep these files? (y/n): ");
    if (scanf(" %c", &save_choice) == 1) {
        if (save_choice == 'n' || save_choice == 'N') {
            remove(txt_filename);
            remove(csv_filename);
            sim_log_files_deleted();
        } else {
            sim_log_files_saved();
        }
    }
}

int main(void)
{
    program_t *prog = malloc(sizeof(program_t));
    if (!prog) {
        sim_log_error("Failed to allocate memory for program");
        return -1;
    }
    memset(prog, 0, sizeof(*prog));

    if (init_program(prog) != 0) {
        sim_log_error("Failed to initialize program. Terminating...");
        return 1;
    }

    int n_flights = prog->flight_plans_info->count;

    parent_sim_ctx_t sim_ctx;
    memset(&sim_ctx, 0, sizeof(sim_ctx));

    if (parent_sim_ctx_init(&sim_ctx) != 0) {
        sim_log_error("Failed to init parent simulation context");
        clean_finish(prog);
        return 2;
    }

    sim_ctx.prog     = prog;
    sim_ctx.n_flights = n_flights;

    if (parent_run_oneshot_thread(init_departure_times_thread, &sim_ctx) != 0) {
        sim_log_error("init_departure_times thread failed");
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    if (!prog->flight_processes) {
        sim_log_error("flight_processes missing after init");
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    int run_id = (int)getpid();
    char run_id_buf[16];
    snprintf(run_id_buf, sizeof(run_id_buf), "%d", run_id);
    setenv(SIM_ENV_RUN_ID, run_id_buf, 1);

    sim_ipc_ctx_t ipc;
    if (sim_shm_create(&ipc, run_id, n_flights) != 0) {
        sim_log_error("Failed to create shared memory");
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }
    sim_ctx.ipc = &ipc;

    if (sim_sem_create(&ipc, run_id) != 0) {
        sim_log_error("Failed to create step semaphores");
        sim_shm_destroy(&ipc);
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    simulation_report_t sim_report;
    simulation_report_init(&sim_report, DT_S);
    sim_ctx.sim_report = &sim_report;

    sim_ctx.forked = fork_flights(&sim_ctx);

    sim_ctx.active          = calloc((size_t)n_flights, sizeof(int));
    sim_ctx.reported        = calloc((size_t)n_flights, sizeof(int));
    sim_ctx.flight_steps    = calloc((size_t)n_flights, sizeof(int));
    sim_ctx.departed        = calloc((size_t)n_flights, sizeof(int));
    sim_ctx.latest_updates  = calloc((size_t)n_flights, sizeof(flight_update_t));
    sim_ctx.updated_this_tick = calloc((size_t)n_flights, sizeof(int));

    if (!sim_ctx.active || !sim_ctx.reported || !sim_ctx.flight_steps || !sim_ctx.departed
        || !sim_ctx.latest_updates || !sim_ctx.updated_this_tick) {
        sim_log_error("Calloc failed");
        parent_run_oneshot_thread(wake_children_on_shutdown_thread, &sim_ctx);
        parent_run_oneshot_thread(kill_remaining_thread, &sim_ctx);
        sim_sem_destroy(&ipc);
        sim_shm_destroy(&ipc);
        simulation_report_free(&sim_report);
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    for (int i = 0; i < n_flights; i++)
        sim_ctx.active[i] = prog->flight_processes[i].plan.is_valid;

    sim_ctx.sim_step = 0;

    sim_thread_sync_t thread_sync;
    if (sim_thread_sync_init(&thread_sync) != 0) {
        sim_log_error("Failed to init thread sync");
        parent_run_oneshot_thread(wake_children_on_shutdown_thread, &sim_ctx);
        parent_run_oneshot_thread(kill_remaining_thread, &sim_ctx);
        sim_sem_destroy(&ipc);
        sim_shm_destroy(&ipc);
        simulation_report_free(&sim_report);
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    sim_thread_arg_t thread_arg = { .ctx = &sim_ctx, .sync = &thread_sync };
    pthread_t t_environment = 0, t_safety = 0, t_report = 0;

    if (sim_dedicated_threads_start(&thread_arg, &t_environment, &t_safety, &t_report) != 0) {
        sim_log_error("Failed to start dedicated threads");
        sim_thread_sync_destroy(&thread_sync);
        parent_run_oneshot_thread(wake_children_on_shutdown_thread, &sim_ctx);
        parent_run_oneshot_thread(kill_remaining_thread, &sim_ctx);
        sim_sem_destroy(&ipc);
        sim_shm_destroy(&ipc);
        simulation_report_free(&sim_report);
        parent_sim_ctx_destroy(&sim_ctx);
        clean_finish(prog);
        return 2;
    }

    int target_flight_id = 0;
    const char *target_env = getenv("FS_TARGET_FLIGHT_ID");
    if (target_env && target_env[0] != '\0')
        target_flight_id = atoi(target_env);

    const useconds_t wall_sleep_us = sim_wall_sleep_us(DT_S);

    const int sse_port = 8080;
    char viewer_path_buf[PATH_MAX];
    const char *viewer_path = resolve_viewer_html_path(viewer_path_buf, sizeof(viewer_path_buf));

    if (sse_server_start(sse_port, viewer_path) != 0) {
        sim_log_error("Failed to start SSE server");
    } else {
        launch_viewer_ui(sse_port, viewer_path);
    }

    

    while (1) {
        if (!parent_any_active(&sim_ctx))
            break;

        sim_ctx.advance_result = try_advance_time(&sim_ctx);
        if (sim_ctx.advance_result == -1)
            break;

        if (sim_ctx.advance_result == 0) {
            pthread_mutex_lock(&sim_ctx.mux);
            memset(sim_ctx.updated_this_tick, 0, (size_t)n_flights * sizeof(int));
            pthread_mutex_unlock(&sim_ctx.mux);

            sim_environment_before_publish(&sim_ctx, &thread_sync);

            sim_ctx.n_active = sim_step_publish(&sim_ctx);
            if (sim_ctx.n_active < 0) {
                mark_comm_lost_on_publish_failure(&sim_ctx);
            } else if (sim_ctx.n_active > 0) {
                sim_ctx.collect_result = sim_step_collect(&sim_ctx);
                if (sim_ctx.collect_result < 0) {
                    mark_comm_lost_on_collect_failure(&sim_ctx);
                } else {
                    sim_dedicated_threads_after_collect(&sim_ctx, &thread_sync);

                    char json[512];
                    for (int i = 0; i < n_flights; i++) {
                        if (!sim_ctx.updated_this_tick[i]) continue;
                        flight_update_t *u = &sim_ctx.latest_updates[i];
                        snprintf(json, sizeof(json),
                        "{\"type\":\"update\",\"t\":%lld,\"id\":%d,\"lat\":%.6f,\"lon\":%.6f,"
                        "\"alt\":%.1f,\"spd\":%.1f,\"fuel\":%.1f,\"phase\":%d,\"done\":%d,\"no_fuel\":%d}",
                        global_sim_time_s, u->flight_id, u->latitude, u->longitude,
                        u->altitude_m, u->speed_kt, u->fuel_kg, u->phase, u->done, u->no_fuel);
                        sse_server_broadcast(json);
                    }
                }
            }

            pthread_mutex_lock(&sim_ctx.mux);
            check_flight_status(&sim_ctx);
            pthread_mutex_unlock(&sim_ctx.mux);

            if (target_flight_id > 0) {
                for (int i = 0; i < n_flights; i++) {
                    if (sim_ctx.reported[i]
                            && sim_ctx.latest_updates[i].flight_id == target_flight_id) {
                        goto sim_loop_done;
                    }
                }
            }

            if (failure_count >= WARNING_THRESHOLD) {
                sim_log_abort((int)failure_count);
                break;
            }
        }

        if (wall_sleep_us > 0)
            usleep(wall_sleep_us);
        global_sim_time_s += DT_S;
        sim_ctx.sim_step++;
    }
sim_loop_done:
    sim_dedicated_threads_stop(&thread_sync, t_environment, t_safety, t_report);
    sim_thread_sync_destroy(&thread_sync);
    
    parent_run_oneshot_thread(wake_children_on_shutdown_thread, &sim_ctx);
    parent_run_oneshot_thread(kill_remaining_thread, &sim_ctx);

    int wstatus;
    for (int i = 0; i < n_flights; i++) {
        if (prog->flight_processes[i].plan.is_valid && prog->flight_processes[i].pid > 0)
            waitpid(prog->flight_processes[i].pid, &wstatus, 0);
    }

    sim_sem_destroy(&ipc);
    sim_shm_destroy(&ipc);

    
    sse_server_stop();
    parent_run_oneshot_thread(report_aborted_thread, &sim_ctx);

    sim_report.simulator_failure_count = (int)failure_count;

    sim_dedicated_threads_stop(&thread_sync, t_environment, t_safety, t_report);
    sim_thread_sync_destroy(&thread_sync);

    simulation_report_free(&sim_report);
    free(sim_ctx.active);
    free(sim_ctx.reported);
    free(sim_ctx.flight_steps);
    free(sim_ctx.departed);
    free(sim_ctx.latest_updates);
    free(sim_ctx.updated_this_tick);
    parent_sim_ctx_destroy(&sim_ctx);
    clean_finish(prog);
    return 0;
}