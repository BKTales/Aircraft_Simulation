#include "simulator_includes.h"

static int umatch(const char *unit, const char *pat) {
    return strcasecmp(unit, pat) == 0;
}

double measure_to_si_altitude(const measure_t *m) {
    if (!m) return 0.0;
    const char *u = m->unit;
    if (umatch(u, "ft") || umatch(u, "feet")) return m->value * FT_TO_M_L;
    if (umatch(u, "FL"))                       return m->value * FL_TO_M_L;
    if (umatch(u, "km"))                       return m->value * 1000.0;
    return m->value;
}

double measure_to_si_speed(const measure_t *m) {
    if (!m) return 0.0;
    const char *u = m->unit;
    if (umatch(u, "kt") || umatch(u, "kts") || umatch(u, "knot") || umatch(u, "knots"))
        return m->value * KT_TO_MS_L;
    if (umatch(u, "M") || umatch(u, "mach"))
        return m->value;
    return m->value;
}

int measure_unit_is_mach(const measure_t *m) {
    if (!m) return 0;
    return umatch(m->unit, "M") || umatch(m->unit, "mach");
}

double measure_to_si_vrate(const measure_t *m) {
    if (!m) return 0.0;
    const char *u = m->unit;
    if (umatch(u, "ft/min") || umatch(u, "fpm")) return m->value * FPM_TO_MS_L;
    if (umatch(u, "m/min"))                       return m->value / 60.0;
    return m->value;
}

double quantity_to_si_alt(const quantity_t *q) {
    if (!q) return 0.0;
    const char *u = q->unit;
    if (umatch(u, "ft") || umatch(u, "feet")) return q->quantity * FT_TO_M_L;
    if (umatch(u, "FL"))                       return q->quantity * FL_TO_M_L;
    if (umatch(u, "km"))                       return q->quantity * 1000.0;
    return q->quantity;
}

double interpolate_cdrag(const aircraft_t *ac, double mach) {
    if (!ac || ac->cdrag_count < 1) return 0.025;
    if (mach <= ac->cdrag[0].speed_m) return ac->cdrag[0].cdrag_0;
    if (mach >= ac->cdrag[ac->cdrag_count - 1].speed_m) return ac->cdrag[ac->cdrag_count - 1].cdrag_0;

    for (int i = 0; i < ac->cdrag_count - 1; i++) {
        double m0 = ac->cdrag[i].speed_m;
        double m1 = ac->cdrag[i + 1].speed_m;
        if (mach >= m0 && mach <= m1) {
            double t = (mach - m0) / (m1 - m0);
            return ac->cdrag[i].cdrag_0 + t * (ac->cdrag[i + 1].cdrag_0 - ac->cdrag[i].cdrag_0);
        }
    }
    return ac->cdrag[ac->cdrag_count - 1].cdrag_0;
}

double calculate_cl(double mass_kg, double rho, double spd_ms, double wing_area) {
    double denom = rho * spd_ms * spd_ms * wing_area;
    if (denom < 1e-9) return 0.0;
    return (2.0 * mass_kg * GRAVITY) / denom;
}

double calculate_cd(double cd0, double cl, double ar, double e) {
    if (ar < 1e-9 || e < 1e-9) return cd0;
    return cd0 + (cl * cl) / (M_PI * ar * e);
}

double calculate_lift_force(double cl, double rho, double spd_ms, double wing_area) {
    return 0.5 * cl * rho * spd_ms * spd_ms * wing_area;
}

double calculate_drag_force(double cd, double rho, double spd_ms, double wing_area) {
    return 0.5 * cd * rho * spd_ms * spd_ms * wing_area;
}

double thrust_sea_level(const aircraft_t *ac, double spd_ms, double alt_m) {
    if (!ac) return 0.0;
    if (spd_ms <= 0.0)             return ac->thrust_0;

    double a = speed_of_sound(alt_m);
    double mach = (a > 1e-6) ? (spd_ms / a) : 0.0;

    double max_speed_mach = ac->max_speed;
    if (max_speed_mach <= 0.0) return ac->thrust_max_speed;

    if (mach >= max_speed_mach)   return ac->thrust_max_speed;
    double t = mach / max_speed_mach;
    return ac->thrust_0 + t * (ac->thrust_max_speed - ac->thrust_0);
}

double thrust_at_altitude(const aircraft_t *ac, double spd_ms, double alt_m) {
    double t_sl    = thrust_sea_level(ac, spd_ms, alt_m);
    double rho     = isa_density(alt_m);
    return t_sl * pow(rho / SEA_LEVEL_DENSITY, THRUST_DENSITY_EXPONENT);
}

double fuel_rate_kgs(double thrust_n, double tsfc_kg_ns) {
    return thrust_n * tsfc_kg_ns;
}

double get_mach_from_ias(double ias_knots, double density) {
    if (ias_knots <= 0.0 || density <= 0.0) return 0.0;

    double pressure_term = pow(1.0 + 0.2 * pow(ias_knots / 661.5, 2), 3.5) - 1.0;
    double compressibility = pow((SEA_LEVEL_DENSITY / density) * pressure_term + 1.0, 0.286) - 1.0;
    if (compressibility <= 0.0) return 0.0;

    return sqrt(5.0 * compressibility);
}
