#!/bin/bash

cd "$(dirname -- $0)"

./check-env.sh

npm run cleanRustLib

cd "../android"

cargoBuildProfile="release"

if [ -v CARGO_BUILD_PROFILE ]; then
   cargoBuildProfile=$CARGO_BUILD_PROFILE
fi

if [ $cargoBuildProfile == "debug" ]
then
   echo "building with rust debug profile"
   cargoBuildProfile=''
else
   echo "building with rust release profile"
   cargoBuildProfile='--release'
fi

./gradlew assembleDebug

cd ..

echo finished building apk in: './android/app/build/outputs/apk/debug/app-debug.apk'
