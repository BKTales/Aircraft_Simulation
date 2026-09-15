#!/usr/bin/env bash
# US110 integration test: wind from FS_WEATHER_FILE must alter flight trajectory.
#
# Usage: scripts/test_us110_wind.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BIN_DIR="$ROOT_DIR/src/main"
BIN="$BIN_DIR/flight_simulator"
PLAN_SRC="$ROOT_DIR/src/data/environments/mixed_failures/flight_plans/flight_plan_ok.json"
WEATHER_FILE="$ROOT_DIR/src/data/weather/weather_constant_west.json"
FLIGHT_ID=888001
FLIGHT_TICKS=900

C_RED=$'\033[31m'
C_GRN=$'\033[32m'
C_YLW=$'\033[33m'
C_CYN=$'\033[36m'
C_DIM=$'\033[2m'
C_BLD=$'\033[1m'
C_RST=$'\033[0m'

print_header() {
    echo
    echo "${C_BLD}============================================================${C_RST}"
    echo "${C_BLD}$1${C_RST}"
    echo "${C_BLD}============================================================${C_RST}"
}

print_subheader() {
    echo
    echo "${C_CYN}--- $1 ---${C_RST}"
}

print_kv() {
    printf "  %-18s %s\n" "$1:" "$2"
}

print_expect() {
    echo "  ${C_YLW}expect:${C_RST} $1"
}

print_result_ok() {
    echo "  ${C_GRN}result:${C_RST} $1"
}

print_result_fail() {
    echo "  ${C_RED}result:${C_RST} $1"
}

position_delta() {
    local lat1="$1" lon1="$2" lat2="$3" lon2="$4"
    awk -v a="$lat1" -v b="$lon1" -v c="$lat2" -v d="$lon2" '
        BEGIN {
            dlat = c - a
            dlon = d - b
            mid = (a + c) / 2.0
            km = sqrt((dlat * 111.0) * (dlat * 111.0) + (dlon * 111.0 * cos(mid * 3.14159265 / 180.0)) * (dlon * 111.0 * cos(mid * 3.14159265 / 180.0)))
            printf "Δlat=%+.6f°  Δlon=%+.6f°  (~%.2f km)\n", dlat, dlon, km
        }
    '
}

if [[ ! -f "$PLAN_SRC" ]]; then
    echo "${C_RED}missing flight plan: $PLAN_SRC${C_RST}" >&2
    exit 2
fi
if [[ ! -f "$WEATHER_FILE" ]]; then
    echo "${C_RED}missing weather fixture: $WEATHER_FILE${C_RST}" >&2
    exit 2
fi

if [[ ! -x "$BIN" ]]; then
    echo "[BUILD] compiling flight_simulator..."
    (cd "$BIN_DIR" && make flight_simulator)
fi

WORK_ROOT="$(mktemp -d)"
trap 'rm -rf "$WORK_ROOT"' EXIT

PLANS_DIR="$WORK_ROOT/plans"
BASELINE_DIR="$WORK_ROOT/baseline"
WIND_DIR="$WORK_ROOT/wind"
mkdir -p "$PLANS_DIR" "$BASELINE_DIR" "$WIND_DIR"
cp "$PLAN_SRC" "$PLANS_DIR/"

run_sim() {
    local label="$1"
    local reports_dir="$2"
    local weather_file="${3:-}"

    local weather_label="(none — no wind file)"
    if [[ -n "$weather_file" ]]; then
        weather_label="$(basename "$weather_file")"
    fi

    echo "${C_DIM}  run: $label | ticks=$FLIGHT_TICKS | weather=$weather_label${C_RST}"

    local -a env=(
        FS_NON_INTERACTIVE=1
        FS_NO_BROWSER=1
        FS_NO_WALL_SLEEP=1
        FS_FLIGHT_PLANS_DIR="$PLANS_DIR"
        FS_REPORTS_DIR="$reports_dir"
        FLIGHT_TICKS="$FLIGHT_TICKS"
    )
    if [[ -n "$weather_file" ]]; then
        env+=(FS_WEATHER_FILE="$weather_file")
    fi

    (cd "$BIN_DIR" && env "${env[@]}" "$BIN" >/dev/null 2>&1)
}

extract_lat_lon() {
    local csv="$1"
    awk -F, -v id="$FLIGHT_ID" '
        $1 == id && $11 ~ /^-?[0-9]/ && $12 ~ /^-?[0-9]/ {
            printf "%s %s\n", $11, $12
            exit
        }
    ' "$csv"
}

print_header "[US110] Wind integration test"

print_subheader "What this test proves"
echo "  Loading FS_WEATHER_FILE changes the aircraft ground track:"
echo "  ground velocity = true airspeed vector + wind vector."
echo
print_kv "Flight plan" "flight $FLIGHT_ID (LIS→BCN, self-contained JSON)"
print_kv "Weather fixture" "270° / 10 m/s, world-wide bounds"
print_kv "Simulation stop" "tick $FLIGHT_TICKS (before both runs snap to same waypoint)"

print_subheader "Runs"
run_sim "baseline (no wind)" "$BASELINE_DIR"
run_sim "with global wind" "$WIND_DIR" "$WEATHER_FILE"

BASELINE_CSV="$BASELINE_DIR/report.csv"
WIND_CSV="$WIND_DIR/report.csv"

for f in "$BASELINE_CSV" "$WIND_CSV"; do
    if [[ ! -f "$f" ]]; then
        print_result_fail "missing report $f"
        exit 1
    fi
done

read -r BASE_LAT BASE_LON < <(extract_lat_lon "$BASELINE_CSV")
read -r WIND_LAT WIND_LON < <(extract_lat_lon "$WIND_CSV")

if [[ -z "${BASE_LAT:-}" || -z "${WIND_LAT:-}" ]]; then
    print_result_fail "could not parse flight $FLIGHT_ID position from report.csv"
    exit 1
fi

print_subheader "CHECK — wind alters trajectory at tick $FLIGHT_TICKS"
print_expect "wind run position ≠ baseline position"
print_kv "baseline position" "$BASE_LAT, $BASE_LON"
print_kv "wind position" "$WIND_LAT, $WIND_LON"
print_kv "drift (wind vs baseline)" "$(position_delta "$BASE_LAT" "$BASE_LON" "$WIND_LAT" "$WIND_LON")"

if [[ "$BASE_LAT" == "$WIND_LAT" && "$BASE_LON" == "$WIND_LON" ]]; then
    print_result_fail "positions identical — wind had no visible effect"
    echo
    echo "  ${C_DIM}report diff:${C_RST}"
    diff -u "$BASELINE_CSV" "$WIND_CSV" || true
    exit 1
fi

print_result_ok "positions differ — wind is affecting ground track"

print_header "Summary"
echo "  ${C_GRN}PASS${C_RST} — FS_WEATHER_FILE changes reported position at tick $FLIGHT_TICKS"
exit 0
