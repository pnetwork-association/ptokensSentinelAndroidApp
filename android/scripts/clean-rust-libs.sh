#!/bin/bash

cd "$(dirname -- $0)"

readarray -d '' paths < <(find ../ -name "libptokens_sentinel_core.so")

for path in "${paths[@]}"
do
   rm $path
done
