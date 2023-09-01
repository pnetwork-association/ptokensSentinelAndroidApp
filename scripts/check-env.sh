#!/bin/bash

echo "check environment variables..."

requiredEnvVars=("NDK_HOME")

showReadmeHelp=false

for envVar in "${requiredEnvVars[@]}"
do
   if [ ! -v $envVar ]; then
      echo "'$envVar' environment variable isn't set - please set it and try again"
      showReadmeHelp=true
   fi
done

if [ $showReadmeHelp = true ]; then
   echo "see 'rust/ptokens-core-private/v3_bridges/evm/sentinel-strongbox/README.md' for more help"
   exit 1
fi

echo "all environment variables appear correctly set"
