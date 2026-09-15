#!/usr/bin/env bash
# Expect: at least one flight ends with OUT OF FUEL status.
set -uo pipefail
TXT="${1:?}"; CSV="${2:?}"
fail=0
grep -q 'OUT OF FUEL' "$TXT" || { echo "    expected OUT OF FUEL in TXT"; fail=1; }
grep -q ',OUT OF FUEL,' "$CSV" || { echo "    expected OUT OF FUEL row in CSV"; fail=1; }
exit $fail
