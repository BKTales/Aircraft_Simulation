#ifndef NETWORK_PARSER_H
#define NETWORK_PARSER_H

/* Load full network from XML */
network_t load_network(const char *xml_path);

/* Find airport by ID */
airport_t *find_airport(const airport_list_t *list, const char *id);

/* Free memory */
void free_network(network_t *net);

/* Debug print */
void print_network(const network_t *net);

#endif