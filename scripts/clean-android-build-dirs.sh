#!/bin/bash

cd "$(dirname -- $0)"

echo "cleaning android build dirs..."

paths=(
   "../android/build"
   "../android/app/build"
)

for path in "${paths[@]}"
do
   if [ -d "$path" ];
   then
      rm -r $path
      echo "'$path' path cleaned"
   fi
done

echo "android build dirs cleaned"
