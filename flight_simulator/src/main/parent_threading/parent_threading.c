#include "simulator_includes.h"

int parent_sim_ctx_init(parent_sim_ctx_t *ctx)
{
    if (!ctx)
        return -1;

    if (pthread_mutex_init(&ctx->mux, NULL) != 0)
        return -1;

    return 0;
}

void parent_sim_ctx_destroy(parent_sim_ctx_t *ctx)
{
    if (!ctx)
        return;

    pthread_mutex_destroy(&ctx->mux);
}

int parent_run_oneshot_thread(void *(*start)(void *), parent_sim_ctx_t *ctx)
{
    pthread_t th;
    if (pthread_create(&th, NULL, start, ctx) != 0)
        return -1;
    if (pthread_join(th, NULL) != 0)
        return -1;
    return 0;
}

int parent_any_active(parent_sim_ctx_t *ctx)
{
    for (int i = 0; i < ctx->n_flights; i++)
        if (ctx->active[i])
            return 1;
    return 0;
}

void *init_departure_times_thread(void *arg)
{
    parent_sim_ctx_t *ctx = (parent_sim_ctx_t *)arg;
    init_departure_times(ctx->prog, ctx->n_flights);
    return NULL;
}

void *wake_children_on_shutdown_thread(void *arg)
{
    parent_sim_ctx_t *ctx = (parent_sim_ctx_t *)arg;
    wake_children_on_shutdown(ctx);
    return NULL;
}

void *kill_remaining_thread(void *arg)
{
    parent_sim_ctx_t *ctx = (parent_sim_ctx_t *)arg;
    kill_remaining(ctx);
    return NULL;
}

void *report_aborted_thread(void *arg)
{
    parent_sim_ctx_t *ctx = (parent_sim_ctx_t *)arg;
    report_aborted(ctx);
    return NULL;
}
