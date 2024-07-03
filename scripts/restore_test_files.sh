#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

git checkout HEAD ../src/main/resources/application.yml
rm -rf ../test-files/*
git checkout HEAD ../test-files
