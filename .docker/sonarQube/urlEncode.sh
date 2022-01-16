#!/usr/bin/env bash

#https://stackoverflow.com/questions/296536/how-to-urlencode-data-for-curl-command
 function encode {
 declare -n ret=$2
  local string="${1}"
   local strlen=${#string}
   local encoded=""
   local pos c o
   for (( pos=0 ; pos<strlen ; pos++ )); do
      c=${string:$pos:1}
      case "$c" in
         [-_.~a-zA-Z0-9] ) o="${c}" ;;
         * )               printf -v o '%%%02x' "'$c"
      esac
      encoded+="${o}"
   done
   ret="${encoded}"
   }