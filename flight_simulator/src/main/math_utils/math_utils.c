#include "simulator_includes.h"

double convert_m_km(double value) {
    return value / 1000.0;
}

double interpolation(double x, double x0, double x1, double y0, double y1) {
    if (fabs(x1 - x0) < 1e-9) return y0;
    return y0 + (x - x0) / (x1 - x0) * (y1 - y0);
}

static double deg2rad(double d) { return d * M_PI / 180.0; }
static double rad2deg(double r) { return r * 180.0 / M_PI; }

double haversine_distance(double lat1, double lon1, double lat2, double lon2) {
    double φ1 = deg2rad(lat1), φ2 = deg2rad(lat2);
    double dφ = deg2rad(lat2 - lat1);
    double dλ = deg2rad(lon2 - lon1);
    double a  = sin(dφ/2)*sin(dφ/2) + cos(φ1)*cos(φ2)*sin(dλ/2)*sin(dλ/2);
    double c  = 2.0 * atan2(sqrt(a), sqrt(1.0 - a));
    return EARTH_RADIUS_M * c;
}

double bearing_to(double lat1, double lon1, double lat2, double lon2) {
    double φ1 = deg2rad(lat1), φ2 = deg2rad(lat2);
    double dλ = deg2rad(lon2 - lon1);
    double y  = sin(dλ) * cos(φ2);
    double x  = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(dλ);
    double b  = atan2(y, x);
    if (b < 0.0) b += 2.0 * M_PI;
    return b;
}

void advance_position(double *lat, double *lon, double bearing_rad, double dist_m) {
    double φ  = deg2rad(*lat);
    double λ  = deg2rad(*lon);
    double δ  = dist_m / EARTH_RADIUS_M;
    double φ2 = asin(sin(φ)*cos(δ) + cos(φ)*sin(δ)*cos(bearing_rad));
    double λ2 = λ + atan2(sin(bearing_rad)*sin(δ)*cos(φ), cos(δ) - sin(φ)*sin(φ2));
    *lat = rad2deg(φ2);
    *lon = rad2deg(λ2);
}