#ifndef SIM_TIME_UTILS_H
#define SIM_TIME_UTILS_H

int  hhmm_to_seconds(const char *hhmm);
void seconds_to_hhmm(long long total_seconds, char *buf, int buf_size);
void fmt_datetime(long long epoch_s, char *buf, size_t n);
void fmt_duration(long long total_s, char *buf, size_t n);

#endif /* SIM_TIME_UTILS_H */
