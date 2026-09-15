#include "../simulator_includes.h"
#include "sim_dedicated_thread.h"
#include "weather_config.h"

void sim_record_violation_batch(parent_sim_ctx_t *ctx,
                                const pending_violation_t *batch,
                                size_t n)
{
    if (n == 0)
        return;

    program_t *prog = ctx->prog;
    simulation_report_t *sim_report = ctx->sim_report;

    for (size_t k = 0; k < n; k++) {
        const pending_violation_t *pv = &batch[k];
        const int i = pv->flight_i;
        const int j = pv->flight_j;
        const long long arr_time = global_sim_time_s;

        simulation_report_record_violation(sim_report, pv->sim_step, "COLLISION",
            &pv->update_i, &pv->update_j, pv->dist_m, pv->alt_diff);

        simulation_report_record_flight(sim_report,
            prog->flight_plans_info->plans[i].legs, "COLLISION",
            prog->flight_plans_info->plans[i].aircraft_ptr, &pv->update_i,
            prog->flight_plans_info->plans[i].departure_time_s, arr_time);

        simulation_report_record_flight(sim_report,
            prog->flight_plans_info->plans[j].legs, "COLLISION",
            prog->flight_plans_info->plans[j].aircraft_ptr, &pv->update_j,
            prog->flight_plans_info->plans[j].departure_time_s, arr_time);

        sim_log_safety_violation_recorded(pv->update_i.flight_id, pv->update_j.flight_id);
    }
}

static void sim_thread_sync_destroy_env(sim_thread_sync_t *sync)
{
    pthread_cond_destroy(&sync->env_done_cond);
    pthread_cond_destroy(&sync->env_cond);
    pthread_mutex_destroy(&sync->env_mutex);
}

int sim_thread_sync_init(sim_thread_sync_t *sync)
{
    memset(sync, 0, sizeof(*sync));

    if (pthread_mutex_init(&sync->violation_mutex, NULL) != 0)
        return -1;

    if (pthread_cond_init(&sync->violation_cond, NULL) != 0) {
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_mutex_init(&sync->done_mutex, NULL) != 0) {
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_cond_init(&sync->done_cond, NULL) != 0) {
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_mutex_init(&sync->step_mutex, NULL) != 0) {
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_cond_init(&sync->step_cond, NULL) != 0) {
        pthread_mutex_destroy(&sync->step_mutex);
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_cond_init(&sync->step_done_cond, NULL) != 0) {
        pthread_cond_destroy(&sync->step_cond);
        pthread_mutex_destroy(&sync->step_mutex);
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_mutex_init(&sync->env_mutex, NULL) != 0) {
        pthread_cond_destroy(&sync->step_done_cond);
        pthread_cond_destroy(&sync->step_cond);
        pthread_mutex_destroy(&sync->step_mutex);
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_cond_init(&sync->env_cond, NULL) != 0) {
        sim_thread_sync_destroy_env(sync);
        pthread_cond_destroy(&sync->step_done_cond);
        pthread_cond_destroy(&sync->step_cond);
        pthread_mutex_destroy(&sync->step_mutex);
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    if (pthread_cond_init(&sync->env_done_cond, NULL) != 0) {
        sim_thread_sync_destroy_env(sync);
        pthread_cond_destroy(&sync->step_done_cond);
        pthread_cond_destroy(&sync->step_cond);
        pthread_mutex_destroy(&sync->step_mutex);
        pthread_cond_destroy(&sync->done_cond);
        pthread_mutex_destroy(&sync->done_mutex);
        pthread_cond_destroy(&sync->violation_cond);
        pthread_mutex_destroy(&sync->violation_mutex);
        return -1;
    }

    return 0;
}

void sim_thread_sync_destroy(sim_thread_sync_t *sync)
{
    sim_thread_sync_destroy_env(sync);
    pthread_cond_destroy(&sync->step_done_cond);
    pthread_cond_destroy(&sync->step_cond);
    pthread_mutex_destroy(&sync->step_mutex);
    pthread_cond_destroy(&sync->done_cond);
    pthread_mutex_destroy(&sync->done_mutex);
    pthread_cond_destroy(&sync->violation_cond);
    pthread_mutex_destroy(&sync->violation_mutex);
}

void sim_environment_before_publish(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync)
{
    (void)ctx;

    pthread_mutex_lock(&sync->env_mutex);
    sync->env_done = 0;
    sync->env_pending = 1;
    sync->env_sim_time_s = global_sim_time_s;
    pthread_cond_signal(&sync->env_cond);

    while (!sync->env_done && !sync->sim_done)
        pthread_cond_wait(&sync->env_done_cond, &sync->env_mutex);

    pthread_mutex_unlock(&sync->env_mutex);
}

void sim_dedicated_threads_after_collect(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync)
{
    (void)ctx;

    pthread_mutex_lock(&sync->step_mutex);
    sync->step_scan_done = 0;
    sync->step_pending   = 1;
    pthread_cond_signal(&sync->step_cond);

    while (!sync->step_scan_done && !sync->sim_done)
        pthread_cond_wait(&sync->step_done_cond, &sync->step_mutex);

    pthread_mutex_unlock(&sync->step_mutex);
}

void *environment_dedicated_thread_fn(void *arg)
{
    sim_thread_arg_t  *targ = (sim_thread_arg_t *)arg;
    parent_sim_ctx_t  *ctx  = targ->ctx;
    sim_thread_sync_t *sync = targ->sync;
    weather_config_t   weather;

    memset(&weather, 0, sizeof(weather));
    const char *weather_path = getenv(SIM_ENV_WEATHER_FILE);
    if (weather_path && weather_path[0] != '\0') {
        if (weather_config_load(&weather, weather_path) != 0)
            fprintf(stderr, "[ENV] Failed to load weather config from %s\n", weather_path);
        else if (getenv("FS_VERBOSE_LOAD"))
            fprintf(stderr, "[ENV] Loaded %d weather record(s) from %s\n",
                    weather.record_count, weather_path);
    }

    for (;;) {
        pthread_mutex_lock(&sync->env_mutex);
        while (!sync->env_pending && !sync->sim_done)
            pthread_cond_wait(&sync->env_cond, &sync->env_mutex);

        if (sync->sim_done) {
            pthread_mutex_unlock(&sync->env_mutex);
            break;
        }

        long long sim_time_s = sync->env_sim_time_s;
        sync->env_pending = 0;
        pthread_mutex_unlock(&sync->env_mutex);

        sim_environment_t env;
        memset(&env, 0, sizeof(env));
        weather_config_lookup(&weather, sim_time_s, NAN, NAN, &env);

        if (ctx->ipc && ctx->ipc->shm)
            ctx->ipc->shm->global.environment = env;

        pthread_mutex_lock(&sync->env_mutex);
        sync->env_done = 1;
        pthread_cond_signal(&sync->env_done_cond);
        pthread_mutex_unlock(&sync->env_mutex);
    }

    weather_config_free(&weather);
    pthread_exit(NULL);
}

void *safety_dedicated_thread_fn(void *arg)
{
    sim_thread_arg_t  *targ = (sim_thread_arg_t *)arg;
    parent_sim_ctx_t  *ctx  = targ->ctx;
    sim_thread_sync_t *sync = targ->sync;

    for (;;) {
        pthread_mutex_lock(&sync->step_mutex);
        while (!sync->step_pending && !sync->sim_done)
            pthread_cond_wait(&sync->step_cond, &sync->step_mutex);

        if (sync->sim_done) {
            pthread_mutex_unlock(&sync->step_mutex);
            break;
        }

        sync->step_pending = 0;
        pthread_mutex_unlock(&sync->step_mutex);

        pthread_mutex_lock(&ctx->mux);
        check_collisions_detect(ctx, sync);
        pthread_mutex_unlock(&ctx->mux);

        pthread_mutex_lock(&sync->step_mutex);
        sync->step_scan_done = 1;
        pthread_cond_signal(&sync->step_done_cond);
        pthread_mutex_unlock(&sync->step_mutex);
    }

    pthread_exit(NULL);
}

void *report_dedicated_thread_fn(void *arg)
{
    sim_thread_arg_t  *targ = (sim_thread_arg_t *)arg;
    parent_sim_ctx_t  *ctx  = targ->ctx;
    sim_thread_sync_t *sync = targ->sync;

    for (;;) {
        pending_violation_t batch[VIOLATION_QUEUE_MAX];
        size_t n = 0;
        int done = 0;

        pthread_mutex_lock(&sync->violation_mutex);
        while (sync->violation_queue_len == 0 && !sync->sim_done)
            pthread_cond_wait(&sync->violation_cond, &sync->violation_mutex);

        n = sync->violation_queue_len;
        if (n > 0) {
            memcpy(batch, sync->violations, n * sizeof(pending_violation_t));
            sync->violation_queue_len = 0;
            sync->violation_pending = 0;
        }
        done = sync->sim_done;
        pthread_mutex_unlock(&sync->violation_mutex);

        if (n > 0)
            sim_record_violation_batch(ctx, batch, n);

        if (done) {
            handle_report_output(ctx);
            break;
        }
    }

    pthread_exit(NULL);
}

static void sim_dedicated_threads_broadcast_shutdown(sim_thread_arg_t *arg)
{
    pthread_mutex_lock(&arg->sync->done_mutex);
    arg->sync->sim_done = 1;
    pthread_cond_broadcast(&arg->sync->done_cond);
    pthread_mutex_unlock(&arg->sync->done_mutex);

    pthread_mutex_lock(&arg->sync->step_mutex);
    arg->sync->step_pending = 0;
    pthread_cond_broadcast(&arg->sync->step_cond);
    pthread_cond_broadcast(&arg->sync->step_done_cond);
    pthread_mutex_unlock(&arg->sync->step_mutex);

    pthread_mutex_lock(&arg->sync->env_mutex);
    arg->sync->env_pending = 0;
    pthread_cond_broadcast(&arg->sync->env_cond);
    pthread_cond_broadcast(&arg->sync->env_done_cond);
    pthread_mutex_unlock(&arg->sync->env_mutex);
}

int sim_dedicated_threads_start(sim_thread_arg_t *arg,
                                pthread_t        *t_environment,
                                pthread_t        *t_safety,
                                pthread_t        *t_report)
{
    *t_environment = 0;
    *t_safety = 0;
    *t_report = 0;

    if (pthread_create(t_environment, NULL, environment_dedicated_thread_fn, arg) != 0) {
        sim_log_error("Failed to create environment dedicated thread");
        return -1;
    }

    if (pthread_create(t_safety, NULL, safety_dedicated_thread_fn, arg) != 0) {
        sim_log_error("Failed to create safety dedicated thread");
        sim_dedicated_threads_broadcast_shutdown(arg);
        pthread_join(*t_environment, NULL);
        *t_environment = 0;
        return -1;
    }

    if (pthread_create(t_report, NULL, report_dedicated_thread_fn, arg) != 0) {
        sim_log_error("Failed to create report dedicated thread");
        sim_dedicated_threads_broadcast_shutdown(arg);
        pthread_join(*t_environment, NULL);
        pthread_join(*t_safety, NULL);
        *t_environment = 0;
        *t_safety = 0;
        return -1;
    }

    return 0;
}

void sim_dedicated_threads_stop(sim_thread_sync_t *sync,
                                pthread_t          t_environment,
                                pthread_t          t_safety,
                                pthread_t          t_report)
{
    pthread_mutex_lock(&sync->done_mutex);
    sync->sim_done = 1;
    pthread_cond_broadcast(&sync->done_cond);
    pthread_mutex_unlock(&sync->done_mutex);

    pthread_mutex_lock(&sync->step_mutex);
    sync->step_pending = 0;
    pthread_cond_broadcast(&sync->step_cond);
    pthread_cond_broadcast(&sync->step_done_cond);
    pthread_mutex_unlock(&sync->step_mutex);

    pthread_mutex_lock(&sync->env_mutex);
    sync->env_pending = 0;
    pthread_cond_broadcast(&sync->env_cond);
    pthread_cond_broadcast(&sync->env_done_cond);
    pthread_mutex_unlock(&sync->env_mutex);

    pthread_mutex_lock(&sync->violation_mutex);
    pthread_cond_broadcast(&sync->violation_cond);
    pthread_mutex_unlock(&sync->violation_mutex);

    if (t_environment)
        pthread_join(t_environment, NULL);
    if (t_safety)
        pthread_join(t_safety, NULL);
    if (t_report)
        pthread_join(t_report, NULL);
}
