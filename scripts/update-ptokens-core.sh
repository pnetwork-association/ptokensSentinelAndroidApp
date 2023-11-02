#!/bin/bash

cd "$(dirname -- $0)"

echo updating ptokens-core
cd ../rust/ptokens-core

echo stashing any staged work...
git stash

echo checking out sentinel branch...
git checkout sentinel

echo maybe deleting 'sentinel-prev' branch...
git branch -D sentinel-prev || true

echo renaming current 'sentinel' branch to 'sentinel-prev'...
git branch -m sentinel-prev

echo checking out master...
git checkout master

echo fetching...
git fetch

echo checking out sentinel branch...
git checkout sentinel

echo done
