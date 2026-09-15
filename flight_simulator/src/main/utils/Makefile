FLAGS = -Wall -Wextra -I..

# Se o Makefile principal não passar a variável, usa o pkg-config por segurança
XML_CFLAGS ?= $(shell pkg-config --cflags libxml-2.0)

# Inclui as flags do XML nas flags de compilação globais deste diretório
FLAGS += $(XML_CFLAGS)

# Ficheiros Objeto
OBJS = flight_plan_parser.o \
       flight_plan_reader.o \
       flight_plan_utils.o \
       aircraft_parser.o \
       network_parser.o \
       flight_report.o \
       sim_time_utils.o \
       sim_clock.o \
       process_spawn.o \
       parent_safety.o \
       sim_shutdown.o \
       sim_log.o \
       reports_path.o \
       sim_dedicated_thread.o \
       weather_config.o

all: $(OBJS)

# ─────────────────────────────
# Regra de Compilação Genérica
# ─────────────────────────────
# O símbolo $< representa o ficheiro .c e o $@ representa o ficheiro .o
%.o: %.c
	gcc $(FLAGS) -c $< -o $@

# ─────────────────────────────
# Limpeza
# ─────────────────────────────
clean:
	rm -f $(OBJS)