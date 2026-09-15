#ifndef MATH_UTILS_H
#define MATH_UTILS_H

double convert_m_km(double value);
double interpolation(double x, double x0, double x1, double y0, double y1);
double haversine_distance(double lat1, double lon1, double lat2, double lon2);
double bearing_to(double lat1, double lon1, double lat2, double lon2);
void advance_position(double *lat, double *lon, double bearing_rad, double dist_m);

#endif /* MATH_UTILS_H */