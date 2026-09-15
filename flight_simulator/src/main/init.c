#include "simulator_includes.h"

validation_result_t validate_flight_plan(flight_plan_t *plan) {
    // Verifica também se o plan tem dados válidos (o load agora está no plan)
    if (!plan || !plan->aircraft_ptr) 
        return ERR_PAYLOAD;

    aircraft_t *ac = plan->aircraft_ptr;
    
    double total_payload = plan->load.total_payload_mass_kg;

    if (total_payload > ac->max_payload) {
        plan->is_valid = 0;
        return ERR_PAYLOAD;
    }

    for (int j = 0; j < plan->leg_count; j++) {
        leg_t *leg = &plan->legs[j];
        double leg_fuel = leg->fuel.quantity;

        if (leg_fuel > ac->fuel_capacity) {
            plan->is_valid = 0;
            return ERR_FUEL_LEG;
        }

        double tow = ac->eweight + leg_fuel + total_payload;
        if (tow > ac->mtow) {
            plan->is_valid = 0;
            return ERR_MTOW;
        }
    }

    plan->is_valid = 1;
    return VALID_OK;
}

int init_program(program_t* prog) {
    const char *env_dir = getenv("FS_FLIGHT_PLANS_DIR");

    const char *dir_path = (env_dir != NULL && env_dir[0] != '\0') ? env_dir : "../data/flight_plans";

    dprintf(STDERR_FILENO, "[INIT] flight_plans_dir = %s\n", dir_path);

    memset(&prog->network, 0, sizeof(prog->network));
    prog->aircraft_list.aircrafts = NULL;
    prog->aircraft_list.count = 0;

    prog->flight_plans_info = malloc(sizeof(flight_plan_list_t));
    if (prog->flight_plans_info == NULL) {
        printf("Failed to allocate memory for flight plans list\n");
        clean_finish(prog);
        return -1;
    }

    prog->flight_plans_info->plans = malloc(sizeof(flight_plan_t));
    if (prog->flight_plans_info->plans == NULL) {
        printf("Failed to allocate memory for flight plans\n");
        clean_finish(prog);
        return -1;
    }

    prog->flight_plans_info->count = 0;
    if (get_flight_plans(prog, dir_path) != 0) {
        printf("Failed to get flight plans from directory\n");
        clean_finish(prog);
        return -1;
    }

    for (int i = 0; i < prog->flight_plans_info->count; i++) {
        flight_plan_t *plan = &prog->flight_plans_info->plans[i];
        plan->is_valid = 1;

        if (!plan->aircraft_ptr) {
            dprintf(STDERR_FILENO,
                "[INIT] ERROR: missing embedded Aircraft for flight %d\n", plan->id);
            plan->is_valid = 0;
            continue;
        }

        for (int j = 0; j < plan->leg_count; j++) {
            leg_t *leg = &plan->legs[j];

            if (!leg->departure_airport_ptr || !leg->arrival_airport_ptr) {
                fprintf(stderr,
                    "[INIT] Flight %d invalid leg %d: missing embedded airport data (%s -> %s)\n",
                    plan->id, j,
                    leg->departure_id,
                    leg->arrival_id);
                plan->is_valid = 0;
                break;
            }
        }
    }

    for (int i = 0; i < prog->flight_plans_info->count; i++) {
        flight_plan_t *plan = &prog->flight_plans_info->plans[i];

        if (!plan->is_valid) continue;

        validation_result_t r = validate_flight_plan(plan);
        if (r != VALID_OK) {
            const char *reasons[] = {
                "", "fuel por leg", "fuel total", "payload", "MTOW"
            };
            dprintf(STDERR_FILENO,
                "[INIT] Flight %d invalid: %s\n", plan->id, reasons[r]);
        }
    }

    prog->flight_processes = malloc(sizeof(flight_process_t) * prog->flight_plans_info->count);
    if (prog->flight_processes == NULL) {
        printf("Failed to allocate memory for flight processes\n");
        clean_finish(prog);
        return -1;
    }

    if(prog->flight_plans_info != NULL && prog->flight_plans_info->count > 0){
        int num_flight_plans = prog->flight_plans_info->count;
        int invalid_count = 0;

        for(int i = 0; i < num_flight_plans; i++){
            prog->flight_processes[i] = (flight_process_t){0};
            prog->flight_processes[i].slot_index = i;
            prog->flight_processes[i].plan = prog->flight_plans_info->plans[i];

            if (prog->flight_processes[i].plan.aircraft_ptr == NULL || !prog->flight_processes[i].plan.is_valid) {
                dprintf(STDERR_FILENO, "[INIT] Flight process %d marked as invalid (embedded data or physics check)\n", i);
                invalid_count++;
                continue;
            }
        }

        if (invalid_count == num_flight_plans) {
            fprintf(stderr, "[INIT] All flights are invalid (embedded data or fuel/payload/MTOW). Terminating.\n");
            clean_finish(prog);
            return 1;
        }
    } else {
        printf("No flight plans loaded. Terminating...\n");
        clean_finish(prog);
        return 1;
    }

    return 0;
}

void clean_finish(program_t* prog) {
    if(prog == NULL) return;

    int flight_count = 0;
    if (prog->flight_plans_info != NULL) {
        flight_count = prog->flight_plans_info->count;
    }

    free_network(&prog->network);

    if (prog->aircraft_list.aircrafts != NULL) {
        free_aircraft_list(&prog->aircraft_list);
        prog->aircraft_list.aircrafts = NULL;
    }

    if(prog->flight_plans_info != NULL) {
        if(prog->flight_plans_info->plans != NULL) {
            for (int i = 0; i < flight_count; i++) {
                free_flight_plan(&prog->flight_plans_info->plans[i]);
            }
            free(prog->flight_plans_info->plans);
            prog->flight_plans_info->plans = NULL;
        }
        free(prog->flight_plans_info);
        prog->flight_plans_info = NULL;
    }

    if(prog->flight_processes != NULL) {
        free(prog->flight_processes);
        prog->flight_processes = NULL;
    }

    free(prog);
}
