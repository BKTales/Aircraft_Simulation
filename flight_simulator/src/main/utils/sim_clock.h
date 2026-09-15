#ifndef SIM_CLOCK_H
#define SIM_CLOCK_H

void init_departure_times(program_t *prog, int n_flights);
int  try_advance_time(parent_sim_ctx_t *ctx);
useconds_t sim_wall_sleep_us(long long sim_seconds);

#endif /* SIM_CLOCK_H */
