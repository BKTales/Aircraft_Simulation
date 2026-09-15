#!/usr/bin/env bash
# US110 spatial wind test:
#   - Child applies wind only when aircraft position is inside weather bounds.
#   - Environment thread still publishes area-level wind to SHM each tick.
#
# Usage: scripts/test_us110_spatial_wind.sh
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
BIN_DIR="$ROOT_DIR/src/main"
BIN="$BIN_DIR/flight_simulator"
PLAN_INSIDE="$ROOT_DIR/src/data/environments/mixed_failures/flight_plans/flight_plan_ok.json"
PLAN_OUTSIDE="$ROOT_DIR/src/data/environments/mixed_failures/flight_plans/flight_plan_outside_wind_box.json"
PLAN_CROSS="$ROOT_DIR/src/data/environments/mixed_failures/flight_plans/flight_plan_cross_two_boxes.json"
WEATHER_LISBOA="$ROOT_DIR/src/data/weather/weather_lisboa_box.json"
WEATHER_GLOBAL="$ROOT_DIR/src/data/weather/weather_constant_west.json"
WEATHER_TWO_BOXES="$ROOT_DIR/src/data/weather/weather_two_boxes.json"
WEATHER_EAST="$ROOT_DIR/src/data/weather/weather_east_box.json"
FLIGHT_INSIDE=888001
FLIGHT_OUTSIDE=888002
FLIGHT_CROSS=888003
TICKS_INSIDE=400
TICKS_OUTSIDE=200
TICKS_CROSS_BOX1=400
TICKS_CROSS_GAP=2000
TICKS_CROSS_BOX2=3200

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
            # rough ground distance (mid-latitude)
            mid = (a + c) / 2.0
            km = sqrt((dlat * 111.0) * (dlat * 111.0) + (dlon * 111.0 * cos(mid * 3.14159265 / 180.0)) * (dlon * 111.0 * cos(mid * 3.14159265 / 180.0)))
            printf "Δlat=%+.6f°  Δlon=%+.6f°  (~%.2f km)\n", dlat, dlon, km
        }
    '
}

if [[ ! -x "$BIN" ]]; then
    echo "[BUILD] compiling flight_simulator..."
    (cd "$BIN_DIR" && make flight_simulator)
fi

WORK_ROOT="$(mktemp -d)"
trap 'rm -rf "$WORK_ROOT"' EXIT

run_sim() {
    local label="$1"
    local plans_dir="$2"
    local reports_dir="$3"
    local weather_file="${4:-}"
    local ticks="$5"

    local weather_label="(none — no wind file)"
    if [[ -n "$weather_file" ]]; then
        weather_label="$(basename "$weather_file")"
    fi

    echo "${C_DIM}  run: $label | ticks=$ticks | weather=$weather_label${C_RST}"

    local -a env=(
        FS_NON_INTERACTIVE=1
        FS_NO_BROWSER=1
        FS_NO_WALL_SLEEP=1
        FS_FLIGHT_PLANS_DIR="$plans_dir"
        FS_REPORTS_DIR="$reports_dir"
        FLIGHT_TICKS="$ticks"
    )
    if [[ -n "$weather_file" ]]; then
        env+=(FS_WEATHER_FILE="$weather_file")
    fi

    (cd "$BIN_DIR" && env "${env[@]}" "$BIN" >/dev/null 2>&1)
}

extract_lat_lon() {
    local csv="$1"
    local flight_id="$2"
    awk -F, -v id="$flight_id" '
        $1 == id && $11 ~ /^-?[0-9]/ && $12 ~ /^-?[0-9]/ {
            printf "%s %s\n", $11, $12
            exit
        }
    ' "$csv"
}

positions_equal() {
    local a_lat="$1" a_lon="$2" b_lat="$3" b_lon="$4"
    awk -v a="$a_lat" -v b="$a_lon" -v c="$b_lat" -v d="$b_lon" \
        'BEGIN { exit !(a == c && b == d) }'
}

print_header "[US110] Spatial wind test"

print_subheader "What this test proves"
echo "  The simulator applies area wind only when the aircraft is inside the"
echo "  weather record bounds. Outside the box, localized wind is ignored;"
echo "  global wind (world-wide bounds) still affects the trajectory."
echo "  A flight crossing two boxes picks up each zone's wind in sequence."
echo
print_kv "Lisboa wind box" "38.0–39.5°N, -9.8–-8.0°E | 270° / 15 m/s (west)"
print_kv "East Spain box" "39.8–41.5°N, -4.0–1.0°E | 0° / 20 m/s (north)"
print_kv "Global wind" "world bounds | 270° / 10 m/s"
print_kv "Inside flight" "$FLIGHT_INSIDE (LIS→BCN) @ tick $TICKS_INSIDE"
print_kv "Outside flight" "$FLIGHT_OUTSIDE (MAD 41°N,2°E → east) @ tick $TICKS_OUTSIDE"
print_kv "Cross flight" "$FLIGHT_CROSS (38.5°N,-9°E → 40.5°N,0°E)"
echo "  ${C_DIM}(outside tick stops before waypoint snap so drift stays visible)${C_RST}"

CHECKS_PASSED=0
CHECKS_TOTAL=6

# ------------------------------------------------------------------ CHECK 1
print_subheader "CHECK 1 / $CHECKS_TOTAL — wind inside Lisboa box changes trajectory"

PLANS_INSIDE="$WORK_ROOT/plans_inside"
mkdir -p "$PLANS_INSIDE"
cp "$PLAN_INSIDE" "$PLANS_INSIDE/"

BASE_IN="$WORK_ROOT/baseline_inside"
LIS_IN="$WORK_ROOT/lisboa_inside"
run_sim "baseline (no wind)" "$PLANS_INSIDE" "$BASE_IN" "" "$TICKS_INSIDE"
run_sim "lisboa box wind" "$PLANS_INSIDE" "$LIS_IN" "$WEATHER_LISBOA" "$TICKS_INSIDE"

read -r BASE_I_LAT BASE_I_LON < <(extract_lat_lon "$BASE_IN/report.csv" "$FLIGHT_INSIDE")
read -r LIS_I_LAT LIS_I_LON < <(extract_lat_lon "$LIS_IN/report.csv" "$FLIGHT_INSIDE")

print_expect "aircraft inside bounds → lisboa wind run ≠ baseline"
print_kv "baseline position" "$BASE_I_LAT, $BASE_I_LON"
print_kv "lisboa position" "$LIS_I_LAT, $LIS_I_LON"
print_kv "drift (lisboa vs baseline)" "$(position_delta "$BASE_I_LAT" "$BASE_I_LON" "$LIS_I_LAT" "$LIS_I_LON")"

if positions_equal "$BASE_I_LAT" "$BASE_I_LON" "$LIS_I_LAT" "$LIS_I_LON"; then
    print_result_fail "positions identical — localized wind had no effect"
    exit 1
fi
print_result_ok "positions differ — localized wind is active inside the box"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ CHECK 2
print_subheader "CHECK 2 / $CHECKS_TOTAL — outside Lisboa box, localized wind is ignored"

PLANS_OUT="$WORK_ROOT/plans_outside"
mkdir -p "$PLANS_OUT"
cp "$PLAN_OUTSIDE" "$PLANS_OUT/"

BASE_OUT="$WORK_ROOT/baseline_outside"
LIS_OUT="$WORK_ROOT/lisboa_outside"
GLO_OUT="$WORK_ROOT/global_outside"
run_sim "baseline (no wind)" "$PLANS_OUT" "$BASE_OUT" "" "$TICKS_OUTSIDE"
run_sim "lisboa box wind" "$PLANS_OUT" "$LIS_OUT" "$WEATHER_LISBOA" "$TICKS_OUTSIDE"
run_sim "global wind (control)" "$PLANS_OUT" "$GLO_OUT" "$WEATHER_GLOBAL" "$TICKS_OUTSIDE"

read -r BASE_O_LAT BASE_O_LON < <(extract_lat_lon "$BASE_OUT/report.csv" "$FLIGHT_OUTSIDE")
read -r LIS_O_LAT LIS_O_LON < <(extract_lat_lon "$LIS_OUT/report.csv" "$FLIGHT_OUTSIDE")
read -r GLO_O_LAT GLO_O_LON < <(extract_lat_lon "$GLO_OUT/report.csv" "$FLIGHT_OUTSIDE")

print_expect "aircraft outside Lisboa bounds → lisboa wind run = baseline"
print_kv "departure" "41.0°N, 2.0°E (outside Lisboa box)"
print_kv "baseline position" "$BASE_O_LAT, $BASE_O_LON"
print_kv "lisboa position" "$LIS_O_LAT, $LIS_O_LON"
print_kv "drift (lisboa vs baseline)" "$(position_delta "$BASE_O_LAT" "$BASE_O_LON" "$LIS_O_LAT" "$LIS_O_LON")"

if ! positions_equal "$BASE_O_LAT" "$BASE_O_LON" "$LIS_O_LAT" "$LIS_O_LON"; then
    print_result_fail "positions differ — localized wind leaked outside bounds"
    exit 1
fi
print_result_ok "positions match — localized wind correctly suppressed outside box"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ CHECK 3
print_subheader "CHECK 3 / $CHECKS_TOTAL — outside box, global wind still applies (control)"

print_expect "same outside flight + global wind → position ≠ baseline"
print_kv "baseline position" "$BASE_O_LAT, $BASE_O_LON"
print_kv "global position" "$GLO_O_LAT, $GLO_O_LON"
print_kv "drift (global vs baseline)" "$(position_delta "$BASE_O_LAT" "$BASE_O_LON" "$GLO_O_LAT" "$GLO_O_LON")"

if positions_equal "$BASE_O_LAT" "$BASE_O_LON" "$GLO_O_LAT" "$GLO_O_LON"; then
    print_result_fail "positions identical — global wind had no effect (sanity check failed)"
    exit 1
fi
print_result_ok "positions differ — global wind still works outside localized box"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ CHECK 4
print_subheader "CHECK 4 / $CHECKS_TOTAL — in box 1, two-box file matches lisboa-only"

PLANS_CROSS="$WORK_ROOT/plans_cross"
mkdir -p "$PLANS_CROSS"
cp "$PLAN_CROSS" "$PLANS_CROSS/"

CROSS_BASE_B1="$WORK_ROOT/cross_base_b1"
CROSS_TWO_B1="$WORK_ROOT/cross_two_b1"
CROSS_LIS_B1="$WORK_ROOT/cross_lis_b1"
run_sim "baseline (no wind)" "$PLANS_CROSS" "$CROSS_BASE_B1" "" "$TICKS_CROSS_BOX1"
run_sim "two wind boxes" "$PLANS_CROSS" "$CROSS_TWO_B1" "$WEATHER_TWO_BOXES" "$TICKS_CROSS_BOX1"
run_sim "lisboa box only" "$PLANS_CROSS" "$CROSS_LIS_B1" "$WEATHER_LISBOA" "$TICKS_CROSS_BOX1"

read -r CROSS_BASE_B1_LAT CROSS_BASE_B1_LON < <(extract_lat_lon "$CROSS_BASE_B1/report.csv" "$FLIGHT_CROSS")
read -r CROSS_TWO_B1_LAT CROSS_TWO_B1_LON < <(extract_lat_lon "$CROSS_TWO_B1/report.csv" "$FLIGHT_CROSS")
read -r CROSS_LIS_B1_LAT CROSS_LIS_B1_LON < <(extract_lat_lon "$CROSS_LIS_B1/report.csv" "$FLIGHT_CROSS")

print_expect "tick $TICKS_CROSS_BOX1 inside Lisboa box → two_boxes = lisboa_only ≠ baseline"
print_kv "baseline position" "$CROSS_BASE_B1_LAT, $CROSS_BASE_B1_LON"
print_kv "two-box position" "$CROSS_TWO_B1_LAT, $CROSS_TWO_B1_LON"
print_kv "lisboa-only position" "$CROSS_LIS_B1_LAT, $CROSS_LIS_B1_LON"
print_kv "drift (two vs baseline)" "$(position_delta "$CROSS_BASE_B1_LAT" "$CROSS_BASE_B1_LON" "$CROSS_TWO_B1_LAT" "$CROSS_TWO_B1_LON")"

if ! positions_equal "$CROSS_TWO_B1_LAT" "$CROSS_TWO_B1_LON" "$CROSS_LIS_B1_LAT" "$CROSS_LIS_B1_LON"; then
    print_result_fail "two-box and lisboa-only differ in box 1 — first record not applied consistently"
    exit 1
fi
if positions_equal "$CROSS_TWO_B1_LAT" "$CROSS_TWO_B1_LON" "$CROSS_BASE_B1_LAT" "$CROSS_BASE_B1_LON"; then
    print_result_fail "wind in box 1 had no effect"
    exit 1
fi
print_result_ok "box 1 wind active; box 2 not reached yet"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ CHECK 5
print_subheader "CHECK 5 / $CHECKS_TOTAL — in gap between boxes, no localized wind"

CROSS_BASE_GAP="$WORK_ROOT/cross_base_gap"
CROSS_EAST_GAP="$WORK_ROOT/cross_east_gap"
run_sim "baseline (no wind)" "$PLANS_CROSS" "$CROSS_BASE_GAP" "" "$TICKS_CROSS_GAP"
run_sim "east box only" "$PLANS_CROSS" "$CROSS_EAST_GAP" "$WEATHER_EAST" "$TICKS_CROSS_GAP"

read -r CROSS_BASE_GAP_LAT CROSS_BASE_GAP_LON < <(extract_lat_lon "$CROSS_BASE_GAP/report.csv" "$FLIGHT_CROSS")
read -r CROSS_EAST_GAP_LAT CROSS_EAST_GAP_LON < <(extract_lat_lon "$CROSS_EAST_GAP/report.csv" "$FLIGHT_CROSS")

print_expect "tick $TICKS_CROSS_GAP between boxes → east-only = baseline (no wind applied)"
print_kv "zone" "~39.5°N, -5°E (outside both boxes)"
print_kv "baseline position" "$CROSS_BASE_GAP_LAT, $CROSS_BASE_GAP_LON"
print_kv "east-only position" "$CROSS_EAST_GAP_LAT, $CROSS_EAST_GAP_LON"
print_kv "drift (east vs baseline)" "$(position_delta "$CROSS_BASE_GAP_LAT" "$CROSS_BASE_GAP_LON" "$CROSS_EAST_GAP_LAT" "$CROSS_EAST_GAP_LON")"

if ! positions_equal "$CROSS_EAST_GAP_LAT" "$CROSS_EAST_GAP_LON" "$CROSS_BASE_GAP_LAT" "$CROSS_BASE_GAP_LON"; then
    print_result_fail "wind applied in gap between boxes"
    exit 1
fi
print_result_ok "no wind in neutral corridor"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ CHECK 6
print_subheader "CHECK 6 / $CHECKS_TOTAL — in box 2, second wind zone changes trajectory"

CROSS_TWO_B2="$WORK_ROOT/cross_two_b2"
CROSS_LIS_B2="$WORK_ROOT/cross_lis_b2"
run_sim "two wind boxes" "$PLANS_CROSS" "$CROSS_TWO_B2" "$WEATHER_TWO_BOXES" "$TICKS_CROSS_BOX2"
run_sim "lisboa box only" "$PLANS_CROSS" "$CROSS_LIS_B2" "$WEATHER_LISBOA" "$TICKS_CROSS_BOX2"

read -r CROSS_TWO_B2_LAT CROSS_TWO_B2_LON < <(extract_lat_lon "$CROSS_TWO_B2/report.csv" "$FLIGHT_CROSS")
read -r CROSS_LIS_B2_LAT CROSS_LIS_B2_LON < <(extract_lat_lon "$CROSS_LIS_B2/report.csv" "$FLIGHT_CROSS")

print_expect "tick $TICKS_CROSS_BOX2 inside east box → two_boxes ≠ lisboa_only"
print_kv "zone" "~40.0°N, -2.5°E (inside east Spain box)"
print_kv "two-box position" "$CROSS_TWO_B2_LAT, $CROSS_TWO_B2_LON"
print_kv "lisboa-only position" "$CROSS_LIS_B2_LAT, $CROSS_LIS_B2_LON"
print_kv "drift (two vs lisboa)" "$(position_delta "$CROSS_LIS_B2_LAT" "$CROSS_LIS_B2_LON" "$CROSS_TWO_B2_LAT" "$CROSS_TWO_B2_LON")"

if positions_equal "$CROSS_TWO_B2_LAT" "$CROSS_TWO_B2_LON" "$CROSS_LIS_B2_LAT" "$CROSS_LIS_B2_LON"; then
    print_result_fail "second wind box had no effect — trajectories still identical"
    exit 1
fi
print_result_ok "second wind box (north wind) diverges from lisboa-only path"
CHECKS_PASSED=$((CHECKS_PASSED + 1))

# ------------------------------------------------------------------ SUMMARY
print_header "Summary"
echo "  ${C_GRN}PASS${C_RST} — $CHECKS_PASSED/$CHECKS_TOTAL checks passed"
echo
echo "  Interpretation:"
echo "    • Inside Lisboa box  → area wind drifts the aircraft"
echo "    • Outside Lisboa box → area wind ignored (zero wind from bounds lookup)"
echo "    • Global wind file   → always drifts, regardless of position"
echo "    • Two-box crossing   → box 1 wind, then calm gap, then box 2 wind"
exit 0
