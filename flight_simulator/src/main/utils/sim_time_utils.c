#include "simulator_includes.h"

int hhmm_to_seconds(const char *hhmm)
{
    int h = 0, m = 0;
    sscanf(hhmm, "%d:%d", &h, &m);
    return h * 3600 + m * 60;
}

void seconds_to_hhmm(long long total_seconds, char *buf, int buf_size)
{
    time_t raw_time = (time_t)total_seconds;
    struct tm *time_info = gmtime(&raw_time);

    if (time_info) {
        snprintf(buf, buf_size, "%02d/%02d %02d:%02d",
                 time_info->tm_mday, time_info->tm_mon + 1,
                 time_info->tm_hour, time_info->tm_min);
    } else {
        snprintf(buf, buf_size, "??/?? ??:??");
    }
}

void fmt_datetime(long long epoch_s, char *buf, size_t n)
{
    time_t t = (time_t)epoch_s;
    struct tm *tm = gmtime(&t);
    if (tm)
        snprintf(buf, (int)n, "%02d/%02d %02d:%02d:%02d",
                 tm->tm_mday, tm->tm_mon + 1,
                 tm->tm_hour, tm->tm_min, tm->tm_sec);
    else
        snprintf(buf, (int)n, "??/?? ??:??:??");
}

/* Duração em  HHh MMm SSs  (pode passar 24h sem problema) */
void fmt_duration(long long total_s, char *buf, size_t n)
{
    if (total_s < 0) total_s = 0;
    long long h = total_s / 3600;
    long long m = (total_s % 3600) / 60;
    long long s = total_s % 60;
    snprintf(buf, (int)n, "%02lldh %02lldm %02llds", h, m, s);
}