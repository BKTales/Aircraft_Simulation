#ifndef FLIGHT_REPORT_H
#define FLIGHT_REPORT_H

#include <pthread.h>

#define REPORT_STATUS_LEN 32
#define REPORT_VIOLATION_TYPE_LEN 32

typedef struct {
    char departure[8];
    char arrival[8];
    char execution_status[REPORT_STATUS_LEN];
    int  flight_id;
    int  step;
    long long departure_time_s;   
    long long arrival_time_s;     
    double latitude;
    double longitude;
    double altitude_m;
    double speed_kt;
    double fuel_total_kg;
    double fuel_used_kg;
    double fuel_remaining_kg;
} report_flight_record_t;

typedef struct {
    char violation_type[REPORT_VIOLATION_TYPE_LEN];
    int step;
    flight_update_t flight_a;
    flight_update_t flight_b;
    double separation_m;
    double alt_sep_m;
} report_violation_record_t;

typedef struct simulation_report {
    int tick_dt_s;
    int simulator_failure_count;
    report_flight_record_t *flights;
    size_t flight_count;
    size_t flight_capacity;
    report_violation_record_t *violations;
    size_t violation_count;
    size_t violation_capacity;
    pthread_mutex_t mutex;
    int mutex_initialized;
} simulation_report_t;

void simulation_report_init(simulation_report_t *report, int tick_dt_s);

void simulation_report_free(simulation_report_t *report);

void simulation_report_record_flight(simulation_report_t  *report,
                                     const leg_t          *leg,
                                     const char           *execution_status,
                                     const aircraft_t     *aircraft,
                                     const flight_update_t *update,
                                     long long             departure_time_s,  
                                     long long             arrival_time_s);   

void simulation_report_record_violation(simulation_report_t *report, int step,
                                        const char *violation_type,
                                        const flight_update_t *a, const flight_update_t *b,
                                        double separation_m, double alt_sep_m);

int simulation_report_write_files(const simulation_report_t *report, const char *txt_path,
                                  const char *csv_path);

void simulation_report_print_to_stream(const simulation_report_t *report, FILE *out);

#endif
