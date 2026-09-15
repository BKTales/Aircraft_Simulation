#include "../simulator_includes.h"
#include "../utils/weather_config.h"

#include <math.h>
#include <stdio.h>
#include <string.h>

static int failures = 0;
static int checks = 0;

static void expect_true(int condition, const char *message)
{
    checks++;
    if (!condition) {
        fprintf(stderr, "  FAIL: %s\n", message);
        failures++;
        return;
    }
    printf("  OK:   %s\n", message);
}

static void expect_double(double actual, double expected, const char *message)
{
    checks++;
    if (fabs(actual - expected) > 1e-6) {
        fprintf(stderr, "  FAIL: %s (got %f expected %f)\n", message, actual, expected);
        failures++;
        return;
    }
    printf("  OK:   %s (%.1f)\n", message, actual);
}

static void section(const char *title)
{
    printf("\n[%s]\n", title);
}

int main(void)
{
    weather_config_t cfg;
    sim_environment_t env;

    section("empty / null config");
    expect_true(weather_config_load(&cfg, NULL) == 0, "null path loads empty config");
    expect_true(cfg.record_count == 0, "empty config has no records");
    weather_config_free(&cfg);

    section("global wind fixture (weather_constant_west.json)");
    const char *fixture = "../data/weather/weather_constant_west.json";
    expect_true(weather_config_load(&cfg, fixture) == 0, "fixture loads");
    expect_true(cfg.record_count == 1, "fixture has one record");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, NAN, NAN, &env) == 1,
                "time-only lookup matches (no position filter)");
    expect_true(env.valid == 1, "lookup marked valid");
    expect_true(env.wind_direction_deg == 270, "wind direction 270° (from west)");
    expect_double(env.wind_speed_ms, 10.0, "wind speed 10 m/s");
    weather_config_free(&cfg);

    section("Lisboa box bounds (weather_lisboa_box.json)");
    const char *lisboa_fixture = "../data/weather/weather_lisboa_box.json";
    expect_true(weather_config_load(&cfg, lisboa_fixture) == 0, "lisboa box loads");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, 38.78, -9.13, &env) == 1,
                "Lisboa (38.78°N, -9.13°E) inside bounds → wind returned");
    expect_true(env.valid == 1, "inside lookup valid");
    expect_true(env.wind_direction_deg == 270, "inside wind direction 270°");
    expect_double(env.wind_speed_ms, 15.0, "inside wind speed 15 m/s");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, 41.0, 2.0, &env) == 0,
                "Madrid (41.0°N, 2.0°E) outside bounds → no wind");
    expect_true(env.valid == 0, "outside lookup invalid (zero wind)");
    weather_config_free(&cfg);

    section("two wind boxes (weather_two_boxes.json)");
    const char *two_boxes_fixture = "../data/weather/weather_two_boxes.json";
    expect_true(weather_config_load(&cfg, two_boxes_fixture) == 0, "two-box fixture loads");
    expect_true(cfg.record_count == 2, "fixture has two records");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, 38.5, -9.0, &env) == 1,
                "box 1 (Lisboa) → west wind 270° / 15 m/s");
    expect_true(env.wind_direction_deg == 270, "box 1 direction");
    expect_double(env.wind_speed_ms, 15.0, "box 1 speed");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, 39.5, -5.0, &env) == 0,
                "gap between boxes → no wind");
    expect_true(env.valid == 0, "gap lookup invalid");

    memset(&env, 0, sizeof(env));
    expect_true(weather_config_lookup(&cfg, 1000, 40.0, -2.5, &env) == 1,
                "box 2 (east Spain) → north wind 0° / 20 m/s");
    expect_true(env.wind_direction_deg == 0, "box 2 direction");
    expect_double(env.wind_speed_ms, 20.0, "box 2 speed");
    weather_config_free(&cfg);

    section("error handling");
    expect_true(weather_config_load(&cfg, "/nonexistent/weather.json") == -1,
                "missing file returns error");

    memset(&env, 0, sizeof(env));
    weather_config_lookup(&cfg, 1000, NAN, NAN, &env);
    expect_true(env.valid == 0, "failed load yields invalid lookup");
    weather_config_free(&cfg);

    printf("\n");
    if (failures == 0) {
        printf("weather_config_test: PASS (%d checks)\n", checks);
        return 0;
    }

    fprintf(stderr, "weather_config_test: FAIL (%d/%d checks failed)\n", failures, checks);
    return 1;
}
