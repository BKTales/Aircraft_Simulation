#include "simulator_includes.h"

/* ─────────────────────────────────────────────
 * Helpers
 * ───────────────────────────────────────────── */

static void safe_strncpy(char *dst, const char *src, size_t size)
{
    if (!src || size == 0) return;
    strncpy(dst, src, size - 1);
    dst[size - 1] = '\0';
}

static char *get_content(xmlNode *n)
{
    return (char *)xmlNodeGetContent(n);
}

/* ─────────────────────────────────────────────
 * Parse position (uses your position_t)
 * ───────────────────────────────────────────── */

static void parse_location(xmlNode *node, position_t *pos)
{
    memset(pos, 0, sizeof(*pos));

    strcpy(pos->altitude.unit, "m");

    for (xmlNode *c = node->children; c; c = c->next)
    {
        if (c->type != XML_ELEMENT_NODE) continue;

        char *content = get_content(c);
        if (!content) continue;

        if (strcmp((char*)c->name, "latitude") == 0)
        {
            pos->latitude = atof(content);
        }
        else if (strcmp((char*)c->name, "longitude") == 0)
        {
            pos->longitude = atof(content);
        }
        else if (strcmp((char*)c->name, "altitude") == 0)
        {
            double v = 0.0;
            sscanf(content, "%lf", &v);
            pos->altitude.quantity = v;
        }

        xmlFree(content);
    }
}

/* ─────────────────────────────────────────────
 * Airport parser
 * ───────────────────────────────────────────── */

static int parse_airport(xmlNode *node, airport_t *ap)
{
    memset(ap, 0, sizeof(*ap));

    xmlChar *id = xmlGetProp(node, BAD_CAST "id");
    if (!id) return -1;

    safe_strncpy(ap->id, (char*)id, sizeof(ap->id));
    xmlFree(id);

    for (xmlNode *c = node->children; c; c = c->next)
    {
        if (c->type != XML_ELEMENT_NODE) continue;

        char *content = get_content(c);
        if (!content) continue;

        const char *name = (const char*)c->name;

        if (strcmp(name, "name") == 0)
        {
            safe_strncpy(ap->name, content, sizeof(ap->name));
        }
        else if (strcmp(name, "town") == 0)
        {
            safe_strncpy(ap->town, content, sizeof(ap->town));
        }
        else if (strcmp(name, "country") == 0)
        {
            safe_strncpy(ap->country, content, sizeof(ap->country));
        }
        else if (strcmp(name, "location") == 0)
        {
            parse_location(c, &ap->location);
        }

        xmlFree(content);
    }

    return 0;
}

/* ─────────────────────────────────────────────
 * Airport list parser
 * ───────────────────────────────────────────── */

static int parse_airport_list(xmlNode *root, airport_list_t *list)
{
    int count = 0;

    for (xmlNode *c = root->children; c; c = c->next)
    {
        if (c->type == XML_ELEMENT_NODE &&
            strcmp((char*)c->name, "airport") == 0)
        {
            count++;
        }
    }

    if (count == 0)
        return 0;

    list->airports = calloc(count, sizeof(airport_t));
    if (!list->airports)
        return -1;

    int idx = 0;

    for (xmlNode *c = root->children; c; c = c->next)
    {
        if (c->type != XML_ELEMENT_NODE) continue;
        if (strcmp((char*)c->name, "airport") != 0) continue;

        if (parse_airport(c, &list->airports[idx]) == 0)
            idx++;
    }

    list->count = idx;
    return 0;
}

/* ─────────────────────────────────────────────
 * Network parser (root)
 * ───────────────────────────────────────────── */

network_t load_network(const char *xml_path)
{
    network_t net;
    memset(&net, 0, sizeof(net));

    xmlDoc *doc = xmlReadFile(xml_path, NULL, XML_PARSE_NONET);
    if (!doc)
    {
        fprintf(stderr, "ERROR: cannot read network XML\n");
        return net;
    }

    xmlNode *root = xmlDocGetRootElement(doc);
    if (!root || strcmp((char*)root->name, "Network") != 0)
    {
        fprintf(stderr, "ERROR: invalid root <Network>\n");
        xmlFreeDoc(doc);
        return net;
    }

    xmlChar *id = xmlGetProp(root, BAD_CAST "id");
    if (id)
    {
        safe_strncpy(net.id, (char*)id, sizeof(net.id));
        xmlFree(id);
    }

    xmlChar *desc = xmlGetProp(root, BAD_CAST "description");
    if (desc)
    {
        safe_strncpy(net.description, (char*)desc, sizeof(net.description));
        xmlFree(desc);
    }

    for (xmlNode *c = root->children; c; c = c->next)
    {
        if (c->type != XML_ELEMENT_NODE) continue;

        if (strcmp((char*)c->name, "airport_list") == 0)
        {
            parse_airport_list(c, &net.airports);
        }
    }

    xmlFreeDoc(doc);
    return net;
}

/* ─────────────────────────────────────────────
 * Find airport by ID
 * ───────────────────────────────────────────── */

airport_t *find_airport(const airport_list_t *list, const char *id)
{
    if (!list || !id) return NULL;

    for (int i = 0; i < list->count; i++)
    {
        if (strcmp(list->airports[i].id, id) == 0)
            return &list->airports[i];
    }

    return NULL;
}


static void invalidate_plan(flight_plan_t *plan)
{
    fprintf(stderr,
        "[FLIGHT PLAN] INVALID → ID=%d Route=%s (removed)\n",
        plan->id, plan->route);

    plan->aircraft_id[0] = '\0';
    plan->aircraft_ptr = NULL;
    plan->leg_count = 0;
}

/* ─────────────────────────────────────────────
 * Free memory
 * ───────────────────────────────────────────── */

void free_network(network_t *net)
{
    if (!net) return;

    if (net->airports.airports)
        free(net->airports.airports);

    net->airports.airports = NULL;
    net->airports.count = 0;
}

/* ─────────────────────────────────────────────
 * Debug print
 * ───────────────────────────────────────────── */

void print_network(const network_t *net)
{
    if (!net) return;

    printf("════════ NETWORK ════════\n");
    printf("ID: %s\n", net->id);
    printf("Description: %s\n", net->description);
    printf("Airports: %d\n\n", net->airports.count);

    for (int i = 0; i < net->airports.count; i++)
    {
        airport_t *a = &net->airports.airports[i];

        printf("[%s] %s (%s)\n",
               a->id, a->name, a->town);

        printf("   Country: %s\n", a->country);
        printf("   Lat: %.4f Lon: %.4f Alt: %.1f m\n",
               a->location.latitude,
               a->location.longitude,
               a->location.altitude.quantity);
    }

    printf("═════════════════════════\n");
}