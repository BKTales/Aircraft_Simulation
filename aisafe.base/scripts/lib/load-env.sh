# Loads aisafe.base/.env into the shell environment.
# Usage: source "$AISAFE_LIB/load-env.sh" && _aisafe_load_env "$AISAFE_BASE"

_aisafe_load_env() {
  local base_dir="${1:?base dir required}"
  local env_file="${AISAFE_ENV:-$base_dir/.env}"
  if [[ -f "$env_file" ]]; then
    set -a
    # shellcheck disable=SC1090
    source "$env_file"
    set +a
    export AISAFE_ENV="$env_file"
  fi
}
