#ifndef AIRCRAFT_PARSER_HEADER
#define AIRCRAFT_PARSER_HEADER

/* ── Unit system ────────────────────────────────────────────────────────────
 * All values stored in aircraft_t are in SI:
 *   distances / altitudes  → metres   (m)
 *   speeds (non-Mach)      → m/s
 *   Mach numbers           → dimensionless
 *   masses                 → kg
 *   forces / thrust        → N
 *   areas                  → m²
 *   fuel                   → kg  (jet-A density 0.800 kg/L assumed)
 *   TSFC                   → kg/(N·s)
 * ────────────────────────────────────────────────────────────────────────── */

/* ── Public API ─────────────────────────────────────────────────────────── */

/**
 * load_aircraft_list
 *
 * Parse the aircraft XML file at @xml_path and return a fully populated
 * aircraft_list_t.  All numeric fields are converted to SI on load.
 *
 * On error the returned list has count == 0 and aircrafts == NULL.
 * The caller must eventually call free_aircraft_list().
 */
aircraft_list_t load_aircraft_list(const char *xml_path);

/**
 * find_aircraft
 *
 * Linear search by model_id (case-sensitive).
 * Returns a pointer into @list->aircrafts, or NULL if not found.
 * The pointer is valid as long as @list is alive and unmodified.
 */
aircraft_t *find_aircraft(const aircraft_list_t *list, const char *model_id);

/**
 * free_aircraft_list
 *
 * Release all memory owned by @list and zero the struct.
 * Safe to call with NULL or an already-freed list.
 */
void free_aircraft_list(aircraft_list_t *list);

/**
 * print_aircraft  (debug helper)
 */
void print_aircraft(const aircraft_t *ac);

#endif /* AIRCRAFT_PARSER_HEADER */