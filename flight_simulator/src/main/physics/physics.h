#ifndef PHYSICS_H
#define PHYSICS_H

double measure_to_si_altitude(const measure_t *m);
double measure_to_si_speed(const measure_t *m);
int measure_unit_is_mach(const measure_t *m);
double measure_to_si_vrate(const measure_t *m);
double quantity_to_si_alt(const quantity_t *q);

double interpolate_cdrag(const aircraft_t *ac, double mach);
double calculate_cl(double mass_kg, double rho, double spd_ms, double wing_area);
double calculate_cd(double cd0, double cl, double ar, double e);
double calculate_lift_force(double cl, double rho, double spd_ms, double wing_area);
double calculate_drag_force(double cd, double rho, double spd_ms, double wing_area);
double thrust_sea_level(const aircraft_t *ac, double spd_ms, double alt_m);
double thrust_at_altitude(const aircraft_t *ac, double spd_ms, double alt_m);
double fuel_rate_kgs(double thrust_n, double tsfc_kg_ns);
double get_mach_from_ias(double ias_knots, double density);


#endif /* PHYSICS_H */
