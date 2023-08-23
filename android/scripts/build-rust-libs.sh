#!/bin/bash

cd "$(dirname -- $0)"

cd "../../rust/ptokens-sentinel-core"

cargo build --target=aarch64-linux-android

cd -
