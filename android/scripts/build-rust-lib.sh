#!/bin/bash

cd "$(dirname -- $0)"

required_env_var="NDK_HOME"

if [ ! -v $required_env_var ]; then
   echo "'$required_env_var' env variable isn't set - please set it and try again"
   echo "see 'rust/ptokens-core-private/v3_bridges/evm/sentinel-strongbox/README.md' for more help"
   exit 1
fi

npm run cleanRustLib

cd "../../rust/ptokens-core-private/v3_bridges/evm/sentinel-strongbox/"

TARGET_CC="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android30-clang" \
TARGET_AR="$NDK_HOME/toolchains/llvm/prebuilt/linux-x86_64/bin/llvm-ar" \
cargo build --target=aarch64-linux-android

cd -
