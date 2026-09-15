WFLAGS = -Wall -g -Wextra -I. $(XML_CFLAGS)

# ─────────────────────────────
# libxml2
# ─────────────────────────────
XML_CFLAGS = $(shell pkg-config --cflags libxml-2.0)
XML_LIBS   = $(shell pkg-config --libs libxml-2.0)

CJSON_DIR = ../lib/cjson
UTILS_DIR = utils

# librt (shm_open, etc.) — Linux only
UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Linux)
RT_LIBS = -lrt
endif

# ─────────────────────────────
# Object files
# ─────────────────────────────
OBJS = \
    flight_process.o \
    flight_simulator.o \
    init.o \
    flight.o \
    ipc/sim_shm.o \
    ipc/sim_sem.o \
    parent_threading/parent_threading.o \
    physics/atmosphere.o \
    physics/physics.o \
    math_utils/math_utils.o \
    server/sse_server.o \
    $(CJSON_DIR)/cJSON.o \
    $(UTILS_DIR)/flight_plan_parser.o \
    $(UTILS_DIR)/flight_plan_reader.o \
    $(UTILS_DIR)/flight_plan_utils.o \
    $(UTILS_DIR)/aircraft_parser.o \
    $(UTILS_DIR)/network_parser.o \
    $(UTILS_DIR)/flight_report.o \
    $(UTILS_DIR)/sim_time_utils.o \
    $(UTILS_DIR)/sim_clock.o \
    $(UTILS_DIR)/process_spawn.o \
    $(UTILS_DIR)/parent_safety.o \
    $(UTILS_DIR)/sim_shutdown.o \
    $(UTILS_DIR)/sim_log.o \
    $(UTILS_DIR)/reports_path.o \
	$(UTILS_DIR)/sim_dedicated_thread.o \
	$(UTILS_DIR)/weather_config.o

.PHONY: all clean run weather_config_test test

all: flight_simulator

weather_config_test: tests/weather_config_test.c utils/weather_config.o ../lib/cjson/cJSON.o
	gcc $(WFLAGS) tests/weather_config_test.c utils/weather_config.o ../lib/cjson/cJSON.o \
		-o weather_config_test -lm

test: weather_config_test
	./weather_config_test

flight_simulator: $(OBJS)
	gcc $(WFLAGS) $(OBJS) -o flight_simulator -lm -pthread $(RT_LIBS) $(XML_LIBS)

# ─────────────────────────────
# Regra de Compilação Genérica (Local e Subpastas locais)
# ─────────────────────────────
# Esta única regra substitui todas as regras individuais de flight.o, ipc/*.o, etc.
%.o: %.c
	gcc $(WFLAGS) -c $< -o $@

# ─────────────────────────────
# Sub-make (Regras para subdiretórios externos)
# ─────────────────────────────

$(CJSON_DIR)/cJSON.o:
	$(MAKE) -C $(CJSON_DIR) cJSON.o

# Envia qualquer pedido de .o na pasta utils para o sub-Makefile
$(UTILS_DIR)/%.o:
	$(MAKE) -C $(UTILS_DIR) $*.o XML_CFLAGS="$(XML_CFLAGS)"

# ─────────────────────────────
# Clean & Run
# ─────────────────────────────

clean:
	$(MAKE) -C $(CJSON_DIR) clean
	$(MAKE) -C $(UTILS_DIR) clean
	rm -f *.o ipc/*.o physics/*.o math_utils/*.o parent_threading/*.o server/*.o
	rm -f flight_simulator

run: flight_simulator
	./flight_simulator