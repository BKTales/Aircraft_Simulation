#include "simulator_includes.h"

/* ═══════════════════════════════════════════════════════════════════════════
 * Internal constants
 * ═══════════════════════════════════════════════════════════════════════════ */

#define FT_TO_M          0.3048          /* feet  → metres              */
#define LBF_TO_N         4.44822162      /* pound-force → Newton        */
#define LBM_TO_KG        0.45359237      /* pound-mass  → kilogram      */
#define GAL_US_TO_L      3.785411784     /* US gallon   → litre         */
#define KT_TO_MS         0.514444444     /* knot        → m/s           */
#define JETA_DENSITY_KGL 0.804           /* jet-A density, kg/L         */

/* ═══════════════════════════════════════════════════════════════════════════
 * Unit-aware raw value
 * ═══════════════════════════════════════════════════════════════════════════ */

typedef enum {
    U_SI      = 0,   /* already SI — store as-is                      */
    U_US_FT   = 1,   /* US altitude / distance (feet)                 */
    U_US_LBF  = 2,   /* US force (pound-force)                        */
    U_US_LBM  = 3,   /* US mass (pound-mass)                          */
    U_US_GAL  = 4,   /* US volume (gallons) — fuel capacity           */
    U_KNOT    = 5,   /* knots                                         */
    U_MACH    = 6,   /* Mach number — dimensionless, pass through     */
    U_NONE    = 7    /* no unit suffix — numeric literal, pass through */
} raw_unit_t;

typedef struct {
    double     value;
    raw_unit_t unit;
    int        valid;   /* 0 if parse failed */
} raw_value_t;

/* ───────────────────────────────────────────────────────────────────────────
 * parse_raw_value
 *
 * Reads  "<number> <unit>"  from a string.
 * The unit suffix is compared case-sensitively against the XML values used
 * in the aircraft definition file.  The caller supplies a "context" string
 * (field name) so that the generic "US" tag can be disambiguated.
 *
 *   "US" on altitudes / distances → feet
 *   "US" on weights / thrust      → pound-force  (lbf)
 *   "US" on fuel capacity         → US gallons
 * ─────────────────────────────────────────────────────────────────────────── */
static raw_value_t parse_raw_value(const char *str, const char *field_name)
{
    raw_value_t rv = { 0.0, U_NONE, 0 };

    if (!str || !str[0]) return rv;

    char unit_buf[32] = { 0 };
    int  n = sscanf(str, "%lf %31s", &rv.value, unit_buf);

    if (n < 1) return rv;   /* not even a number */
    rv.valid = 1;

    if (n < 2 || unit_buf[0] == '\0') {
        rv.unit = U_NONE;   /* dimensionless literal */
        return rv;
    }

    if (strcmp(unit_buf, "SI") == 0) {
        rv.unit = U_SI;
    } else if (strcmp(unit_buf, "M") == 0) {
        rv.unit = U_MACH;
    } else if (strcmp(unit_buf, "Knot") == 0) {
        rv.unit = U_KNOT;
    } else if (strcmp(unit_buf, "US") == 0) {
        /* Disambiguate by field name */
        if (field_name && (strstr(field_name, "altitude") ||
                           strstr(field_name, "distance") ||
                           strstr(field_name, "range"))) {
            rv.unit = U_US_FT;
        } else if (field_name && strstr(field_name, "fuel")) {
            rv.unit = U_US_GAL;
        } else if (field_name && (strstr(field_name, "weight") ||
                                  strstr(field_name, "payload") ||
                                  strstr(field_name, "MTOW")    ||
                                  strstr(field_name, "EWeight"))) {
            rv.unit = U_US_LBM;
        } else {
            /* thrust or unknown → treat as lbf */
            rv.unit = U_US_LBF;
        }
    } else {
        /* Unrecognised suffix: log and treat as SI */
        fprintf(stderr, "WARN: unknown unit '%s' for field '%s', treating as SI\n",
                unit_buf, field_name ? field_name : "?");
        rv.unit = U_SI;
    }

    return rv;
}

/* ───────────────────────────────────────────────────────────────────────────
 * to_si
 *
 * Convert a raw_value_t to its canonical SI value.
 * fuel_is_mass: when true, US gallons are converted to kg via jet-A density.
 * ─────────────────────────────────────────────────────────────────────────── */
static double to_si(raw_value_t rv, int fuel_to_kg)
{
    if (!rv.valid) return 0.0;

    switch (rv.unit) {
        case U_SI:     return rv.value;
        case U_NONE:   return rv.value;
        case U_MACH:   return rv.value;
        case U_US_FT:  return rv.value * FT_TO_M;
        case U_US_LBF: return rv.value * LBF_TO_N;
        case U_US_LBM: return rv.value * LBM_TO_KG;
        case U_US_GAL:
            if (fuel_to_kg)
                return rv.value * GAL_US_TO_L * JETA_DENSITY_KGL;
            else
                return rv.value * GAL_US_TO_L;
        case U_KNOT:   return rv.value * KT_TO_MS;
        default:       return rv.value;
    }
}

/* Convenience: parse a field and convert immediately */
static double parse_si(const char *str, const char *field, int fuel_to_kg)
{
    return to_si(parse_raw_value(str, field), fuel_to_kg);
}

/* ═══════════════════════════════════════════════════════════════════════════
 * Small safety helpers
 * ═══════════════════════════════════════════════════════════════════════════ */

static void safe_strncpy(char *dst, const char *src, size_t size)
{
    if (!src || size == 0) { if (size) dst[0] = '\0'; return; }
    strncpy(dst, src, size - 1);
    dst[size - 1] = '\0';
}

/* xmlNodeGetContent wrapper: caller must xmlFree() the result. */
static char *node_content(xmlNode *n)
{
    return (char *)xmlNodeGetContent(n);
}

/* ═══════════════════════════════════════════════════════════════════════════
 * Cdrag point parser
 * ═══════════════════════════════════════════════════════════════════════════ */

/*
 * Cdrag speed values in the XML use "M" (Mach) — stored as dimensionless.
 */
static int parse_cdrag_point(xmlNode *iten, cdrag_point_t *out)
{
    if (!iten || !out) return -1;

    memset(out, 0, sizeof(*out));
    int got_speed = 0, got_cd = 0;

    for (xmlNode *c = iten->children; c; c = c->next) {
        if (c->type != XML_ELEMENT_NODE) continue;

        char *content = node_content(c);
        if (!content) continue;

        const char *name = (const char *)c->name;

        if (strcmp(name, "speed") == 0) {
            raw_value_t rv = parse_raw_value(content, "speed");
            /* speed here is Mach; to_si on U_MACH returns value unchanged */
            out->speed_m = to_si(rv, 0);
            got_speed = 1;
        } else if (strcmp(name, "Cdrag_0") == 0) {
            raw_value_t rv = parse_raw_value(content, "Cdrag_0");
            out->cdrag_0 = to_si(rv, 0);   /* dimensionless */
            got_cd = 1;
        }

        xmlFree(content);
    }

    return (got_speed && got_cd) ? 0 : -1;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * Cdrag table parser
 * ═══════════════════════════════════════════════════════════════════════════ */

static int parse_cdrag_function(xmlNode *cdrag_node, aircraft_t *ac)
{
    if (!cdrag_node || !ac) return -1;

    /* Count valid <iten> children first to allocate exactly what we need */
    int count = 0;
    for (xmlNode *c = cdrag_node->children; c; c = c->next)
        if (c->type == XML_ELEMENT_NODE && strcmp((char *)c->name, "iten") == 0)
            count++;

    if (count == 0) return 0;   /* empty table is not an error per se */

    ac->cdrag = malloc((size_t)count * sizeof(cdrag_point_t));
    if (!ac->cdrag) {
        fprintf(stderr, "ERROR: malloc failed for cdrag table (%d points)\n", count);
        return -1;
    }

    int idx = 0;
    for (xmlNode *c = cdrag_node->children; c && idx < count; c = c->next) {
        if (c->type != XML_ELEMENT_NODE) continue;
        if (strcmp((char *)c->name, "iten") != 0) continue;

        cdrag_point_t pt;
        if (parse_cdrag_point(c, &pt) == 0)
            ac->cdrag[idx++] = pt;
        else
            fprintf(stderr, "WARN: skipping malformed <iten> in Cdrag_function\n");
    }

    ac->cdrag_count = idx;
    return 0;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * thrust_function sub-parser (child of <motorization>)
 * ═══════════════════════════════════════════════════════════════════════════ */

static void parse_thrust_function(xmlNode *tf_node, aircraft_t *ac)
{
    for (xmlNode *c = tf_node->children; c; c = c->next) {
        if (c->type != XML_ELEMENT_NODE) continue;

        char *content = node_content(c);
        if (!content) continue;

        const char *name = (const char *)c->name;

        if (strcmp(name, "thrust_0") == 0) {
            /* Thrust at static (zero speed) - SI units: Newtons
               Example: 4.89E+05 N (≈ GE90 static thrust) */
            ac->thrust_0 = parse_si(content, "thrust", 0);
        } else if (strcmp(name, "thrust_max_speed") == 0) {
            /* Thrust at maximum speed - SI units: Newtons
               Example: 3.0E+05 N (reduced at cruise) */
            ac->thrust_max_speed = parse_si(content, "thrust", 0);
        } else if (strcmp(name, "max_speed") == 0) {
            raw_value_t rv = parse_raw_value(content, "max_speed");
            double mach = to_si(rv, 0);
            ac->max_speed = mach;
        }

        xmlFree(content);
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
 * <motorization> parser
 * ═══════════════════════════════════════════════════════════════════════════ */

static void parse_motorization(xmlNode *motor_node, aircraft_t *ac)
{
    for (xmlNode *c = motor_node->children; c; c = c->next) {
        if (c->type != XML_ELEMENT_NODE) continue;

        const char *name = (const char *)c->name;

        /* Handle sub-tree nodes without calling xmlNodeGetContent on them */
        if (strcmp(name, "thrust_function") == 0) {
            parse_thrust_function(c, ac);
            continue;
        }

        char *content = node_content(c);
        if (!content) continue;

        if (strcmp(name, "number_motors") == 0) {
            long v = strtol(content, NULL, 10);
            ac->number_motors = (int)v;
        } else if (strcmp(name, "motor") == 0) {
            safe_strncpy(ac->motor, content, sizeof(ac->motor));
        } else if (strcmp(name, "motor_type") == 0) {
            safe_strncpy(ac->motor_type, content, sizeof(ac->motor_type));
        } else if (strcmp(name, "cruise_altitude") == 0) {
            ac->cruise_altitude = parse_si(content, "altitude", 0);
        } else if (strcmp(name, "cruise_speed") == 0) {
            raw_value_t rv = parse_raw_value(content, "cruise_speed");
            ac->cruise_speed = to_si(rv, 0);
        } else if (strcmp(name, "TSFC") == 0) {
            ac->tsfc = parse_si(content, "TSFC", 0);  
            ac->tsfc = ac->tsfc / G;
        } else if (strcmp(name, "lapse_rate_factor") == 0) {
            ac->lapse_rate_factor = parse_si(content, "lapse_rate_factor", 0);
        }

        xmlFree(content);
    }
}

/* ═══════════════════════════════════════════════════════════════════════════
 * Single <aircraft> node parser
 * ═══════════════════════════════════════════════════════════════════════════ */

static int parse_aircraft_node(xmlNode *node, aircraft_t *out)
{
    if (!node || !out) return -1;

    memset(out, 0, sizeof(*out));

    /* Attributes */
    xmlChar *model_id = xmlGetProp(node, BAD_CAST "model_ID");
    if (model_id) {
        safe_strncpy(out->model_id, (char *)model_id, sizeof(out->model_id));
        xmlFree(model_id);
    } else {
        fprintf(stderr, "WARN: <aircraft> missing model_ID attribute\n");
    }

    xmlChar *description = xmlGetProp(node, BAD_CAST "description");
    if (description) {
        safe_strncpy(out->description, (char *)description, sizeof(out->description));
        xmlFree(description);
    }

    /* Child elements */
    for (xmlNode *c = node->children; c; c = c->next) {
        if (c->type != XML_ELEMENT_NODE) continue;

        const char *name = (const char *)c->name;

        /* Sub-tree nodes — do NOT call xmlNodeGetContent on these */
        if (strcmp(name, "motorization") == 0) {
            parse_motorization(c, out);
            continue;
        }
        if (strcmp(name, "Cdrag_function") == 0) {
            parse_cdrag_function(c, out);
            continue;
        }

        /* Leaf nodes */
        char *content = node_content(c);
        if (!content) continue;

        if (strcmp(name, "maker") == 0) {
            safe_strncpy(out->maker, content, sizeof(out->maker));
        } else if (strcmp(name, "type") == 0) {
            safe_strncpy(out->type, content, sizeof(out->type));
        } else if (strcmp(name, "EWeight") == 0) {
            out->eweight = parse_si(content, "EWeight", 0);
        } else if (strcmp(name, "MTOW") == 0) {
            out->mtow = parse_si(content, "MTOW", 0);
        } else if (strcmp(name, "max_payload") == 0) {
            out->max_payload = parse_si(content, "payload", 0);
        } else if (strcmp(name, "fuel_capacity") == 0) {
            /*
             * Fuel capacity: US gallons → kg via jet-A density,
             * SI value assumed to already be in kg.
             */
            raw_value_t rv = parse_raw_value(content, "fuel");
            if (rv.unit == U_SI || rv.unit == U_NONE) {
                out->fuel_capacity = rv.value;          /* already kg */
            } else {
                out->fuel_capacity = to_si(rv, 1);      /* gallons → kg */
            }
        } else if (strcmp(name, "VMO") == 0) {
            out->vmo = parse_si(content, "VMO", 0);     /* knots → m/s */
        } else if (strcmp(name, "MMO") == 0) {
            raw_value_t rv = parse_raw_value(content, "MMO");
            out->mmo = to_si(rv, 0);                    /* Mach, pass-through */
        } else if (strcmp(name, "wing_area") == 0) {
            out->wing_area = parse_si(content, "wing_area", 0);
        } else if (strcmp(name, "wing_span") == 0) {
            out->wing_span = parse_si(content, "wing_span", 0);
        } else if (strcmp(name, "aspect_ratio") == 0) {
            out->aspect_ratio = parse_si(content, "aspect_ratio", 0);
        } else if (strcmp(name, "e") == 0) {
            out->e = parse_si(content, "e", 0);
        } else {
            fprintf(stderr, "WARN: unknown field <%s> in aircraft '%s'\n",
                    name, out->model_id);
        }

        xmlFree(content);
    }

    /* Basic sanity: warn if critical fields are zero */
    if (out->mtow <= 0.0)
        fprintf(stderr, "WARN: aircraft '%s' has MTOW == 0\n", out->model_id);
    if (out->wing_area <= 0.0)
        fprintf(stderr, "WARN: aircraft '%s' has wing_area == 0\n", out->model_id);

    return 0;
}

/* ═══════════════════════════════════════════════════════════════════════════
 * Public API
 * ═══════════════════════════════════════════════════════════════════════════ */

aircraft_list_t load_aircraft_list(const char *xml_path)
{
    aircraft_list_t list = { NULL, 0 };

    if (!xml_path || xml_path[0] == '\0') {
        fprintf(stderr, "ERROR: load_aircraft_list: null or empty path\n");
        return list;
    }

    xmlDoc *doc = xmlReadFile(xml_path, NULL, XML_PARSE_NONET | XML_PARSE_PEDANTIC);
    if (!doc) {
        fprintf(stderr, "ERROR: failed to parse XML: %s\n", xml_path);
        return list;
    }

    xmlNode *root = xmlDocGetRootElement(doc);
    if (!root) {
        fprintf(stderr, "ERROR: XML has no root element: %s\n", xml_path);
        xmlFreeDoc(doc);
        return list;
    }
    if (strcmp((char *)root->name, "aircraft_list") != 0) {
        fprintf(stderr, "ERROR: unexpected root element <%s>, expected <aircraft_list>\n",
                (char *)root->name);
        xmlFreeDoc(doc);
        return list;
    }

    /* Count <aircraft> children to allocate exactly once */
    int count = 0;
    for (xmlNode *n = root->children; n; n = n->next)
        if (n->type == XML_ELEMENT_NODE &&
            strcmp((char *)n->name, "aircraft") == 0)
            count++;

    if (count == 0) {
        fprintf(stderr, "WARN: no <aircraft> entries found in %s\n", xml_path);
        xmlFreeDoc(doc);
        return list;
    }

    list.aircrafts = calloc((size_t)count, sizeof(aircraft_t));
    if (!list.aircrafts) {
        fprintf(stderr, "ERROR: calloc failed for %d aircraft\n", count);
        xmlFreeDoc(doc);
        return list;
    }

    int idx = 0;
    for (xmlNode *n = root->children; n && idx < count; n = n->next) {
        if (n->type != XML_ELEMENT_NODE) continue;
        if (strcmp((char *)n->name, "aircraft") != 0) continue;

        if (parse_aircraft_node(n, &list.aircrafts[idx]) == 0) {
            idx++;
        } else {
            fprintf(stderr, "ERROR: failed to parse aircraft at index %d, skipping\n", idx);
        }
    }

    list.count = idx;
    xmlFreeDoc(doc);
    return list;
}

/* ─────────────────────────────────────────────────────────────────────────── */

aircraft_t *find_aircraft(const aircraft_list_t *list, const char *model_id)
{
    if (!list || !model_id || model_id[0] == '\0') return NULL;

    for (int i = 0; i < list->count; i++)
        if (strcmp(list->aircrafts[i].model_id, model_id) == 0)
            return &list->aircrafts[i];

    return NULL;
}

/* ─────────────────────────────────────────────────────────────────────────── */

void free_aircraft_list(aircraft_list_t *list)
{
    if (!list) return;

    if (list->aircrafts) {
        for (int i = 0; i < list->count; i++)
            free(list->aircrafts[i].cdrag);   /* free(NULL) is safe */
        free(list->aircrafts);
    }

    list->aircrafts = NULL;
    list->count     = 0;
}

/* ─────────────────────────────────────────────────────────────────────────── */

void print_aircraft(const aircraft_t *ac)
{
    if (!ac) { printf("(null aircraft)\n"); return; }

    printf("─────────────────────────────────────\n");
    printf("  Model   : %s (%s)\n",  ac->model_id,    ac->description);
    printf("  Maker   : %s  type=%s\n", ac->maker,    ac->type);
    printf("  Motors  : %d × %s (%s)\n",
           ac->number_motors, ac->motor, ac->motor_type);
    printf("  Cruise  : alt=%.0f m   Mach=%.3f\n",
           ac->cruise_altitude, ac->cruise_speed);
    printf("  TSFC    : %.4e kg/(N·s)\n", ac->tsfc);
    printf("  Thrust  : T0=%.0f N  Tmax=%.0f N  Vmax=Mach %.2f\n",
           ac->thrust_0, ac->thrust_max_speed, ac->max_speed);
    printf("  Weights : EW=%.0f kg  MTOW=%.0f kg  PLD=%.0f kg\n",
           ac->eweight, ac->mtow, ac->max_payload);
    printf("  Fuel    : %.0f kg\n", ac->fuel_capacity);
    printf("  Limits  : VMO=%.1f m/s  MMO=%.3f\n", ac->vmo, ac->mmo);
    printf("  Wing    : S=%.1f m²  b=%.2f m  AR=%.2f  e=%.3f\n",
           ac->wing_area, ac->wing_span, ac->aspect_ratio, ac->e);
    printf("  Cdrag   : %d points\n", ac->cdrag_count);
    for (int i = 0; i < ac->cdrag_count; i++)
        printf("            Mach=%.3f  Cd0=%.5f\n",
               ac->cdrag[i].speed_m, ac->cdrag[i].cdrag_0);
    printf("─────────────────────────────────────\n");
}