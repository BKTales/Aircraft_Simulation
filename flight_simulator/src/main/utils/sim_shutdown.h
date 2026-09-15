#ifndef SIM_SHUTDOWN_H
#define SIM_SHUTDOWN_H

void wake_children_on_shutdown(parent_sim_ctx_t *ctx);
void kill_remaining(parent_sim_ctx_t *ctx);
void report_aborted(parent_sim_ctx_t *ctx);

#endif /* SIM_SHUTDOWN_H */
