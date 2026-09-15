#include "simulator_includes.h"

sim_ipc_ctx_t *g_sim_ipc = NULL;

size_t sim_shm_bytes(int capacity)
{
    if (capacity < 0)
        return 0;
    return offsetof(sim_shared_t, slots) + (size_t)capacity * sizeof(sim_flight_slot_t);
}

int sim_shm_create(sim_ipc_ctx_t *ctx, int run_id, int capacity)
{
    if (!ctx || capacity <= 0)
        return -1;

    memset(ctx, 0, sizeof(*ctx));
    ctx->run_id = run_id;
    ctx->capacity = capacity;
    snprintf(ctx->shm_name, sizeof(ctx->shm_name), SIM_SHM_NAME_FMT, run_id);

    ctx->shm_size = sim_shm_bytes(capacity);
    shm_unlink(ctx->shm_name);

    ctx->shm_fd = shm_open(ctx->shm_name, O_CREAT | O_RDWR, 0666);
    if (ctx->shm_fd < 0) {
        perror("shm_open");
        return -1;
    }

    if (ftruncate(ctx->shm_fd, (off_t)ctx->shm_size) != 0) {
        perror("ftruncate");
        close(ctx->shm_fd);
        shm_unlink(ctx->shm_name);
        return -1;
    }

    ctx->shm = mmap(NULL, ctx->shm_size, PROT_READ | PROT_WRITE, MAP_SHARED, ctx->shm_fd, 0);
    if (ctx->shm == MAP_FAILED) {
        perror("mmap");
        close(ctx->shm_fd);
        shm_unlink(ctx->shm_name);
        ctx->shm = NULL;
        return -1;
    }

    memset(ctx->shm, 0, ctx->shm_size);
    ctx->shm->capacity = capacity;
    ctx->shm->global.dt_s = DT_S;

    for (int i = 0; i < capacity; i++)
        ctx->shm->slots[i].slot_index = i;

    g_sim_ipc = ctx;
    return 0;
}

void sim_shm_destroy(sim_ipc_ctx_t *ctx)
{
    if (!ctx)
        return;

    if (ctx->shm && ctx->shm != MAP_FAILED) {
        munmap(ctx->shm, ctx->shm_size);
        ctx->shm = NULL;
    }

    if (ctx->shm_fd >= 0) {
        close(ctx->shm_fd);
        ctx->shm_fd = -1;
    }

    if (ctx->shm_name[0] != '\0')
        shm_unlink(ctx->shm_name);

    if (g_sim_ipc == ctx)
        g_sim_ipc = NULL;
}
