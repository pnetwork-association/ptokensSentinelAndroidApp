#!/bin/bash

cd "$(dirname -- $0)"

./clean-android-build-dirs.sh  .
./clean-rust-lib.sh
cd ../android
./gradlew clean

echo cleaning complete
