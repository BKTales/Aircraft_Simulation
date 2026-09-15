#ifndef ATMOSPHERE_H
#define ATMOSPHERE_H

void get_atmosphere_strength(double altitude, double *temp, double *pressure, double *density);
double isa_temperature(double alt_m);
double isa_density(double alt_m);
double speed_of_sound(double alt_m);
double ms_to_mach(double spd_ms, double alt_m);
double mach_to_ms(double mach, double alt_m);

#endif /* ATMOSPHERE_H */