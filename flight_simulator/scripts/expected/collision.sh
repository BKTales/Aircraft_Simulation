#!/usr/bin/env bash
# Expect: CLOSE_PROXIMITY violation, COLLISION status, validation FAIL.
set -uo pipefail
TXT="${1:?}"; CSV="${2:?}"
fail=0
grep -q 'FINAL VALIDATION RESULT      : FAIL' "$TXT" || { echo "    expected FAIL in TXT"; fail=1; }
grep -q '^validation_result,FAIL$'             "$CSV" || { echo "    expected validation_result,FAIL"; fail=1; }
viol=$(awk -F, '/^safety_violation_events,/{print $2}' "$CSV")
[[ "${viol:-0}" -gt 0 ]] || { echo "    expected safety_violation_events > 0 (got ${viol:-0})"; fail=1; }
grep -q 'COLLISION' "$CSV" || { echo "    expected COLLISION row in CSV"; fail=1; }
exit $fail
