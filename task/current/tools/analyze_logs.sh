#!/usr/bin/env bash
set -euo pipefail

LOG_PATH="./logs"

for arg in "$@"; do
  case "${arg}" in
    --path=*) LOG_PATH="${arg#*=}" ;;
    -h|--help)
      echo "Usage: $0 --path=./logs"
      exit 0
      ;;
  esac
done

if [ ! -d "${LOG_PATH}" ]; then
  echo "No such directory: ${LOG_PATH}" >&2
  exit 1
fi

echo "== Files in ${LOG_PATH} =="
find "${LOG_PATH}" -maxdepth 1 -type f \( -name '*.log' -o -name '*.err' \) | sort

echo
echo "== Error lines =="
found=0
for file in "${LOG_PATH}"/*.log "${LOG_PATH}"/*.err; do
  [ -e "${file}" ] || continue
  while IFS= read -r line; do
    printf '%s: %s\n' "$(basename "${file}")" "${line}"
    found=1
  done < <(grep -E 'ERROR|Exception|Caused by|FAILED' "${file}" || true)
done
if [ "${found}" -eq 0 ]; then
  echo "no error lines found"
fi

echo
echo "== Summary =="
for file in "${LOG_PATH}"/*.log "${LOG_PATH}"/*.err; do
  [ -e "${file}" ] || continue
  count=$(grep -cE 'ERROR|Exception|FAILED' "${file}" || true)
  printf '%-32s %s error lines\n' "$(basename "${file}")" "${count}"
done
