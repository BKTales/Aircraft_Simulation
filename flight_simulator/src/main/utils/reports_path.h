#ifndef REPORTS_PATH_H
#define REPORTS_PATH_H

/* Resolve FS_REPORTS_DIR (or default), mkdir -p, build txt/csv paths. */
int reports_path_prepare(char *txt_path, size_t txt_sz,
                         char *csv_path, size_t csv_sz,
                         int n_flights);

#endif
