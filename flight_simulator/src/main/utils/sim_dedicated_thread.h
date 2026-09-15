#ifndef SIM_DEDICATED_THREAD_H
#define SIM_DEDICATED_THREAD_H

#include <pthread.h>

#include "../flight_simulator.h"

struct parent_sim_ctx;

#define VIOLATION_QUEUE_MAX 16

typedef struct {
    int             flight_i;
    int             flight_j;
    int             sim_step;
    double          dist_m;
    double          alt_diff;
    flight_update_t update_i;
    flight_update_t update_j;
} pending_violation_t;

typedef struct sim_thread_sync {
    pthread_mutex_t violation_mutex;
    pthread_cond_t  violation_cond;
    int             violation_pending;

    pending_violation_t violations[VIOLATION_QUEUE_MAX];
    size_t              violation_queue_len;

    pthread_mutex_t done_mutex;
    pthread_cond_t  done_cond;
    int             sim_done;

    pthread_mutex_t step_mutex;
    pthread_cond_t  step_cond;
    pthread_cond_t  step_done_cond;
    int             step_pending;
    int             step_scan_done;

    pthread_mutex_t env_mutex;
    pthread_cond_t  env_cond;
    pthread_cond_t  env_done_cond;
    int             env_pending;
    int             env_done;
    long long       env_sim_time_s;
} sim_thread_sync_t;

typedef struct {
    struct parent_sim_ctx *ctx;
    sim_thread_sync_t     *sync;
} sim_thread_arg_t;

int  sim_thread_sync_init(sim_thread_sync_t *sync);
void sim_thread_sync_destroy(sim_thread_sync_t *sync);

void sim_environment_before_publish(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync);

void sim_dedicated_threads_after_collect(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync);

void sim_record_violation_batch(parent_sim_ctx_t *ctx,
                                const pending_violation_t *batch,
                                size_t n);

void *environment_dedicated_thread_fn(void *arg);
void *safety_dedicated_thread_fn(void *arg);
void *report_dedicated_thread_fn(void *arg);

int sim_dedicated_threads_start(sim_thread_arg_t *arg,
                                pthread_t        *t_environment,
                                pthread_t        *t_safety,
                                pthread_t        *t_report);

void sim_dedicated_threads_stop(sim_thread_sync_t *sync,
                                pthread_t          t_environment,
                                pthread_t          t_safety,
                                pthread_t          t_report);

#endif /* SIM_DEDICATED_THREAD_H */
