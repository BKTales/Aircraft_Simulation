#include "simulator_includes.h"
#include "utils/weather_config.h"

static weather_config_t g_child_weather;
static int              g_child_weather_ready = 0;

void weather_child_init(void)
{
    memset(&g_child_weather, 0, sizeof(g_child_weather));
    g_child_weather_ready = 0;

    const char *path = getenv(SIM_ENV_WEATHER_FILE);
    if (!path || path[0] == '\0')
        return;

    if (weather_config_load(&g_child_weather, path) == 0)
        g_child_weather_ready = (g_child_weather.record_count > 0);
}

void weather_child_shutdown(void)
{
    weather_config_free(&g_child_weather);
    g_child_weather_ready = 0;
}

static double clamp_value(double value, double min, double max) {
    if (value < min) return min;
    if (value > max) return max;
    return value;
}

static const double LANDING_ALTITUDE_EPSILON_M = 1.0;

static double interpolate_profile_ias(const flight_profile_point_t *profile,
                                      int count,
                                      double altitude_m) {
    if (!profile || count <= 0) return -1.0;

    if (altitude_m <= profile[0].altitude_m) return profile[0].ias_kt;
    for (int i = 0; i < count - 1; i++) {
        if (altitude_m <= profile[i + 1].altitude_m) {
            return interpolation(altitude_m,
                                 profile[i].altitude_m,
                                 profile[i + 1].altitude_m,
                                 profile[i].ias_kt,
                                 profile[i + 1].ias_kt);
        }
    }
    return profile[count - 1].ias_kt;
}

static double select_ias_target(const flight_state_t *state,
                                const leg_t *leg,
                                const aircraft_t *ac) {
    double ias_kt = -1.0;

    if (leg) {
        const flight_profile_point_t *profile = NULL;
        int profile_count = 0;

        if (state->phase == CLIMB) {
            profile = leg->profile.climb;
            profile_count = leg->profile.climb_count;
        } else if (state->phase == DESCEND) {
            profile = leg->profile.descend;
            profile_count = leg->profile.descend_count;
        }

        if (profile && profile_count > 0) {
            ias_kt = interpolate_profile_ias(profile, profile_count, state->altitude_m);
        }
    }

    if (ias_kt > 0.0) {
        if (ac && ac->vmo > 0.0) {
            double vmo_kt = ac->vmo / KNOTS_TO_MS;
            ias_kt = clamp_value(ias_kt, 0.0, vmo_kt);
        }
        return ias_kt;
    }

    double temp, pressure, density;
    get_atmosphere_strength(state->altitude_m, &temp, &pressure, &density);
    double mach = ac && ac->cruise_speed > 0.0 ? ac->cruise_speed : 0.78;
    if (ac && ac->mmo > 0.0 && mach > ac->mmo) mach = ac->mmo;
    double tas_ms = mach_to_ms(mach, state->altitude_m);
    double ias_ms = (density > 0.0) ? tas_ms * sqrt(density / SEA_LEVEL_DENSITY) : tas_ms;
    if (ac && ac->vmo > 0.0) ias_ms = clamp_value(ias_ms, 0.0, ac->vmo);
    return ias_ms / KNOTS_TO_MS;
}


void physics_state_init(flight_state_t *state, const flight_plan_t *plan, const aircraft_t *ac) {
    memset(state, 0, sizeof(*state));
    if (!plan || plan->leg_count == 0 || !plan->aircraft_ptr) return;

    const leg_t *leg = &plan->legs[0];

    const segment_t *seg = (leg->segment_count > 0) ? &leg->segments[0] : NULL;
    if (leg->departure_airport_ptr) {
        state->latitude = leg->departure_airport_ptr->location.latitude;
        state->longitude = leg->departure_airport_ptr->location.longitude;
        state->altitude_m = quantity_to_si_alt(&leg->departure_airport_ptr->location.altitude);
    } else if (seg) {
        state->latitude = seg->start.latitude;
        state->longitude = seg->start.longitude;
        state->altitude_m = quantity_to_si_alt(&seg->start.altitude);
    }

    state->fuel_kg = leg->fuel.quantity;
    state->mass_kg = ac->eweight + plan->load.total_payload_mass_kg + state->fuel_kg;

    
    state->phase = CLIMB;
    if (seg) {
        if (strcmp(seg->mode, "cruise") == 0)  state->phase = CRUISE;
        if (strcmp(seg->mode, "descend") == 0) state->phase = DESCEND;
    }

    if (seg && state->segment_index < leg->segment_count) {
        state->bearing_rad = bearing_to(state->latitude, state->longitude,
                                        seg->end.latitude, seg->end.longitude);
    } else if (leg->arrival_airport_ptr) {
        state->bearing_rad = bearing_to(state->latitude, state->longitude,
                                        leg->arrival_airport_ptr->location.latitude,
                                        leg->arrival_airport_ptr->location.longitude);
    }

    if (leg->arrival_airport_ptr) {
        state->dist_to_next_m = haversine_distance(state->latitude, state->longitude,
                                                   leg->arrival_airport_ptr->location.latitude,
                                                   leg->arrival_airport_ptr->location.longitude);
    } else if (seg) {
        state->dist_to_next_m = haversine_distance(state->latitude, state->longitude,
                                                   seg->end.latitude, seg->end.longitude);
    }
}

static void resolve_wind_for_step(const flight_plan_t *plan,
                                  const flight_state_t *state,
                                  const sim_environment_t *shm_env,
                                  long long sim_time_s,
                                  int *wind_dir_deg,
                                  double *wind_speed_ms,
                                  int *wind_valid)
{
    *wind_valid = 0;
    *wind_dir_deg = 0;
    *wind_speed_ms = 0.0;

    if (!plan || !state)
        return;

    const leg_t *leg = &plan->legs[state->leg_index];
    const segment_t *seg = (state->segment_index < leg->segment_count)
        ? &leg->segments[state->segment_index]
        : NULL;

    if (seg && seg->has_wind) {
        *wind_valid = 1;
        *wind_dir_deg = seg->wind_direction_deg;
        *wind_speed_ms = seg->wind_speed_ms;
        return;
    }

    if (g_child_weather_ready) {
        sim_environment_t local;
        memset(&local, 0, sizeof(local));
        if (weather_config_lookup(&g_child_weather,
                                  sim_time_s,
                                  state->latitude,
                                  state->longitude,
                                  &local) == 1 && local.valid) {
            *wind_valid = 1;
            *wind_dir_deg = local.wind_direction_deg;
            *wind_speed_ms = local.wind_speed_ms;
        }
        return;
    }

    if (shm_env && shm_env->valid) {
        *wind_valid = 1;
        *wind_dir_deg = shm_env->wind_direction_deg;
        *wind_speed_ms = shm_env->wind_speed_ms;
    }
}

static void advance_with_wind(double *latitude,
                              double *longitude,
                              double bearing_rad,
                              double tas_horizontal,
                              double dt,
                              int wind_dir_deg,
                              double wind_speed_ms,
                              int wind_valid)
{
    double air_e = tas_horizontal * sin(bearing_rad);
    double air_n = tas_horizontal * cos(bearing_rad);
    double ground_e = air_e;
    double ground_n = air_n;
    double ground_speed = tas_horizontal;
    double ground_track = bearing_rad;

    if (wind_valid && wind_speed_ms > 0.0) {
        double to_rad = (wind_dir_deg + 180.0) * M_PI / 180.0;
        double wind_e = wind_speed_ms * sin(to_rad);
        double wind_n = wind_speed_ms * cos(to_rad);
        ground_e = air_e + wind_e;
        ground_n = air_n + wind_n;
        ground_speed = sqrt(ground_e * ground_e + ground_n * ground_n);
        if (ground_speed > 1e-6)
            ground_track = atan2(ground_e, ground_n);
    }

    advance_position(latitude, longitude, ground_track, ground_speed * dt);
}

void physics_update(flight_state_t *state, const flight_plan_t *plan,
                    const aircraft_t *ac, int dt_s,
                    const sim_environment_t *shm_env,
                    long long sim_time_s) {
    if (!state || !plan || !ac || state->done) return;
    double dt = (double)dt_s;

    const leg_t *leg = &plan->legs[state->leg_index];
    state->mass_kg = ac->eweight + plan->load.total_payload_mass_kg + state->fuel_kg;

    if (state->leg_index >= plan->leg_count) { state->done = 1; return; }
    int terminal_segment_count = leg->segment_count + (leg->arrival_airport_ptr ? 1 : 0);
    if (state->segment_index >= terminal_segment_count) { state->done = 1; return; }
    const segment_t *seg = (state->segment_index < leg->segment_count)
        ? &leg->segments[state->segment_index]
        : NULL;

    double target_lat = 0.0;
    double target_lon = 0.0;
    double target_alt_m = 0.0;
    if (seg) {
        target_lat = seg->end.latitude;
        target_lon = seg->end.longitude;
        target_alt_m = quantity_to_si_alt(&seg->end.altitude);
    } else if (leg->arrival_airport_ptr) {
        target_lat = leg->arrival_airport_ptr->location.latitude;
        target_lon = leg->arrival_airport_ptr->location.longitude;
        target_alt_m = quantity_to_si_alt(&leg->arrival_airport_ptr->location.altitude);
    }
    double dist_to_target = haversine_distance(state->latitude, state->longitude, target_lat, target_lon);

    double final_lat = target_lat;
    double final_lon = target_lon;
    double final_alt_m = target_alt_m;
    if (leg->arrival_airport_ptr) {
        final_lat = leg->arrival_airport_ptr->location.latitude;
        final_lon = leg->arrival_airport_ptr->location.longitude;
        final_alt_m = quantity_to_si_alt(&leg->arrival_airport_ptr->location.altitude);
    }

    int has_fuel = (state->fuel_kg > 0.0);
    if (!has_fuel) {
        state->fuel_kg = 0.0;
        state->no_fuel = 1;
        state->phase = DESCEND;
    }

    double temp, pressure, density;
    get_atmosphere_strength(state->altitude_m, &temp, &pressure, &density);
    double speed_of_sound = sqrt(GAMMA_AIR * GAS_CONSTANT * temp);

    if (!seg && leg->arrival_airport_ptr) {
        state->phase = DESCEND;
    } else if (seg) {
        if (strcmp(seg->mode, "cruise") == 0) state->phase = CRUISE;
        if (strcmp(seg->mode, "descend") == 0) state->phase = DESCEND;
        if (strcmp(seg->mode, "climb") == 0) state->phase = CLIMB;
    }

    double ias_kt = select_ias_target(state, leg, ac);
    double ias_ms = ias_kt * KNOTS_TO_MS;
    double mach = get_mach_from_ias(ias_ms / KNOTS_TO_MS, density);
    if (ac->mmo > 0.0 && mach > ac->mmo) mach = ac->mmo;
    double tas = mach * speed_of_sound;
    state->velocity_ms = tas;
    double cl = calculate_cl(state->mass_kg, density, tas, ac->wing_area);
    double cd0 = interpolate_cdrag(ac, mach);
    double cd = calculate_cd(cd0, cl, ac->aspect_ratio, ac->e);
    double drag = calculate_drag_force(cd, density, tas, ac->wing_area);

    double thrust = 0.0;
    if (has_fuel) {
        if (state->phase == CRUISE) thrust = drag;
        else if (state->phase == DESCEND) thrust = thrust_at_altitude(ac, tas, state->altitude_m) * 0.1;
        else thrust = thrust_at_altitude(ac, tas, state->altitude_m);
    }

    double dhdt = 0.0;
    if (state->phase == CLIMB) {
        dhdt = (thrust - drag) * tas / (state->mass_kg * GRAVITY);
        if (state->altitude_m + dhdt * dt >= target_alt_m) {
            state->altitude_m = target_alt_m;
            state->phase = CRUISE;
            dhdt = 0.0;
        }
    } else if (state->phase == DESCEND) {
        dhdt = (thrust - drag) * tas / (state->mass_kg * GRAVITY);

        double descent_time = (tas > 1e-6) ? dist_to_target / tas : dt;
        if (descent_time < dt) descent_time = dt;
        double required_dhdt = (target_alt_m - state->altitude_m) / descent_time;
        if (required_dhdt > 0.0) required_dhdt = 0.0;

        if (dhdt > 0.0 || dhdt < required_dhdt) {
            dhdt = required_dhdt;
        }

        if (state->altitude_m + dhdt * dt <= target_alt_m) {
            state->altitude_m = target_alt_m;
            dhdt = 0.0;
        }
    }

    double clmp = dhdt / (tas > 0.0 ? tas : 1.0);
    if (clmp > 1.0) clmp = 1.0;
    if (clmp < -1.0) clmp = -1.0;
    double tas_horizontal = tas * cos(asin(clmp));
    double horizontal_distance = tas_horizontal * dt;

    int wind_dir_deg = 0;
    double wind_speed_ms = 0.0;
    int wind_valid = 0;
    resolve_wind_for_step(plan, state, shm_env, sim_time_s,
                          &wind_dir_deg, &wind_speed_ms, &wind_valid);

    if (dist_to_target > horizontal_distance) {
        advance_with_wind(&state->latitude, &state->longitude, state->bearing_rad,
                          tas_horizontal, dt, wind_dir_deg, wind_speed_ms, wind_valid);
    } else {
        state->latitude = target_lat;
        state->longitude = target_lon;
    }

    state->altitude_m += dhdt * dt;
    if (state->altitude_m < 0.0) state->altitude_m = 0.0;



    double fuel_burn = fuel_rate_kgs(thrust, ac->tsfc) * dt;
    if (fuel_burn > state->fuel_kg) fuel_burn = state->fuel_kg;
    state->fuel_kg -= fuel_burn;

    if (!state->done && dist_to_target <= horizontal_distance + 1.0 && fabs(state->altitude_m - target_alt_m) <= 10.0) {
       state->segment_index++;

        if (state->segment_index >= terminal_segment_count)
        {
            /* terminou a leg atual */
            state->leg_index++;

            /* terminou o plano inteiro */
            if (state->leg_index >= plan->leg_count)
            {
                state->done = 1;
                state->dist_to_next_m = 0.0;
                return;
            }

            /* inicializar próxima leg */
            const leg_t *new_leg = &plan->legs[state->leg_index];

            state->segment_index = 0;

            /* combustível da nova leg */
            state->fuel_kg = new_leg->fuel.quantity;

            /* massa da nova leg */
            state->mass_kg =
                ac->eweight +
                plan->load.total_payload_mass_kg +
                state->fuel_kg;

            state->no_fuel = 0;
            state->velocity_ms = 0.0;
            if (new_leg->segment_count > 0)
            {
                const segment_t *first_seg = &new_leg->segments[0];

                if (strcmp(first_seg->mode, "climb") == 0)
                    state->phase = CLIMB;
                else if (strcmp(first_seg->mode, "cruise") == 0)
                    state->phase = CRUISE;
                else
                    state->phase = DESCEND;

                state->bearing_rad =
                    bearing_to(
                        state->latitude,
                        state->longitude,
                        first_seg->end.latitude,
                        first_seg->end.longitude);

                state->dist_to_next_m =
                    haversine_distance(
                        state->latitude,
                        state->longitude,
                        first_seg->end.latitude,
                        first_seg->end.longitude);
            }
            else if (new_leg->arrival_airport_ptr)
            {
                state->bearing_rad =
                    bearing_to(
                        state->latitude,
                        state->longitude,
                        new_leg->arrival_airport_ptr->location.latitude,
                        new_leg->arrival_airport_ptr->location.longitude);

                state->dist_to_next_m =
                    haversine_distance(
                        state->latitude,
                        state->longitude,
                        new_leg->arrival_airport_ptr->location.latitude,
                        new_leg->arrival_airport_ptr->location.longitude);
            }

            /* próximo tick começa a nova leg */
            return;
        }
    }

    double next_target_lat = target_lat;
    double next_target_lon = target_lon;

    if (state->segment_index < leg->segment_count) {
        const segment_t *next_seg = &leg->segments[state->segment_index];
        next_target_lat = next_seg->end.latitude;
        next_target_lon = next_seg->end.longitude;
        state->bearing_rad = bearing_to(state->latitude, state->longitude,
                                        next_target_lat, next_target_lon);
    } else if (leg->arrival_airport_ptr) {
        next_target_lat = leg->arrival_airport_ptr->location.latitude;
        next_target_lon = leg->arrival_airport_ptr->location.longitude;
        state->bearing_rad = bearing_to(state->latitude, state->longitude,
                                        next_target_lat, next_target_lon);
    }

    double dist_horizontal = haversine_distance(state->latitude, state->longitude, final_lat, final_lon);

    double delta_alt = final_alt_m - state->altitude_m;

    double dist_3d = sqrt(
        dist_horizontal * dist_horizontal +
        delta_alt * delta_alt
    );

    state->dist_to_next_m = dist_3d;
                                            
}

void populate_flight_update(flight_update_t *update, const flight_plan_t *plan, const flight_state_t *state, int step) {
    update->flight_id = plan->id;
    update->step = step;
    update->latitude = state->latitude;
    update->longitude = state->longitude;
    update->altitude_m = state->altitude_m;

    update->speed_kt = state->velocity_ms / KNOTS_TO_MS;

    update->fuel_kg = state->fuel_kg;
    update->done = state->done;
    update->no_fuel = state->no_fuel;
    update->phase = state->phase;
    update->remaining_distance_m = state->dist_to_next_m;
}
