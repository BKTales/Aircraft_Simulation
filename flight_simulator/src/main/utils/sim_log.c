#include "simulator_includes.h"

void sim_log_error(const char *msg)
{
    fprintf(stderr, C_RED C_BOLD "[ERROR] %s\n" C_RESET, msg);
}

void sim_log_skip_flight(int process_index)
{
    dprintf(STDERR_FILENO, C_YELLOW "[SIM] Skipping flight process %d (no valid aircraft)\n" C_RESET,
            process_index);
}

void log_step_header(int step)
{
    dprintf(STDERR_FILENO, "\nstep %d\n", step);
}

int sim_should_log_telemetry(int flight_step, long long global_sim_time_s, const flight_update_t *u)
{
    if (!u)
        return 0;
    if (flight_step == 0)
        return 1;
    if (u->done)
        return 1;
    if (TELEMETRY_LOG_INTERVAL_S > 0
            && global_sim_time_s > 0
            && (global_sim_time_s % TELEMETRY_LOG_INTERVAL_S) == 0)
        return 1;
    return 0;
}

void sim_log_telemetry_tick(int flight_step, long long global_sim_time_s, flight_update_t u) {
    char time_buf[32];
    fmt_datetime(global_sim_time_s, time_buf, sizeof(time_buf));

    dprintf(STDERR_FILENO,
        C_CYAN "[TELEMETRY] [%s] Flight ID: %-3d | Step: %-5d | Lat: %9.5f | Lon: %9.5f | Alt: %7.2fm | Speed: %6.2fkt | Fuel: %7.2fkg | Phase: %d | Dist: %7.2fkm\n" C_RESET,
        time_buf, u.flight_id, flight_step, u.latitude, u.longitude,
        u.altitude_m, u.speed_kt, u.fuel_kg, u.phase, u.remaining_distance_m / 1000.0);
}


void sim_log_warn_out_of_fuel(int flight_id)
{
    dprintf(STDERR_FILENO, C_YELLOW "[WARN] Flight %d ran out of fuel! Terminating.\n" C_RESET, flight_id);
}

void sim_log_warn_low_altitude(int flight_id)
{
    dprintf(STDERR_FILENO, C_YELLOW "[WARN] Flight %d is way too low! PULL UP! Terminating.\n" C_RESET,
            flight_id);
}

void sim_log_critical_collision(int flight_id_a, int flight_id_b, double dist_m, double alt_diff)
{
    dprintf(STDERR_FILENO,
            C_RED C_BOLD "[CRITICAL] Flights too close! Flight %d and %d (Dist: %.2fm, Alt Diff: %.2fm). Terminating both.\n" C_RESET,
            flight_id_a, flight_id_b, dist_m, alt_diff);
}

void sim_log_safety_violation_recorded(int flight_id_a, int flight_id_b)
{
    dprintf(STDERR_FILENO,
            C_YELLOW "[REPORT] Safety violation recorded for flights %d and %d.\n" C_RESET,
            flight_id_a, flight_id_b);
}

void sim_log_abort(int failure_count)
{
    dprintf(STDERR_FILENO,
            C_RED C_BOLD "\n[ABORT] Failure threshold reached (%d failures). Terminating simulation!\n" C_RESET,
            failure_count);
}

void sim_log_flight_success(int flight_id, int step)
{
    dprintf(STDERR_FILENO, C_GREEN "[DONE] Flight %d finished at step %d — SUCCESS\n" C_RESET,
            flight_id, step);
}

void sim_log_reports_generated(const char *txt_path, const char *csv_path)
{
    dprintf(STDOUT_FILENO, "\nReports generated:\n");
    dprintf(STDOUT_FILENO, "  - " C_BOLD "%s\n" C_RESET, txt_path);
    dprintf(STDOUT_FILENO, "  - " C_BOLD "%s\n" C_RESET, csv_path);
}

void sim_log_report_header(void)
{
    dprintf(STDOUT_FILENO, "\n" C_CYAN C_BOLD "============== SIMULATION REPORT ==============\n" C_RESET);
}

void sim_log_report_footer(void)
{
    dprintf(STDOUT_FILENO, C_CYAN C_BOLD "=================================================\n" C_RESET);
}

void sim_log_non_interactive_keep(void)
{
    dprintf(STDOUT_FILENO, "[NON-INTERACTIVE] keeping report files.\n");
}

void sim_log_files_deleted(void)
{
    dprintf(STDOUT_FILENO, C_YELLOW "Files deleted.\n" C_RESET);
}

void sim_log_files_saved(void)
{
    dprintf(STDOUT_FILENO, C_GREEN "Files saved.\n" C_RESET);
}
