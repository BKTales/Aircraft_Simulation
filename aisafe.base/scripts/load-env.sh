# Compatibility shim — use scripts/lib/load-env.sh
# shellcheck source=lib/load-env.sh
source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/lib/load-env.sh"
