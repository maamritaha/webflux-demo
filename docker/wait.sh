#!/usr/bin/env bash
function wait_for_command {
  command="${1}"
  timeout="${2:-30}"
  i=1
  until eval "${command}"; do
    ((i++))
    if [ "${i}" -gt "${timeout}" ]; then
      echo "command was never successful, aborting due to ${timeout}s timeout!"
      exit 1
    fi
    sleep 1
  done
}

function wait_for_url {
  url="${1}"
  target_status="${2:-200}"
  attempts_number="${3:-30}"
  wait_time="${4:-3}"
  i=1
  echo "check if ${url} is responding with status ${target_status}"
  while true; do
    echo "status : $(curl --insecure -s -o /dev/null -w ''%{http_code}'' "${url}")"
    ((i++))
    if [ "$(curl --insecure -s -o /dev/null -w ''%{http_code}'' "${url}")" = "${target_status}" ]; then
      curl -v "${url}"
      echo -e "\n"
      return 0
    fi

    if [[ ${i} -ge ${attempts_number} ]]; then
      echo "$url was never responding with status ${target_status}, aborting due to $((wait_time * attempts_number)) s timeout!"
      exit 1
    fi
    sleep "${wait_time}"
  done
}
