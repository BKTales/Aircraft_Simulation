#include "simulator_includes.h"

static int sem_open_slot(sem_t **out, const char *name)
{
    sem_unlink(name);
    //mode 0666 read + write
    *out = sem_open(name, O_CREAT | O_EXCL, 0666, 0);
    if (*out == SEM_FAILED) {
        perror(name);
        return -1;
    }
    return 0;
}

int sim_sem_create(sim_ipc_ctx_t *ctx, int run_id)
{
    if (!ctx || ctx->capacity <= 0)
        return -1;

    ctx->run_id = run_id;
    ctx->step_start = calloc((size_t)ctx->capacity, sizeof(sem_t *));
    ctx->step_done = calloc((size_t)ctx->capacity, sizeof(sem_t *));
    if (!ctx->step_start || !ctx->step_done) {
        perror("calloc sem arrays");
        free(ctx->step_start);
        free(ctx->step_done);
        ctx->step_start = NULL;
        ctx->step_done = NULL;
        return -1;
    }

    char name[64];
    for (int i = 0; i < ctx->capacity; i++) {
        snprintf(name, sizeof(name), SIM_SEM_START_FMT, run_id, i);
        if (sem_open_slot(&ctx->step_start[i], name) != 0) {
            sim_sem_destroy(ctx);
            return -1;
        }
        snprintf(name, sizeof(name), SIM_SEM_DONE_FMT, run_id, i);
        if (sem_open_slot(&ctx->step_done[i], name) != 0) {
            sim_sem_destroy(ctx);
            return -1;
        }
    }

    return 0;
}

int sim_sem_open_for_slot(sem_t **step_start, sem_t **step_done, int run_id, int slot_index)
{
    if (!step_start || !step_done)
        return -1;

    char name[64];
    snprintf(name, sizeof(name), SIM_SEM_START_FMT, run_id, slot_index);
    *step_start = sem_open(name, 0);
    if (*step_start == SEM_FAILED) {
        perror(name);
        return -1;
    }

    snprintf(name, sizeof(name), SIM_SEM_DONE_FMT, run_id, slot_index);
    *step_done = sem_open(name, 0);
    if (*step_done == SEM_FAILED) {
        perror(name);
        sem_close(*step_start);
        *step_start = NULL;
        return -1;
    }

    return 0;
}

void sim_sem_destroy(sim_ipc_ctx_t *ctx)
{
    if (!ctx)
        return;

    if (ctx->step_start && ctx->step_done && ctx->capacity > 0) {
        char name[64];
        int run_id = ctx->run_id;
        for (int i = 0; i < ctx->capacity; i++) {
            if (ctx->step_start[i]) {
                sem_close(ctx->step_start[i]);
                snprintf(name, sizeof(name), SIM_SEM_START_FMT, run_id, i);
                sem_unlink(name);
                ctx->step_start[i] = NULL;
            }
            if (ctx->step_done[i]) {
                sem_close(ctx->step_done[i]);
                snprintf(name, sizeof(name), SIM_SEM_DONE_FMT, run_id, i);
                sem_unlink(name);
                ctx->step_done[i] = NULL;
            }
        }
    }

    free(ctx->step_start);
    free(ctx->step_done);
    ctx->step_start = NULL;
    ctx->step_done = NULL;
}

int sim_ipc_run_id_from_env(void)
{
    const char *v = getenv(SIM_ENV_RUN_ID);
    if (!v || !*v)
        return -1;
    return atoi(v);
}
