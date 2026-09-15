#ifndef WEATHER_CONFIG_H
#define WEATHER_CONFIG_H

typedef struct sim_environment sim_environment_t;

typedef struct weather_bounds {
    double min_lat;
    double max_lat;
    double min_lon;
    double max_lon;
} weather_bounds_t;

typedef struct weather_record {
    long long        start_time_s;
    long long        end_time_s;
    int              wind_direction_deg;
    double           wind_speed_ms;
    weather_bounds_t bounds;
    int              has_bounds;
} weather_record_t;

typedef struct weather_config {
    char              area_code[32];
    weather_record_t *records;
    int               record_count;
} weather_config_t;

int  weather_config_load(weather_config_t *cfg, const char *path);
void weather_config_free(weather_config_t *cfg);

int weather_config_lookup(const weather_config_t *cfg,
                          long long sim_time_s,
                          double latitude,
                          double longitude,
                          sim_environment_t *out);

#endif /* WEATHER_CONFIG_H */
