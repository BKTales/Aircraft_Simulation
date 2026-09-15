#!/usr/bin/env bash
# Runs the simulator once per environment and validates the produced report.
# Flight plans must be self-contained JSON (embedded Aircraft + DepartureAirport/ArrivalAirport),
# same format as Java FlightPlanJsonExporter. Refresh fixtures: python3 scripts/embed_flight_plan_data.py
# Usage: bash scripts/run_tests.sh [all | env1 env2 ...]
set -u

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
ENV_ROOT="$ROOT_DIR/src/data/environments"
EXPECTED_DIR="$SCRIPT_DIR/expected"
BIN_DIR="$ROOT_DIR/src/main"
BIN="$BIN_DIR/flight_simulator"

C_RED=$'\033[31m'; C_GRN=$'\033[32m'; C_YLW=$'\033[33m'; C_RST=$'\033[0m'

if [[ ! -x "$BIN" ]]; then
    echo "[BUILD] flight_simulator binary not found, building..."
    (cd "$BIN_DIR" && make flight_simulator) || { echo "${C_RED}build failed${C_RST}"; exit 2; }
fi

# Bash 3.2 (macOS default) has no mapfile — use read loop instead.
all_envs=()
while IFS= read -r _env; do
    [[ -n "$_env" ]] && all_envs+=("$_env")
done < <(find "$ENV_ROOT" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort)

if (($# > 0)); then
    if [[ "$1" == "all" ]]; then
        envs=("${all_envs[@]}")
    else
        envs=("$@")
    fi
else
    echo "Available environments:"
    for i in "${!all_envs[@]}"; do
        printf "  %d) %s\n" "$((i+1))" "${all_envs[$i]}"
    done
    printf "  a) all\n"
    printf "Choice (1-%d or a): " "${#all_envs[@]}"
    read -r choice
    if [[ "$choice" == "a" || "$choice" == "A" ]]; then
        envs=("${all_envs[@]}")
    elif [[ "$choice" =~ ^[0-9]+$ ]] && (( choice >= 1 && choice <= ${#all_envs[@]} )); then
        envs=("${all_envs[$((choice-1))]}")
    else
        echo "${C_RED}invalid choice${C_RST}"; exit 2
    fi
fi

pass=0; fail=0; skipped=0
results=()

for env in "${envs[@]}"; do
    plans_dir="$ENV_ROOT/$env/flight_plans"
    if [[ ! -d "$plans_dir" ]]; then
        echo "${C_YLW}[SKIP]${C_RST} $env: no flight_plans dir"
        ((skipped++)); continue
    fi

    echo "============================================================"
    echo "[TEST] $env"
    echo "  flight_plans: $plans_dir"

    log_dir="$ROOT_DIR/reports/tests/$env"
    mkdir -p "$log_dir"
    rm -f "$log_dir/report.txt" "$log_dir/report.csv"

    echo "------------------------------------------------------------"
    echo "[SIM OUTPUT] $env"
    echo "------------------------------------------------------------"
    (
        cd "$BIN_DIR" && \
        FS_NON_INTERACTIVE=1 \
        FS_NO_BROWSER=1 \
        FS_NO_WALL_SLEEP=1 \
        FS_FLIGHT_PLANS_DIR="$plans_dir" \
        FS_REPORTS_DIR="$log_dir" \
        "$BIN"
    )
    rc=$?
    echo "------------------------------------------------------------"
    echo "  exit=$rc"

    txt="$log_dir/report.txt"
    csv="$log_dir/report.csv"

    if [[ ! -f "$txt" || ! -f "$csv" ]]; then
        echo "  ${C_RED}NO REPORT${C_RST}: simulator did not produce report (exit=$rc)"
        results+=("NO REPORT  $env"); ((fail++)); continue
    fi
    echo "  txt=$txt"
    echo "  csv=$csv"

    assertion="$EXPECTED_DIR/$env.sh"
    if [[ -x "$assertion" ]]; then
        if "$assertion" "$txt" "$csv"; then
            echo "  ${C_GRN}PASS${C_RST} (assertion)"
            results+=("PASS  $env"); ((pass++))
        else
            echo "  ${C_RED}FAIL${C_RST} (assertion $env.sh)"
            results+=("FAIL  $env"); ((fail++))
        fi
    else
        echo "  ${C_YLW}OK${C_RST} (no assertion script)"
        results+=("OK    $env"); ((pass++))
    fi
done

echo "============================================================"
printf '%s\n' "${results[@]}"
echo "------------------------------------------------------------"
echo "pass=$pass  fail=$fail  skipped=$skipped"
exit $(( fail > 0 ? 1 : 0 ))
