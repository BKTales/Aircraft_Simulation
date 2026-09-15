#ifndef FLIGHT_H
#define FLIGHT_H

#include "ipc/sim_ipc.h"

void physics_state_init(flight_state_t *state, const flight_plan_t *plan, const aircraft_t *ac);

void weather_child_init(void);
void weather_child_shutdown(void);

void physics_update(flight_state_t *state, const flight_plan_t *plan,
                    const aircraft_t *ac, int dt_s,
                    const sim_environment_t *shm_env,
                    long long sim_time_s);


void populate_flight_update(flight_update_t *update, const flight_plan_t *plan,
                            const flight_state_t *state, int step);

#endif /* FLIGHT_H */