#ifndef SIM_IPC_H
#define SIM_IPC_H

#define SIM_SHM_NAME_FMT   "/aisafe_%d_shm"
#define SIM_SEM_START_FMT  "/aisafe_%d_f%d_start"
#define SIM_SEM_DONE_FMT   "/aisafe_%d_f%d_done"
#define SIM_ENV_RUN_ID       "FS_RUN_ID"
#define SIM_ENV_WEATHER_FILE "FS_WEATHER_FILE"

#define ENV_SOURCE_NONE         0
#define ENV_SOURCE_WEATHER_FILE 1
#define ENV_SOURCE_SEGMENT      2

typedef struct sim_environment {
    int    valid;
    int    wind_direction_deg;
    double wind_speed_ms;
    int    source;
} sim_environment_t;

typedef struct sim_global {
    int step;
    int sim_time_s;
    int dt_s;
    int active_flights;
    int shutdown;
    sim_environment_t environment;
} sim_global_t;

typedef struct sim_flight_slot {
    int flight_id;
    int slot_index;
    int active;
    int departed;
    int pending_step;
    int update_ready;
    flight_update_t update;
} sim_flight_slot_t;

typedef struct sim_shared {
    sim_global_t global;
    int capacity;
    sim_flight_slot_t slots[];
} sim_shared_t;

typedef struct sim_ipc_ctx {
    int run_id;
    int capacity;
    sim_shared_t *shm;
    size_t shm_size;
    int shm_fd;
    sem_t **step_start;
    sem_t **step_done;
    char shm_name[64];
} sim_ipc_ctx_t;

extern sim_ipc_ctx_t *g_sim_ipc;

size_t sim_shm_bytes(int capacity);

int sim_shm_create(sim_ipc_ctx_t *ctx, int run_id, int capacity);
void sim_shm_destroy(sim_ipc_ctx_t *ctx);

int sim_sem_create(sim_ipc_ctx_t *ctx, int run_id);
int sim_sem_open_for_slot(sem_t **step_start, sem_t **step_done, int run_id, int slot_index);
void sim_sem_destroy(sim_ipc_ctx_t *ctx);

int sim_ipc_run_id_from_env(void);

#endif /* SIM_IPC_H */
