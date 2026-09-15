#include "simulator_includes.h"

#define DEFAULT_REPORTS_DIR "../../reports/run"
#define PATH_MAX_LEN 512

static int ensure_dir_exists(const char *path)
{
    char buf[PATH_MAX_LEN];
    size_t len = strlen(path);
    if (len == 0 || len >= sizeof(buf))
        return -1;
    memcpy(buf, path, len + 1);
    for (size_t i = 1; i < len; i++) {
        if (buf[i] != '/')
            continue;
        buf[i] = '\0';
        struct stat st;
        if (stat(buf, &st) == -1 && mkdir(buf, 0777) != 0 && errno != EEXIST)
            return -1;
        buf[i] = '/';
    }
    struct stat st;
    if (stat(buf, &st) == -1 && mkdir(buf, 0777) != 0 && errno != EEXIST)
        return -1;
    return 0;
}

int reports_path_prepare(char *txt_path, size_t txt_sz,
                         char *csv_path, size_t csv_sz,
                         int n_flights)
{
    const char *env_reports_dir = getenv("FS_REPORTS_DIR");
    const char *reports_dir = (env_reports_dir != NULL && env_reports_dir[0] != '\0')
        ? env_reports_dir
        : DEFAULT_REPORTS_DIR;

    if (ensure_dir_exists(reports_dir) != 0)
        return -1;

    if (env_reports_dir != NULL && env_reports_dir[0] != '\0') {
        snprintf(txt_path, txt_sz, "%s/report.txt", reports_dir);
        snprintf(csv_path, csv_sz, "%s/report.csv", reports_dir);
    } else {
        time_t t = time(NULL);
        struct tm tm = *localtime(&t);
        snprintf(txt_path, txt_sz,
                 "%s/%d_%04d-%02d-%02d_%02d-%02d-%02d.txt",
                 reports_dir, n_flights, tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday,
                 tm.tm_hour, tm.tm_min, tm.tm_sec);
        snprintf(csv_path, csv_sz,
                 "%s/%d_%04d-%02d-%02d_%02d-%02d-%02d.csv",
                 reports_dir, n_flights, tm.tm_year + 1900, tm.tm_mon + 1, tm.tm_mday,
                 tm.tm_hour, tm.tm_min, tm.tm_sec);
    }
    return 0;
}
