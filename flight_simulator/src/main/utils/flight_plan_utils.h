#ifndef FLIGHT_PLAN_UTILS_HEADER
#define FLIGHT_PLAN_UTILS_HEADER

measure_t parse_measure(cJSON *obj, const char *value_key);
quantity_t parse_quantity(cJSON *obj);
position_t parse_position(cJSON *obj);
leg_t parse_leg(cJSON *obj);
flight_plan_t parse_flight_plan(const char *json_str);
flight_profile_point_t *parse_profile_array(cJSON *arr, int *out_count) ;
void free_flight_plan(flight_plan_t *plan);

char* read_flight_plan(const char* path);
int get_flight_plans(program_t* prog, const char* dir_path);

void print_flight_plan(flight_plan_t plan);
void print_flight_plans(program_t* prog);

#endif // FLIGHT_PLAN_UTILS_HEADER