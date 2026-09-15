#include "simulator_includes.h"

measure_t parse_measure(cJSON *obj, const char *value_key) {
    measure_t m = {0};
    cJSON *val  = cJSON_GetObjectItem(obj, value_key);
    cJSON *unit = cJSON_GetObjectItem(obj, "Unit");
    if (val)  m.value = val->valuedouble;
    if (unit) strncpy(m.unit, unit->valuestring, sizeof(m.unit) - 1);
    return m;
}

quantity_t parse_quantity(cJSON *obj) {
    quantity_t q = {0};
    cJSON *qty  = cJSON_GetObjectItem(obj, "Quantity");
    cJSON *unit = cJSON_GetObjectItem(obj, "Unit");
    if (qty)  q.quantity = qty->valuedouble;
    if (unit) strncpy(q.unit, unit->valuestring, sizeof(q.unit) - 1);
    return q;
}

position_t parse_position(cJSON *obj) {
    position_t p = {0};
    cJSON *lat = cJSON_GetObjectItem(obj, "Latitude");
    cJSON *lon = cJSON_GetObjectItem(obj, "Longitude");
    cJSON *alt = cJSON_GetObjectItem(obj, "Altitude");
    if (lat) p.latitude  = lat->valuedouble;
    if (lon) p.longitude = lon->valuedouble;
    if (alt) p.altitude  = parse_quantity(alt);
    return p;
}

flight_profile_point_t *parse_profile_array(cJSON *arr, int *out_count) {
    if (!arr || !cJSON_IsArray(arr)) {
        *out_count = 0;
        return NULL;
    }

    int count = cJSON_GetArraySize(arr);
    flight_profile_point_t *points = malloc(count * sizeof(flight_profile_point_t));
    if (!points) { *out_count = 0; return NULL; }

    for (int i = 0; i < count; i++) {
        cJSON *entry = cJSON_GetArrayItem(arr, i);
        cJSON *alt   = cJSON_GetObjectItem(entry, "Altitude");
        cJSON *ias   = cJSON_GetObjectItem(entry, "IAS");
        points[i].altitude_m = alt ? alt->valuedouble : 0.0;
        points[i].ias_kt     = ias ? ias->valuedouble : 0.0;
    }

    *out_count = count;
    return points;
}

sim_datetime_t parse_datetime(const char *str) {
    sim_datetime_t dt = {0};
    if (sscanf(str, "%d-%d-%d %d:%d",
               &dt.year, &dt.month, &dt.day,
               &dt.hour, &dt.min) == 5)
        return dt;
    sscanf(str, "%d:%d", &dt.hour, &dt.min);
    return dt;
}

void advance_day(sim_datetime_t *dt) {
    static const int days_in_month[] =
        {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    int is_leap = (dt->year % 4 == 0 && dt->year % 100 != 0)
               || (dt->year % 400 == 0);
    int max_day = (dt->month == 2 && is_leap) ? 29 : days_in_month[dt->month];
    dt->day++;
    if (dt->day > max_day) {
        dt->day = 1;
        dt->month++;
        if (dt->month > 12) { dt->month = 1; dt->year++; }
    }
}

int datetime_to_seconds(sim_datetime_t dt) {
    return dt.hour * 3600 + dt.min * 60 + dt.sec;
}

int datetime_cmp(sim_datetime_t a, sim_datetime_t b) {
    if (a.year  != b.year)  return a.year  < b.year  ? -1 : 1;
    if (a.month != b.month) return a.month < b.month ? -1 : 1;
    if (a.day   != b.day)   return a.day   < b.day   ? -1 : 1;
    if (a.hour  != b.hour)  return a.hour  < b.hour  ? -1 : 1;
    if (a.min   != b.min)   return a.min   < b.min   ? -1 : 1;
    return a.sec != b.sec   ? (a.sec < b.sec ? -1 : 1) : 0;
}

void datetime_to_str(sim_datetime_t dt, char *buf, int buf_size) {
    snprintf(buf, buf_size, "%04d-%02d-%02d %02d:%02d:%02d",
             dt.year, dt.month, dt.day, dt.hour, dt.min, dt.sec);
}

static void safe_strncpy(char *dst, const char *src, size_t size) {
    if (!dst || size == 0) return;
    if (!src) {
        dst[0] = '\0';
        return;
    }
    strncpy(dst, src, size - 1);
    dst[size - 1] = '\0';
}

static double json_double(cJSON *obj, const char *key, double fallback) {
    cJSON *item = cJSON_GetObjectItem(obj, key);
    if (!item) return fallback;
    if (cJSON_IsNumber(item)) return item->valuedouble;
    if (cJSON_IsString(item) && item->valuestring) return atof(item->valuestring);
    return fallback;
}

static const char *json_string_field(cJSON *obj, const char *key) {
    cJSON *item = cJSON_GetObjectItem(obj, key);
    if (item && cJSON_IsString(item) && item->valuestring) return item->valuestring;
    return "";
}

static airport_t *parse_airport_embedded(cJSON *obj) {
    if (!obj || !cJSON_IsObject(obj)) return NULL;

    airport_t *ap = calloc(1, sizeof(airport_t));
    if (!ap) return NULL;

    cJSON *id = cJSON_GetObjectItem(obj, "Id");
    if (id && cJSON_IsString(id))
        safe_strncpy(ap->id, id->valuestring, sizeof(ap->id));

    safe_strncpy(ap->name, json_string_field(obj, "Name"), sizeof(ap->name));
    safe_strncpy(ap->town, json_string_field(obj, "Town"), sizeof(ap->town));
    safe_strncpy(ap->country, json_string_field(obj, "Country"), sizeof(ap->country));

    ap->location.latitude  = json_double(obj, "Latitude", 0.0);
    ap->location.longitude = json_double(obj, "Longitude", 0.0);
    ap->location.altitude.quantity = json_double(obj, "Altitude", 0.0);
    safe_strncpy(ap->location.altitude.unit, "m", sizeof(ap->location.altitude.unit));

    return ap;
}

static aircraft_t *parse_aircraft_embedded(cJSON *obj) {
    if (!obj || !cJSON_IsObject(obj)) return NULL;

    aircraft_t *ac = calloc(1, sizeof(aircraft_t));
    if (!ac) return NULL;

    safe_strncpy(ac->model_id, json_string_field(obj, "ModelId"), sizeof(ac->model_id));
    safe_strncpy(ac->description, json_string_field(obj, "Description"), sizeof(ac->description));
    safe_strncpy(ac->maker, json_string_field(obj, "Maker"), sizeof(ac->maker));
    safe_strncpy(ac->type, json_string_field(obj, "Type"), sizeof(ac->type));
    safe_strncpy(ac->motor, json_string_field(obj, "Motor"), sizeof(ac->motor));
    safe_strncpy(ac->motor_type, json_string_field(obj, "MotorType"), sizeof(ac->motor_type));

    ac->number_motors       = (int)json_double(obj, "NumberMotors", 0.0);
    ac->cruise_altitude     = json_double(obj, "CruiseAltitude", 0.0);
    ac->cruise_speed        = json_double(obj, "CruiseSpeed", 0.0);
    ac->tsfc                = json_double(obj, "TSFC", 0.0);
    ac->lapse_rate_factor   = json_double(obj, "LapseRateFactor", 0.0);
    ac->thrust_0            = json_double(obj, "Thrust0", 0.0);
    ac->thrust_max_speed    = json_double(obj, "ThrustMaxSpeed", 0.0);
    ac->max_speed           = json_double(obj, "MaxSpeed", 0.0);
    ac->eweight             = json_double(obj, "EWeight", 0.0);
    ac->mtow                = json_double(obj, "MTOW", 0.0);
    ac->max_payload         = json_double(obj, "MaxPayload", 0.0);
    ac->fuel_capacity       = json_double(obj, "FuelCapacity", 0.0);
    ac->vmo                 = json_double(obj, "VMO", 0.0);
    ac->mmo                 = json_double(obj, "MMO", 0.0);
    ac->wing_area           = json_double(obj, "WingArea", 0.0);
    ac->wing_span           = json_double(obj, "WingSpan", 0.0);
    ac->aspect_ratio        = json_double(obj, "AspectRatio", 0.0);
    ac->e                   = json_double(obj, "E", 0.0);

    cJSON *cdrag_arr = cJSON_GetObjectItem(obj, "CdragFunction");
    if (cdrag_arr && cJSON_IsArray(cdrag_arr)) {
        ac->cdrag_count = cJSON_GetArraySize(cdrag_arr);
        if (ac->cdrag_count > 0) {
            ac->cdrag = calloc(ac->cdrag_count, sizeof(cdrag_point_t));
            if (ac->cdrag) {
                for (int i = 0; i < ac->cdrag_count; i++) {
                    cJSON *point = cJSON_GetArrayItem(cdrag_arr, i);
                    ac->cdrag[i].speed_m  = json_double(point, "Speed", 0.0);
                    ac->cdrag[i].cdrag_0  = json_double(point, "Cdrag0", 0.0);
                }
            } else {
                ac->cdrag_count = 0;
            }
        }
    }

    return ac;
}

static void free_owned_aircraft(aircraft_t *ac) {
    if (!ac) return;
    free(ac->cdrag);
    free(ac);
}

static void free_owned_airport(airport_t *ap) {
    free(ap);
}

leg_t parse_leg(cJSON *obj) {
    leg_t leg;
    memset(&leg, 0, sizeof(leg));

    cJSON *dep         = cJSON_GetObjectItem(obj, "Departure");
    cJSON *arr         = cJSON_GetObjectItem(obj, "Arrival");
    cJSON *fuel        = cJSON_GetObjectItem(obj, "Fuel");
    cJSON *segs        = cJSON_GetObjectItem(obj, "Segments");
    cJSON *profile_obj = cJSON_GetObjectItem(obj, "FlightProfile");
    if (!profile_obj) profile_obj = cJSON_GetObjectItem(obj, "Flight Profile");

    cJSON *dep_airport = cJSON_GetObjectItem(obj, "DepartureAirport");
    cJSON *arr_airport = cJSON_GetObjectItem(obj, "ArrivalAirport");

    if (dep) {
        if (cJSON_IsString(dep)) {
            strncpy(leg.departure_id, dep->valuestring, sizeof(leg.departure_id) - 1);
        } else {
            cJSON *airport = cJSON_GetObjectItem(dep, "Airport");
            if (airport && cJSON_IsString(airport))
                strncpy(leg.departure_id, airport->valuestring, sizeof(leg.departure_id) - 1);
        }
    }

    // Arrival: string "MAD" ou objeto {Airport, ...}
    if (arr) {
        if (cJSON_IsString(arr)) {
            strncpy(leg.arrival_id, arr->valuestring, sizeof(leg.arrival_id) - 1);
        } else {
            cJSON *airport = cJSON_GetObjectItem(arr, "Airport");
            if (airport && cJSON_IsString(airport))
                strncpy(leg.arrival_id, airport->valuestring, sizeof(leg.arrival_id) - 1);
        }
    }

    if (fuel) leg.fuel = parse_quantity(fuel);


    if (profile_obj) {
        cJSON *climb_arr   = cJSON_GetObjectItem(profile_obj, "Climb");
        cJSON *descend_arr = cJSON_GetObjectItem(profile_obj, "Descend");
        leg.profile.climb   = parse_profile_array(climb_arr,   &leg.profile.climb_count);
        leg.profile.descend = parse_profile_array(descend_arr, &leg.profile.descend_count);
    }

    if (segs) {
        leg.segment_count = cJSON_GetArraySize(segs);
        leg.segments = malloc(leg.segment_count * sizeof(segment_t));
        for (int i = 0; i < leg.segment_count; i++) {
            cJSON *entry = cJSON_GetArrayItem(segs, i);
            cJSON *mode  = cJSON_GetObjectItem(entry, "Mode");
            cJSON *start = cJSON_GetObjectItem(entry, "Start");
            cJSON *end   = cJSON_GetObjectItem(entry, "End");
            cJSON *wind_dir = cJSON_GetObjectItem(entry, "WindDirectionDeg");
            cJSON *wind_spd = cJSON_GetObjectItem(entry, "WindSpeedMs");

            memset(&leg.segments[i], 0, sizeof(leg.segments[i]));
            if (mode) {
                strncpy(leg.segments[i].mode, mode->valuestring, sizeof(leg.segments[i].mode) - 1);
                leg.segments[i].mode[sizeof(leg.segments[i].mode) - 1] = '\0';
            }
            if (start) leg.segments[i].start = parse_position(start);
            if (end)   leg.segments[i].end   = parse_position(end);
            if (wind_dir && wind_spd) {
                leg.segments[i].has_wind = 1;
                leg.segments[i].wind_direction_deg = wind_dir->valueint;
                leg.segments[i].wind_speed_ms = wind_spd->valuedouble;
            }
        }
    }

    if (dep_airport) {
        leg.departure_airport_ptr = parse_airport_embedded(dep_airport);
        leg.departure_airport_owned = leg.departure_airport_ptr ? 1 : 0;
        if (leg.departure_airport_ptr && leg.departure_id[0] == '\0')
            safe_strncpy(leg.departure_id, leg.departure_airport_ptr->id, sizeof(leg.departure_id));
    }

    if (arr_airport) {
        leg.arrival_airport_ptr = parse_airport_embedded(arr_airport);
        leg.arrival_airport_owned = leg.arrival_airport_ptr ? 1 : 0;
        if (leg.arrival_airport_ptr && leg.arrival_id[0] == '\0')
            safe_strncpy(leg.arrival_id, leg.arrival_airport_ptr->id, sizeof(leg.arrival_id));
    }

    return leg;
}

flight_plan_t parse_flight_plan(const char *json_str) {
    flight_plan_t plan = {0};
    cJSON *root = cJSON_Parse(json_str);
    if (!root) {
        fprintf(stderr, "Erro ao fazer parse do JSON\n");
        return plan;
    }

    cJSON *id          = cJSON_GetObjectItem(root, "ID");
    cJSON *type        = cJSON_GetObjectItem(root, "Type");
    cJSON *route       = cJSON_GetObjectItem(root, "Route");
    cJSON *dep_time    = cJSON_GetObjectItem(root, "DepartureTime");
    cJSON *aircraft_id = cJSON_GetObjectItem(root, "AircraftId");
    cJSON *load        = cJSON_GetObjectItem(root, "Load");

    if (load) {
        cJSON *pax     = cJSON_GetObjectItem(load, "PassengerCount");
        cJSON *pax_w   = cJSON_GetObjectItem(load, "PassengerWeight");
        cJSON *cargo   = cJSON_GetObjectItem(load, "CargoWeight");
        cJSON *payload = cJSON_GetObjectItem(load, "TotalPayloadMassKg");

        if (pax)     plan.load.passenger_count     = pax->valueint;
        if (pax_w)   plan.load.passenger_weight_kg = parse_quantity(pax_w).quantity;
        if (cargo)   plan.load.cargo_weight_kg     = parse_quantity(cargo).quantity;
        plan.load.total_payload_mass_kg = plan.load.passenger_weight_kg + plan.load.cargo_weight_kg;
           
    }

    cJSON *legs = cJSON_GetObjectItem(root, "Legs");
    if (!legs) legs = cJSON_GetObjectItem(root, "Leg");

    cJSON *aircraft_obj = cJSON_GetObjectItem(root, "Aircraft");

    if (id)    plan.id = id->valueint;
    if (type)  strncpy(plan.type,        type->valuestring,  sizeof(plan.type)        - 1);
    if (route) strncpy(plan.route,       route->valuestring, sizeof(plan.route)       - 1);
    if (aircraft_id) {
        strncpy(plan.aircraft_id, aircraft_id->valuestring, sizeof(plan.aircraft_id) - 1);
    } else {
        cJSON *maker = cJSON_GetObjectItem(root, "AircraftMaker");
        if (maker) strncpy(plan.aircraft_id, maker->valuestring, sizeof(plan.aircraft_id) - 1);
    }


    if (legs && cJSON_IsArray(legs)) {
        plan.leg_count = cJSON_GetArraySize(legs);
        plan.legs = malloc(plan.leg_count * sizeof(leg_t));
        
        if (plan.legs) {
            for (int i = 0; i < plan.leg_count; i++) {
                plan.legs[i] = parse_leg(cJSON_GetArrayItem(legs, i));
            }

            cJSON *first_leg = cJSON_GetArrayItem(legs, 0);
            cJSON *dep_time = cJSON_GetObjectItem(first_leg, "DepartureTime");
            if (dep_time && cJSON_IsString(dep_time)) {
                plan.departure_time = parse_datetime(dep_time->valuestring);
            }
            plan.departure_time_s = datetime_to_epoch_s(plan.departure_time);

            cJSON *last_leg = cJSON_GetArrayItem(legs, plan.leg_count - 1);
            cJSON *arr_time = cJSON_GetObjectItem(last_leg, "ArrivalTime");
            if (arr_time && cJSON_IsString(arr_time)) {
                plan.arrival_time = parse_datetime(arr_time->valuestring);

                /* Se apenas continha "HH:MM" (year == 0), herda a data de partida */
                if (plan.arrival_time.year == 0) {          
                    if (plan.departure_time.year == 0) {
                        plan.arrival_time.year  = 1;
                        plan.arrival_time.month = 1;
                        plan.arrival_time.day   = 1;
                    } else {
                        plan.arrival_time.year  = plan.departure_time.year;
                        plan.arrival_time.month = plan.departure_time.month;
                        plan.arrival_time.day   = plan.departure_time.day;
                    }

                    if (datetime_to_seconds(plan.arrival_time) < datetime_to_seconds(plan.departure_time))
                        advance_day(&plan.arrival_time);
                }
            }
            plan.arrival_time_s = datetime_to_epoch_s(plan.arrival_time);
        }
    }

    if (aircraft_obj) {
        plan.aircraft_ptr = parse_aircraft_embedded(aircraft_obj);
        plan.aircraft_owned = plan.aircraft_ptr ? 1 : 0;
        if (plan.aircraft_ptr && plan.aircraft_id[0] == '\0')
            safe_strncpy(plan.aircraft_id, plan.aircraft_ptr->model_id, sizeof(plan.aircraft_id));
    }

    cJSON_Delete(root);
    return plan;
}

void free_flight_plan(flight_plan_t *plan) {
    if (plan != NULL) {
        if (plan->aircraft_owned && plan->aircraft_ptr)
            free_owned_aircraft(plan->aircraft_ptr);
        plan->aircraft_ptr = NULL;
        plan->aircraft_owned = 0;

        for (int i = 0; i < plan->leg_count; i++) {
            if (plan->legs[i].departure_airport_owned && plan->legs[i].departure_airport_ptr)
                free_owned_airport(plan->legs[i].departure_airport_ptr);
            if (plan->legs[i].arrival_airport_owned && plan->legs[i].arrival_airport_ptr)
                free_owned_airport(plan->legs[i].arrival_airport_ptr);
            free(plan->legs[i].segments);
            free(plan->legs[i].profile.climb);
            free(plan->legs[i].profile.descend);
        }
        free(plan->legs);
        plan->legs = NULL;
        plan->leg_count = 0;
    }
}


long long datetime_to_epoch_s(sim_datetime_t dt)
{
    // Se a estrutura estiver vazia ou inválida, evita gerar lixo
    if (dt.year == 0) return 0LL;

    struct tm t;
    memset(&t, 0, sizeof(struct tm));
    
    t.tm_year = dt.year - 1900; // O ano no C conta a partir de 1900
    t.tm_mon  = dt.month - 1;   // Os meses no C vão de 0 a 11
    t.tm_mday = dt.day;
    t.tm_hour = dt.hour;
    t.tm_min  = dt.min;
    t.tm_sec  = dt.sec;

    time_t epoch = mktime(&t);
    if (epoch == -1) {
        return 0LL;
    }
    
    return (long long)epoch;
}