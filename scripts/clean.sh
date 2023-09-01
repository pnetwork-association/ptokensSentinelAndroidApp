#!/bin/bash

cd "$(dirname -- $0)"

./clean-android-build-dirs.sh  .
./clean-rust-lib.sh

echo cleaning complete
