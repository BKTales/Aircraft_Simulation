#!/usr/bin/env bash
# Expect: OUT OF FUEL + collision; simulator aborts (failure counter >= 3).
set -uo pipefail
TXT="${1:?}"; CSV="${2:?}"
fail=0
grep -q ',OUT OF FUEL,'   "$CSV" || { echo "    expected OUT OF FUEL row in CSV"; fail=1; }
grep -q ',COLLISION,'     "$CSV" || { echo "    expected COLLISION row in CSV"; fail=1; }
grep -q ',ABORTED,'       "$CSV" || { echo "    expected ABORTED row in CSV"; fail=1; }
fc=$(awk -F, '$1=="simulator_failure_count"{print $2}' "$CSV")
[[ -n "$fc" && "$fc" -ge 3 ]] || { echo "    expected simulator_failure_count >= 3 (got ${fc:-none})"; fail=1; }
exit $fail
