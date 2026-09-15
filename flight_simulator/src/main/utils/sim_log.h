#ifndef SIM_LOG_H
#define SIM_LOG_H

void log_step_header(int step);
void sim_log_error(const char *msg);
void sim_log_skip_flight(int process_index);
int  sim_should_log_telemetry(int flight_step, long long global_sim_time_s, const flight_update_t *u);
void sim_log_telemetry_tick(int flight_step, long long global_sim_time_s, flight_update_t u);
void sim_log_warn_out_of_fuel(int flight_id);
void sim_log_warn_low_altitude(int flight_id);
void sim_log_critical_collision(int flight_id_a, int flight_id_b, double dist_m, double alt_diff);
void sim_log_safety_violation_recorded(int flight_id_a, int flight_id_b);
void sim_log_abort(int failure_count);
void sim_log_flight_success(int flight_id, int step);
void sim_log_reports_generated(const char *txt_path, const char *csv_path);
void sim_log_report_header(void);
void sim_log_report_footer(void);
void sim_log_non_interactive_keep(void);
void sim_log_files_deleted(void);
void sim_log_files_saved(void);

#endif
