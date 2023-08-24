#!/bin/bash

cd "$(dirname -- $0)"

npm run cleanRustLibs

cd "../../rust/ptokens-sentinel-core"

cargo build --target=aarch64-linux-android

cd -
