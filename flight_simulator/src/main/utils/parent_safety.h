#ifndef PARENT_SAFETY_H
#define PARENT_SAFETY_H

typedef struct sim_thread_sync sim_thread_sync_t;

void check_collisions_detect(parent_sim_ctx_t *ctx, sim_thread_sync_t *sync);
void check_flight_status(parent_sim_ctx_t *ctx);
void mark_comm_lost_on_publish_failure(parent_sim_ctx_t *ctx);
void mark_comm_lost_on_collect_failure(parent_sim_ctx_t *ctx);

#endif /* PARENT_SAFETY_H */
