#include "simulator_includes.h"

char* read_flight_plan(const char* path){
    FILE *f = fopen(path, "rb");
    if (!f) return NULL;

    fseek(f, 0, SEEK_END);
    long size = ftell(f);
    rewind(f);

    char *buf = malloc(size + 1);
    fread(buf, 1, size, f);
    buf[size] = '\0';
    fclose(f);
    return buf;
}

int get_flight_plans(program_t* prog, const char* dir_path){
    DIR *dir = opendir(dir_path);

    if (!dir) {
        perror("Error opening directory");
        return -1;
    }

    int cont = 0;
    struct dirent *entry;
    while ((entry = readdir(dir)) != NULL) {


        // Filters only .json files
        const char *ext = strrchr(entry->d_name, '.');
        if (!ext || strcmp(ext, ".json") != 0) continue;

        // Build the full path to the file
        char path[512];
        snprintf(path, sizeof(path), "%s/%s", dir_path, entry->d_name);

        prog->flight_plans_info->plans = realloc(prog->flight_plans_info->plans, (cont + 1) * sizeof(flight_plan_t));
        if (!prog->flight_plans_info->plans) {
            perror("Error reallocating memory for flight plans");
            closedir(dir);
            return -1;
        }

        if (getenv("FS_VERBOSE_LOAD"))
            dprintf(STDERR_FILENO, "Reading file: %s\n", entry->d_name);

        // Reads and parses the file
        char *content = read_flight_plan(path);
        if (!content) continue;

        
        flight_plan_t plan = parse_flight_plan(content);
        
        if(plan.id == 0) {
            fprintf(stderr, "Error parsing flight plan: %s\n", path);
            free(content);
            continue;
        } else {
            prog->flight_plans_info->plans[cont++] = plan;
            prog->flight_plans_info->count++;
            if (getenv("FS_VERBOSE_LOAD"))
                dprintf(STDERR_FILENO, "Flight plan ID %d loaded successfully\n", plan.id);
            free(content);
        }
    }

    closedir(dir);
    if (cont > 0) {
        dprintf(STDERR_FILENO, "[INIT] Loaded %d flight plan(s)\n", cont);
    }
    return 0;
}