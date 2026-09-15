#include "simulator_includes.h"

void get_atmosphere_strength(double altitude, double *temp, double *pressure, double *density) {
    if (altitude < 11000.0) {
        *temp = SEA_LEVEL_TEMP + TEMP_LAPSE_RATE * altitude;
        *pressure = SEA_LEVEL_PRESSURE
                  * pow((*temp / SEA_LEVEL_TEMP),
                        (GRAVITY / (-TEMP_LAPSE_RATE * GAS_CONSTANT)));
    } else {
        double h_tropo = 11000.0;
        double t_tropo = SEA_LEVEL_TEMP + TEMP_LAPSE_RATE * h_tropo;
        double p_tropo = SEA_LEVEL_PRESSURE
                       * pow((t_tropo / SEA_LEVEL_TEMP),
                             (GRAVITY / (-TEMP_LAPSE_RATE * GAS_CONSTANT)));

        *temp = t_tropo;
        *pressure = p_tropo
                  * exp(-GRAVITY * (altitude - h_tropo) / (GAS_CONSTANT * t_tropo));
    }
    *density = *pressure / (GAS_CONSTANT * *temp);
}

double isa_temperature(double alt_m) {
    double temp = 0.0, pressure = 0.0, density = 0.0;
    get_atmosphere_strength(alt_m, &temp, &pressure, &density);
    return temp;
}

double isa_density(double alt_m) {
    double temp = 0.0, pressure = 0.0, density = 0.0;
    get_atmosphere_strength(alt_m, &temp, &pressure, &density);
    return density;
}

double speed_of_sound(double alt_m) {
    return sqrt(GAMMA_AIR * GAS_CONSTANT * isa_temperature(alt_m));
}

double ms_to_mach(double spd_ms, double alt_m) {
    double a = speed_of_sound(alt_m);
    if (a < 1e-6) return 0.0;
    return spd_ms / a;
}

double mach_to_ms(double mach, double alt_m) {
    return mach * speed_of_sound(alt_m);
}
