#include "../simulator_includes.h"
#include "weather_config.h"

#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "../lib/cjson/cJSON.h"

static int parse_bounds(cJSON *obj, weather_bounds_t *bounds)
{
    if (!obj || !bounds)
        return 0;

    cJSON *min_lat = cJSON_GetObjectItem(obj, "minLat");
    cJSON *max_lat = cJSON_GetObjectItem(obj, "maxLat");
    cJSON *min_lon = cJSON_GetObjectItem(obj, "minLon");
    cJSON *max_lon = cJSON_GetObjectItem(obj, "maxLon");

    if (!min_lat || !max_lat || !min_lon || !max_lon)
        return 0;

    bounds->min_lat = min_lat->valuedouble;
    bounds->max_lat = max_lat->valuedouble;
    bounds->min_lon = min_lon->valuedouble;
    bounds->max_lon = max_lon->valuedouble;
    return 1;
}

static int point_in_bounds(double lat, double lon, const weather_bounds_t *b)
{
    return lat >= b->min_lat && lat <= b->max_lat
        && lon >= b->min_lon && lon <= b->max_lon;
}

static int record_matches(const weather_record_t *rec,
                          long long sim_time_s,
                          double latitude,
                          double longitude)
{
    if (sim_time_s < rec->start_time_s || sim_time_s > rec->end_time_s)
        return 0;
    if (!rec->has_bounds || isnan(latitude) || isnan(longitude))
        return 1;
    return point_in_bounds(latitude, longitude, &rec->bounds);
}

int weather_config_load(weather_config_t *cfg, const char *path)
{
    if (!cfg)
        return -1;

    memset(cfg, 0, sizeof(*cfg));

    if (!path || path[0] == '\0')
        return 0;

    FILE *fp = fopen(path, "rb");
    if (!fp)
        return -1;

    if (fseek(fp, 0, SEEK_END) != 0) {
        fclose(fp);
        return -1;
    }

    long size = ftell(fp);
    if (size < 0) {
        fclose(fp);
        return -1;
    }

    rewind(fp);

    char *buf = malloc((size_t)size + 1);
    if (!buf) {
        fclose(fp);
        return -1;
    }

    size_t read = fread(buf, 1, (size_t)size, fp);
    fclose(fp);
    buf[read] = '\0';

    cJSON *root = cJSON_Parse(buf);
    free(buf);
    if (!root)
        return -1;

    cJSON *area = cJSON_GetObjectItem(root, "areaCode");
    if (area && cJSON_IsString(area))
        strncpy(cfg->area_code, area->valuestring, sizeof(cfg->area_code) - 1);

    cJSON *records = cJSON_GetObjectItem(root, "records");
    if (!records || !cJSON_IsArray(records)) {
        cJSON_Delete(root);
        return 0;
    }

    int count = cJSON_GetArraySize(records);
    if (count <= 0) {
        cJSON_Delete(root);
        return 0;
    }

    cfg->records = calloc((size_t)count, sizeof(weather_record_t));
    if (!cfg->records) {
        cJSON_Delete(root);
        return -1;
    }

    cfg->record_count = count;

    for (int i = 0; i < count; i++) {
        cJSON *item = cJSON_GetArrayItem(records, i);
        weather_record_t *rec = &cfg->records[i];

        cJSON *start = cJSON_GetObjectItem(item, "startTimeS");
        cJSON *end   = cJSON_GetObjectItem(item, "endTimeS");
        cJSON *dir   = cJSON_GetObjectItem(item, "windDirectionDeg");
        cJSON *spd   = cJSON_GetObjectItem(item, "windSpeedMs");
        cJSON *bounds = cJSON_GetObjectItem(item, "bounds");

        if (start) rec->start_time_s = (long long)start->valuedouble;
        if (end)   rec->end_time_s   = (long long)end->valuedouble;
        if (dir)   rec->wind_direction_deg = dir->valueint;
        if (spd)   rec->wind_speed_ms = spd->valuedouble;
        rec->has_bounds = parse_bounds(bounds, &rec->bounds);
    }

    cJSON_Delete(root);
    return 0;
}

void weather_config_free(weather_config_t *cfg)
{
    if (!cfg)
        return;
    free(cfg->records);
    cfg->records = NULL;
    cfg->record_count = 0;
    cfg->area_code[0] = '\0';
}

int weather_config_lookup(const weather_config_t *cfg,
                          long long sim_time_s,
                          double latitude,
                          double longitude,
                          sim_environment_t *out)
{
    if (!out)
        return -1;

    memset(out, 0, sizeof(*out));

    if (!cfg || cfg->record_count <= 0)
        return 0;

    for (int i = 0; i < cfg->record_count; i++) {
        const weather_record_t *rec = &cfg->records[i];
        if (!record_matches(rec, sim_time_s, latitude, longitude))
            continue;

        out->valid = 1;
        out->wind_direction_deg = rec->wind_direction_deg;
        out->wind_speed_ms = rec->wind_speed_ms;
        out->source = 1; /* ENV_SOURCE_WEATHER_FILE */
        return 1;
    }

    return 0;
}
