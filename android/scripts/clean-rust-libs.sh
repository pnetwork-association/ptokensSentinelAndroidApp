#!/bin/bash

cd "$(dirname -- $0)"

find ../ -name "libptokens_sentinel_core.so" | xargs rm
