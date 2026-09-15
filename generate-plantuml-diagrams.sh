#!/usr/bin/env bash

set -eu

echo "LOG: Generate PlantUML diagrams"

export_format="${EXPORT_FORMAT:-svg}"
extra="-SdefaultFontSize=20"

if [ -n "${PLANTUML_JAR:-}" ] && [ -f "${PLANTUML_JAR}" ]; then
  plantuml_mode="jar"
  plantuml_jar="${PLANTUML_JAR}"
elif [ -f "libs/plantuml-1.2026.2.jar" ]; then
  plantuml_mode="jar"
  plantuml_jar="libs/plantuml-1.2026.2.jar"
elif command -v plantuml >/dev/null 2>&1; then
  plantuml_mode="cmd"
else
  echo "ERROR: PlantUML not found."
  echo " - Option 1: set PLANTUML_JAR to a valid PlantUML jar path"
  echo " - Option 2: place jar at libs/plantuml-1.2026.2.jar"
  echo " - Option 3: install plantuml command in PATH"
  exit 1
fi

find docs -type f -name "*.puml" -print0 | while IFS= read -r -d '' a_file; do
  echo "Processing file: $a_file"
  if [ "${plantuml_mode}" = "jar" ]; then
    # shellcheck disable=SC2086
    java -jar "$plantuml_jar" $extra -t"$export_format" "$a_file"
  else
    # shellcheck disable=SC2086
    plantuml $extra -t"$export_format" "$a_file"
  fi
done

echo "Finished"
