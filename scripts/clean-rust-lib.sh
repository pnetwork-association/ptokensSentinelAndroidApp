#!/bin/bash

cd "$(dirname -- $0)"

echo "removing old rust libs..."

readarray -d '' paths < <(find ../ -name "libsentinel_strongbox.so")

for path in "${paths[@]}"
do
   echo "removing old lib @ path: $path"
   rm $path
done

echo "remove old rust libs complete"
