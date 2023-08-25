#!/bin/bash

cd "$(dirname -- $0)"

readarray -d '' paths < <(find ../ -name "libsentinel-strongbox.so")

for path in "${paths[@]}"
do
   rm $path
done
