#!/usr/bin/env bash
# Expect: no failures, all flights SUCCESS, PASS validation.
set -uo pipefail
TXT="${1:?}"; CSV="${2:?}"
fail=0
grep -q 'FINAL VALIDATION RESULT      : PASS' "$TXT" || { echo "    expected PASS in TXT"; fail=1; }
grep -q '^validation_result,PASS$'             "$CSV" || { echo "    expected validation_result,PASS"; fail=1; }
grep -q '^safety_violation_events,0$'          "$CSV" || { echo "    expected 0 safety violations"; fail=1; }
grep -q '^execution_failures,0$'               "$CSV" || { echo "    expected 0 execution failures"; fail=1; }
exit $fail
