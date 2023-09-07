#!/bin/bash

cd "$(dirname -- $0)"

./check-env.sh

npm run cleanRustLib

cd "./rust/ptokens-core-private/v3_bridges/evm/sentinel-strongbox/"

cargoBuildProfile="release"

if [ -v CARGO_BUILD_PROFILE ]; then
   cargoBuildProfile=$CARGO_BUILD_PROFILE
fi
echo "here: $cargoBuildProfile"

if [ $cargoBuildProfile == "debug" ]
then
   echo "building with debug profile"
   cargoBuildProfile=''
else
   echo "building with release profile"
   cargoBuildProfile='--release'
fi

TARGET_CC="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android30-clang" \
TARGET_AR="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar" \
cargo build \
$cargoBuildProfile \
--target=aarch64-linux-android

cd -
