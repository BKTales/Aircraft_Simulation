#ifndef SIMULATOR_INCLUDES_H
#define SIMULATOR_INCLUDES_H

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

/* C standard / POSIX */
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <math.h>
#include <pthread.h>
#include <semaphore.h>
#include <signal.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

/* Third party */
#include <libxml/parser.h>
#include <libxml/tree.h>
#include "../lib/cjson/cJSON.h"

/* Project (dependency order) */
#include "math_utils/constants.h"
#include "flight_simulator.h"
#include "math_utils/math_utils.h"
#include "physics/atmosphere.h"
#include "physics/physics.h"
#include "flight.h"
#include "ipc/sim_ipc.h"
#include "utils/aircraft_parser.h"
#include "utils/network_parser.h"
#include "utils/flight_plan_utils.h"
#include "utils/flight_report.h"
#include "utils/sim_time_utils.h"
#include "utils/sim_clock.h"
#include "utils/process_spawn.h"
#include "utils/parent_safety.h"
#include "utils/sim_shutdown.h"
#include "parent_threading/parent_threading.h"
#include "utils/sim_dedicated_thread.h"
#include "utils/weather_config.h"
#include "utils/sim_log.h"
#include "utils/reports_path.h"
#include "server/sse_server.h"

#endif /* SIMULATOR_INCLUDES_H */
