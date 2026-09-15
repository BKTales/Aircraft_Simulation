#include "simulator_includes.h"

#define REPORT_LINE "================================================================================"
#define REPORT_SUB  "--------------------------------------------------------------------------------"




static double knots_to_ms(double knots) {
    return knots * KNOTS_TO_MS;
}

static void copy_str(char *dest, size_t dest_size, const char *src) {
    if (dest_size == 0) {
        return;
    }
    snprintf(dest, dest_size, "%s", src != NULL ? src : "");
}

static int ensure_flight_capacity(simulation_report_t *report) {
    if (report->flight_count < report->flight_capacity) {
        return 0;
    }
    size_t new_cap = report->flight_capacity == 0 ? 8 : report->flight_capacity * 2;
    report_flight_record_t *grown =
            realloc(report->flights, new_cap * sizeof(report_flight_record_t));
    if (grown == NULL) {
        return -1;
    }
    report->flights = grown;
    report->flight_capacity = new_cap;
    return 0;
}

static int ensure_violation_capacity(simulation_report_t *report) {
    if (report->violation_count < report->violation_capacity) {
        return 0;
    }
    size_t new_cap = report->violation_capacity == 0 ? 4 : report->violation_capacity * 2;
    report_violation_record_t *grown =
            realloc(report->violations, new_cap * sizeof(report_violation_record_t));
    if (grown == NULL) {
        return -1;
    }
    report->violations = grown;
    report->violation_capacity = new_cap;
    return 0;
}

static int count_success(const simulation_report_t *report) {
    int n = 0;
    for (size_t i = 0; i < report->flight_count; i++) {
        if (strcmp(report->flights[i].execution_status, "SUCCESS") == 0) {
            n++;
        }
    }
    return n;
}

static int count_collision_flights(const simulation_report_t *report) {
    int n = 0;
    for (size_t i = 0; i < report->flight_count; i++) {
        if (strcmp(report->flights[i].execution_status, "COLLISION") == 0) {
            n++;
        }
    }
    return n;
}

static int is_collision_violation(const char *type) {
    return strcmp(type, "COLLISION") == 0 || strcmp(type, "CLOSE_PROXIMITY") == 0;
}

static size_t count_collision_events(const simulation_report_t *report) {
    size_t n = 0;
    for (size_t i = 0; i < report->violation_count; i++) {
        if (is_collision_violation(report->violations[i].violation_type)) {
            n++;
        }
    }
    return n;
}

static void format_collision_flight_ids(const simulation_report_t *report,
                                        char *buf, size_t buf_size) {
    if (buf_size == 0) {
        return;
    }
    buf[0] = '\0';
    int first = 1;
    for (size_t i = 0; i < report->flight_count; i++) {
        if (strcmp(report->flights[i].execution_status, "COLLISION") != 0) {
            continue;
        }
        char id_buf[16];
        snprintf(id_buf, sizeof(id_buf), "%s%d", first ? "" : ";", report->flights[i].flight_id);
        strncat(buf, id_buf, buf_size - strlen(buf) - 1);
        first = 0;
    }
}

static const char *validation_result(const simulation_report_t *report) {
    if (report->simulator_failure_count >= WARNING_THRESHOLD) {
        return "FAIL";
    }
    if (report->violation_count > 0) {
        return "FAIL";
    }
    if (report->flight_count == 0) {
        return "FAIL";
    }
    return "PASS";
}

static void write_validation_summary_txt(const simulation_report_t *report, FILE *out) {
    const int success = count_success(report);
    const int execution_failures = (int) report->flight_count - success;
    const int collision_flights = count_collision_flights(report);
    const size_t collision_events = count_collision_events(report);
    const char *result = validation_result(report);
    char collision_ids[128];

    format_collision_flight_ids(report, collision_ids, sizeof(collision_ids));

    fprintf(out, "%s\n", REPORT_LINE);
    fprintf(out, " VALIDATION SUMMARY\n");
    fprintf(out, "%s\n", REPORT_LINE);
    fprintf(out, " Flights reported             : %zu\n", report->flight_count);
    fprintf(out, " Flights completed (SUCCESS)  : %d\n", success);
    fprintf(out, " Execution failures           : %d\n", execution_failures);
    fprintf(out, " Collision events             : %zu\n", collision_events);
    fprintf(out, " Flights with COLLISION status: %d", collision_flights);
    if (collision_ids[0] != '\0') {
        fprintf(out, " (%s)", collision_ids);
    }
    fputc('\n', out);
    fprintf(out, " Safety violation events      : %zu\n", report->violation_count);
    fprintf(out, " Failure counter (simulator)  : %d  (threshold %d)\n",
            report->simulator_failure_count, WARNING_THRESHOLD);
    fprintf(out, "%s\n", REPORT_SUB);
    fprintf(out, " FINAL VALIDATION RESULT      : %s\n", result);
    if (strcmp(result, "FAIL") == 0) {
        if (collision_events > 0) {
            for (size_t i = 0; i < report->violation_count; i++) {
                const report_violation_record_t *v = &report->violations[i];
                if (!is_collision_violation(v->violation_type)) {
                    continue;
                }
                fprintf(out,
                        "  -> COLLISION: flights %d and %d (step %d, sep %.2f m).\n",
                        v->flight_a.flight_id, v->flight_b.flight_id, v->step,
                        v->separation_m);
            }
        }
        if (report->simulator_failure_count >= WARNING_THRESHOLD) {
            fprintf(out, "  -> Failure counter reached %d (>= %d).\n",
                    report->simulator_failure_count, WARNING_THRESHOLD);
        }
        if (report->violation_count > 0 && collision_events == 0) {
            fprintf(out, "  -> At least one safety violation was recorded.\n");
        }
        if (report->flight_count == 0) {
            fprintf(out, "  -> No flight execution data was recorded.\n");
        }
    }
    fprintf(out, "%s\n\n", REPORT_LINE);
}

static void write_collision_flights_csv(const simulation_report_t *report, FILE *csv) {
    int collision_flights = count_collision_flights(report);
    if (collision_flights <= 0) {
        return;
    }

    fprintf(csv, "collision_flights\n");
    fprintf(csv,
            "flight_id,departure,arrival,execution_status,step,elapsed_s,latitude_deg,longitude_deg,"
            "altitude_m,speed_kt,speed_ms,fuel_total_kg,fuel_used_kg,fuel_remaining_kg\n");
    for (size_t i = 0; i < report->flight_count; i++) {
        const report_flight_record_t *f = &report->flights[i];
        if (strcmp(f->execution_status, "COLLISION") != 0) {
            continue;
        }
        const int elapsed_s = f->step * report->tick_dt_s;
        fprintf(csv, "%d,%s,%s,%s,%d,%d,%.6f,%.6f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                f->flight_id, f->departure, f->arrival, f->execution_status, f->step, elapsed_s,
                f->latitude, f->longitude, f->altitude_m, f->speed_kt, knots_to_ms(f->speed_kt),
                f->fuel_total_kg, f->fuel_used_kg, f->fuel_remaining_kg);
    }
    fprintf(csv, "\n");
}

void simulation_report_init(simulation_report_t *report, int tick_dt_s) {
    memset(report, 0, sizeof(*report));
    report->tick_dt_s = tick_dt_s > 0 ? tick_dt_s : DT_S;
    if (pthread_mutex_init(&report->mutex, NULL) == 0)
        report->mutex_initialized = 1;
}

void simulation_report_free(simulation_report_t *report) {
    if (report->mutex_initialized)
        pthread_mutex_destroy(&report->mutex);
    free(report->flights);
    free(report->violations);
    memset(report, 0, sizeof(*report));
}

void simulation_report_record_flight(simulation_report_t  *report,
                                     const leg_t          *leg,
                                     const char           *execution_status,
                                     const aircraft_t     *aircraft,
                                     const flight_update_t *update,
                                     long long             departure_time_s,
                                     long long             arrival_time_s)
{
    if (!report || !leg || !aircraft || !update) return;

    if (report->mutex_initialized)
        pthread_mutex_lock(&report->mutex);
    if (ensure_flight_capacity(report) != 0) {
        if (report->mutex_initialized)
            pthread_mutex_unlock(&report->mutex);
        return;
    }

    report_flight_record_t *row = &report->flights[report->flight_count++];
    copy_str(row->departure, sizeof(row->departure), leg->departure_id);
    copy_str(row->arrival,   sizeof(row->arrival),   leg->arrival_id);
    copy_str(row->execution_status, sizeof(row->execution_status), execution_status);
    row->flight_id         = update->flight_id;
    row->step              = update->step;
    row->departure_time_s  = departure_time_s;
    row->arrival_time_s    = arrival_time_s;
    row->latitude          = update->latitude;
    row->longitude         = update->longitude;
    row->altitude_m        = update->altitude_m;
    row->speed_kt          = update->speed_kt;
    row->fuel_total_kg     = aircraft->fuel_capacity;
    row->fuel_remaining_kg = update->fuel_kg;
    row->fuel_used_kg      = aircraft->fuel_capacity - update->fuel_kg;
    if (report->mutex_initialized)
        pthread_mutex_unlock(&report->mutex);
}

void simulation_report_record_violation(simulation_report_t *report, int step,
                                        const char *violation_type,
                                        const flight_update_t *a, const flight_update_t *b,
                                        double separation_m, double alt_sep_m) {
    if (report == NULL || a == NULL || b == NULL) {
        return;
    }

    if (report->mutex_initialized)
        pthread_mutex_lock(&report->mutex);
    if (ensure_violation_capacity(report) != 0) {
        if (report->mutex_initialized)
            pthread_mutex_unlock(&report->mutex);
        return;
    }

    report_violation_record_t *row = &report->violations[report->violation_count++];
    copy_str(row->violation_type, sizeof(row->violation_type), violation_type);
    row->step = step;
    row->flight_a = *a;
    row->flight_b = *b;
    row->separation_m = separation_m;
    row->alt_sep_m = alt_sep_m;
    if (report->mutex_initialized)
        pthread_mutex_unlock(&report->mutex);
}

static void write_txt(const simulation_report_t *report, FILE *txt) {
    time_t now = time(NULL);
    struct tm *tm_info = localtime(&now);
    char ts[64];
    strftime(ts, sizeof(ts), "%Y-%m-%d %H:%M:%S", tm_info);

    fprintf(txt, "%s\n", REPORT_LINE);
    fprintf(txt, "                    AISafe FLIGHT SIMULATION REPORT\n");
    fprintf(txt, "%s\n", REPORT_LINE);
    fprintf(txt, " Generated at       : %s\n", ts);
    fprintf(txt, " Tick interval (s)  : %d\n", report->tick_dt_s);
    fprintf(txt, "%s\n\n", REPORT_LINE);

    fprintf(txt, "FLIGHT EXECUTION RECORDS\n");
    fprintf(txt, "%s\n\n", REPORT_SUB);

    for (size_t i = 0; i < report->flight_count; i++) {
        const report_flight_record_t *f = &report->flights[i];

        long long real_elapsed_s  = (long long)f->step * report->tick_dt_s;
        long long plan_duration_s = f->arrival_time_s - f->departure_time_s;
        long long real_end_s      = f->departure_time_s + real_elapsed_s;

        char dep_str[32], real_end_str[32], plan_end_str[32];
        char real_dur_str[32], plan_dur_str[32];

        fmt_datetime(f->departure_time_s, dep_str,      sizeof(dep_str));
        fmt_datetime(real_end_s,          real_end_str, sizeof(real_end_str));
        fmt_datetime(f->arrival_time_s,   plan_end_str, sizeof(plan_end_str));
        fmt_duration(real_elapsed_s,      real_dur_str, sizeof(real_dur_str));
        fmt_duration(plan_duration_s,     plan_dur_str, sizeof(plan_dur_str));

        const double speed_ms = knots_to_ms(f->speed_kt);

        fprintf(txt, "[Flight %03zu]  Flight ID %d\n", i, f->flight_id);
        fprintf(txt, "  Route              : %s -> %s\n", f->departure, f->arrival);
        fprintf(txt, "  Execution status   : %s\n", f->execution_status);
        fprintf(txt, "  Simulation step    : %d\n", f->step);
        fprintf(txt, "  Departure          : %s UTC\n", dep_str);
        fprintf(txt, "  Last update at     : %s UTC  (%s)\n", real_end_str, real_dur_str);
        fprintf(txt, "  Planned arrival    : %s UTC  (%s)\n", plan_end_str, plan_dur_str);
        fprintf(txt, "  Position           : lat %+.5f deg, lon %+.5f deg, alt %.2f m\n",
                f->latitude, f->longitude, f->altitude_m);
        fprintf(txt, "  Velocity (scalar)  : %.2f kt (%.2f m/s ground speed)\n", f->speed_kt, speed_ms);
        fprintf(txt, "  Fuel total         : %.2f kg\n", f->fuel_total_kg);
        fprintf(txt, "  Fuel used          : %.2f kg\n", f->fuel_used_kg);
        fprintf(txt, "  Fuel remaining     : %.2f kg\n", f->fuel_remaining_kg);
        fprintf(txt, "%s\n\n", REPORT_SUB);
    }

    if (report->violation_count > 0) {
        fprintf(txt, "%s\n", REPORT_LINE);
        fprintf(txt, " SAFETY VIOLATION EVENTS\n");
        fprintf(txt, "%s\n\n", REPORT_LINE);

        for (size_t i = 0; i < report->violation_count; i++) {
            const report_violation_record_t *v = &report->violations[i];
            const int elapsed_s = v->step * report->tick_dt_s;
            const flight_update_t *a = &v->flight_a;
            const flight_update_t *b = &v->flight_b;

            fprintf(txt, "[Event %03zu]  %s\n", i, v->violation_type);
            fprintf(txt, "  Simulation step    : %d  (elapsed %d s)\n", v->step, elapsed_s);
            fprintf(txt, "  Horizontal sep.    : %.2f m\n", v->separation_m);
            fprintf(txt, "  Vertical sep.      : %.2f m\n", v->alt_sep_m);
            fprintf(txt, "  Flight A (ID %d)\n", a->flight_id);
            fprintf(txt, "    Position         : lat %+.5f, lon %+.5f, alt %.2f m\n",
                    a->latitude, a->longitude, a->altitude_m);
            fprintf(txt, "    Velocity         : %.2f kt (%.2f m/s)\n",
                    a->speed_kt, knots_to_ms(a->speed_kt));
            fprintf(txt, "  Flight B (ID %d)\n", b->flight_id);
            fprintf(txt, "    Position         : lat %+.5f, lon %+.5f, alt %.2f m\n",
                    b->latitude, b->longitude, b->altitude_m);
            fprintf(txt, "    Velocity         : %.2f kt (%.2f m/s)\n",
                    b->speed_kt, knots_to_ms(b->speed_kt));
            fprintf(txt, "%s\n\n", REPORT_SUB);
        }
    }

    write_validation_summary_txt(report, txt);
}

static void write_csv(const simulation_report_t *report, FILE *csv) {
    const int success = count_success(report);
    const int execution_failures = (int)report->flight_count - success;
    const int collision_flights = count_collision_flights(report);
    const size_t collision_events = count_collision_events(report);
    const char *result = validation_result(report);
    char collision_ids[128];

    format_collision_flight_ids(report, collision_ids, sizeof(collision_ids));

    fprintf(csv, "metric,value\n");
    fprintf(csv, "flights_reported,%zu\n",           report->flight_count);
    fprintf(csv, "flights_success,%d\n",             success);
    fprintf(csv, "execution_failures,%d\n",          execution_failures);
    fprintf(csv, "collision_events,%zu\n",           collision_events);
    fprintf(csv, "collision_flights,%d\n",           collision_flights);
    if (collision_ids[0] != '\0')
        fprintf(csv, "collision_flight_ids,%s\n",    collision_ids);
    fprintf(csv, "safety_violation_events,%zu\n",    report->violation_count);
    fprintf(csv, "simulator_failure_count,%d\n",     report->simulator_failure_count);
    fprintf(csv, "simulator_failure_threshold,%d\n", WARNING_THRESHOLD);
    fprintf(csv, "validation_result,%s\n",           result);
    fprintf(csv, "\n");

    write_collision_flights_csv(report, csv);

    fprintf(csv,
        "flight_id,departure,arrival,execution_status,step,"
        "departure_utc,last_update_utc,real_duration,"
        "planned_arrival_utc,planned_duration,"
        "latitude_deg,longitude_deg,altitude_m,"
        "speed_kt,speed_ms,fuel_total_kg,fuel_used_kg,fuel_remaining_kg\n");

    for (size_t i = 0; i < report->flight_count; i++) {
        const report_flight_record_t *f = &report->flights[i];

        long long real_elapsed_s  = (long long)f->step * report->tick_dt_s;
        long long plan_duration_s = f->arrival_time_s - f->departure_time_s;
        long long real_end_s      = f->departure_time_s + real_elapsed_s;

        char dep_str[32], real_end_str[32], plan_end_str[32];
        char real_dur_str[32], plan_dur_str[32];

        fmt_datetime(f->departure_time_s, dep_str,      sizeof(dep_str));
        fmt_datetime(real_end_s,          real_end_str, sizeof(real_end_str));
        fmt_datetime(f->arrival_time_s,   plan_end_str, sizeof(plan_end_str));
        fmt_duration(real_elapsed_s,      real_dur_str, sizeof(real_dur_str));
        fmt_duration(plan_duration_s,     plan_dur_str, sizeof(plan_dur_str));

        fprintf(csv, "%d,%s,%s,%s,%d,%s,%s,%s,%s,%s,%.6f,%.6f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n",
                f->flight_id, f->departure, f->arrival, f->execution_status, f->step,
                dep_str, real_end_str, real_dur_str,
                plan_end_str, plan_dur_str,
                f->latitude, f->longitude, f->altitude_m,
                f->speed_kt, knots_to_ms(f->speed_kt),
                f->fuel_total_kg, f->fuel_used_kg, f->fuel_remaining_kg);
    }

    if (report->violation_count > 0) {
        fprintf(csv, "\n");
        fprintf(csv,
            "flight_id_a,flight_id_b,violation_type,step,elapsed_s,separation_m,alt_sep_m,"
            "lat_a_deg,lon_a_deg,alt_a_m,speed_kt_a,speed_ms_a,"
            "lat_b_deg,lon_b_deg,alt_b_m,speed_kt_b,speed_ms_b\n");

        for (size_t i = 0; i < report->violation_count; i++) {
            const report_violation_record_t *v = &report->violations[i];
            const int elapsed_s = v->step * report->tick_dt_s;
            const flight_update_t *a = &v->flight_a;
            const flight_update_t *b = &v->flight_b;

            fprintf(csv, "%d,%d,%s,%d,%d,%.2f,%.2f,%.6f,%.6f,%.2f,%.2f,%.2f,%.6f,%.6f,%.2f,%.2f,%.2f\n",
                    a->flight_id, b->flight_id, v->violation_type, v->step, elapsed_s,
                    v->separation_m, v->alt_sep_m,
                    a->latitude, a->longitude, a->altitude_m,
                    a->speed_kt, knots_to_ms(a->speed_kt),
                    b->latitude, b->longitude, b->altitude_m,
                    b->speed_kt, knots_to_ms(b->speed_kt));
        }
    }
}

int simulation_report_write_files(const simulation_report_t *report, const char *txt_path,
                                  const char *csv_path) {
    if (report == NULL || txt_path == NULL || csv_path == NULL) {
        return -1;
    }

    FILE *txt = fopen(txt_path, "w");
    if (txt == NULL) {
        return -1;
    }
    write_txt(report, txt);
    fclose(txt);

    FILE *csv = fopen(csv_path, "w");
    if (csv == NULL) {
        return -1;
    }
    write_csv(report, csv);
    fclose(csv);

    return 0;
}

void simulation_report_print_to_stream(const simulation_report_t *report, FILE *out) {
    if (report == NULL || out == NULL) {
        return;
    }
    write_txt(report, out);
}
