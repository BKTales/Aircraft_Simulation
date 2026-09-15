#ifndef FLIGHT_SIMULATOR_HEADER
#define FLIGHT_SIMULATOR_HEADER

/* Include simulator_includes.h before this header (types only here). */

#define C_BOLD    "\x1b[1m"
#define C_RED     "\x1b[31m"
#define C_GREEN   "\x1b[32m"
#define C_YELLOW  "\x1b[33m"
#define C_BLUE    "\x1b[34m"
#define C_MAGENTA "\x1b[35m"
#define C_CYAN    "\x1b[36m"
#define C_RESET   "\033[0m"

#define READ_END  0
#define WRITE_END 1
#define DT_S                      1
#define TELEMETRY_LOG_INTERVAL_S  900
#define MAX_STEPS                 10000
#define WARNING_THRESHOLD         3
#define STEP_WALL_MS_PER_30_SIM_S 100

#define CLIMB        0
#define CRUISE       1
#define DESCEND      2
#define LOW_ALTITUDE 228

#define CLOSE_PLANE_X 5000
#define CLOSE_PLANE_Y 300


typedef struct sim_datetime
{
    int year;
    int month;  /* 1-12  */
    int day;    /* 1-31  */
    int hour;   /* 0-23  */
    int min;    /* 0-59  */
    int sec;    /* 0-59  */
} sim_datetime_t;

sim_datetime_t parse_datetime(const char *str);
void           advance_day(sim_datetime_t *dt);
int            datetime_to_seconds(sim_datetime_t dt);   /* HH:MM:SS → segundos do dia */
long long      datetime_to_epoch_s(sim_datetime_t dt);   /* segundos absolutos (multi-dia) */
void           datetime_to_str(sim_datetime_t dt, char *buf, int buf_size);
int            datetime_cmp(sim_datetime_t a, sim_datetime_t b);



typedef struct flight_profile_point {
    double altitude_m;
    double ias_kt;
} flight_profile_point_t;

typedef struct flight_profile {
    flight_profile_point_t *climb;
    int climb_count;
    flight_profile_point_t *descend;
    int descend_count;
} flight_profile_t;


typedef struct tick
{
    int step;
    int dt_s;
} tick_t;

typedef struct flight_update
{
    int    flight_id;
    int    step;
    double latitude;
    double longitude;
    double altitude_m;
    double speed_kt;
    double fuel_kg;
    double remaining_distance_m;
    int    done;
    int    no_fuel;
    int    phase;   /* 0 climb, 1 cruise, 2 descend */
} flight_update_t;


typedef struct measure
{
    double value;
    char   unit[16];
} measure_t;

typedef struct quantity
{
    double quantity;
    char   unit[16];
} quantity_t;


typedef struct position
{
    double     latitude;
    double     longitude;
    quantity_t altitude;
} position_t;

typedef struct airport
{
    char       id[8];
    char       name[128];
    char       town[64];
    char       country[64];
    position_t location;
} airport_t;

typedef struct airport_list
{
    airport_t *airports;
    int        count;
} airport_list_t;

typedef struct network
{
    char           id[32];
    char           description[128];
    airport_list_t airports;
} network_t;


typedef struct segment
{
    char       mode[16];
    position_t start;
    position_t end;
    int        has_wind;
    int        wind_direction_deg;
    double     wind_speed_ms;
} segment_t;

typedef struct load
{
    int    passenger_count;
    double passenger_weight_kg;
    double cargo_weight_kg;
    double total_payload_mass_kg;
} load_t;

typedef struct leg
{
    airport_t       *departure_airport_ptr;
    airport_t       *arrival_airport_ptr;
    int              departure_airport_owned;
    int              arrival_airport_owned;
    char             departure_id[8];
    char             arrival_id[8];
    quantity_t       fuel;
    segment_t       *segments;
    int              segment_count;
    flight_profile_t profile;
} leg_t;

typedef struct cdrag_point
{
    double speed_m;
    double cdrag_0;
} cdrag_point_t;

typedef struct aircraft
{
    char   model_id[32];
    char   description[64];
    char   maker[32];
    char   type[32];
    int    number_motors;
    char   motor[32];
    char   motor_type[32];
    double cruise_altitude;
    double cruise_speed;
    double tsfc;
    double lapse_rate_factor;
    double thrust_0;
    double thrust_max_speed;
    double max_speed;
    double eweight;
    double mtow;
    double max_payload;
    double fuel_capacity;
    double vmo;
    double mmo;
    double wing_area;
    double wing_span;
    double aspect_ratio;
    double e;
    cdrag_point_t *cdrag;
    int            cdrag_count;
} aircraft_t;

typedef struct aircraft_list
{
    aircraft_t *aircrafts;
    int         count;
} aircraft_list_t;

typedef struct flight_plan
{
    int        id;
    char       type[32];
    char       route[16];
    char       aircraft_id[32];
    aircraft_t *aircraft_ptr;
    load_t           load;
    int        aircraft_owned;

    sim_datetime_t departure_time;   /* data+hora de partida    */
    sim_datetime_t arrival_time;     /* data+hora de chegada    */
    long long      departure_time_s; /* segundos absolutos      */
    long long      arrival_time_s;   /* segundos absolutos      */

    leg_t *legs;
    int    leg_count;
    int    is_valid;
} flight_plan_t;

typedef struct flight_plan_list
{
    flight_plan_t *plans;
    int            count;
} flight_plan_list_t;


typedef struct flight_state
{
    double latitude, longitude;
    double altitude_m;
    double velocity_ms;
    double bearing_rad;
    double mass_kg;
    double fuel_kg;
    double dist_to_next_m;
    int    phase;         /* 0=climb, 1=cruise, 2=descend */
    int    leg_index;
    int    segment_index;
    int    done;
    int    no_fuel;
} flight_state_t;

typedef struct flight_process
{
    pid_t         pid;
    int           slot_index;
    flight_plan_t plan;
} flight_process_t;

typedef struct program
{
    aircraft_list_t     aircraft_list;
    flight_plan_list_t *flight_plans_info;
    flight_process_t   *flight_processes;
    network_t           network;
} program_t;

typedef enum {
    VALID_OK     = 0,
    ERR_FUEL_LEG = 1,
    ERR_PAYLOAD  = 3,
    ERR_MTOW     = 4,
} validation_result_t;





extern volatile sig_atomic_t failure_count;
extern long long              global_sim_time_s;  /* segundos absolutos */

/* Forward declarations (avoid include cycles) */
typedef struct sim_ipc_ctx sim_ipc_ctx_t;
typedef struct simulation_report simulation_report_t;

typedef struct parent_sim_ctx {
    program_t *prog;
    sim_ipc_ctx_t *ipc;
    simulation_report_t *sim_report;
    int n_flights;
    int forked;
    int sim_step;
    int advance_result;
    int n_active;
    int collect_result;

    int *active;
    int *reported;
    int *flight_steps;
    int *departed;
    flight_update_t *latest_updates;
    int *updated_this_tick;

    pthread_mutex_t mux;
} parent_sim_ctx_t;



int                 flight_process_main(int slot_index, flight_plan_t plan);
int                 init_program(program_t *prog);
void                clean_finish(program_t *prog);
validation_result_t validate_flight_plan(flight_plan_t *plan);

int  sim_step_publish(parent_sim_ctx_t *ctx);
int  sim_step_collect(parent_sim_ctx_t *ctx);
void handle_report_output(parent_sim_ctx_t *ctx);

#endif /* FLIGHT_SIMULATOR_HEADER */