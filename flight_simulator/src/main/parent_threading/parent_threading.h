#ifndef PARENT_THREADING_H
#define PARENT_THREADING_H

int  parent_sim_ctx_init(parent_sim_ctx_t *ctx);
void parent_sim_ctx_destroy(parent_sim_ctx_t *ctx);

int  parent_run_oneshot_thread(void *(*start)(void *), parent_sim_ctx_t *ctx);
int  parent_any_active(parent_sim_ctx_t *ctx);

void *init_departure_times_thread(void *arg);
void *wake_children_on_shutdown_thread(void *arg);
void *kill_remaining_thread(void *arg);
void *report_aborted_thread(void *arg);

#endif /* PARENT_THREADING_H */
