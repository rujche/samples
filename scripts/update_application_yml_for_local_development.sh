#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

source ./variables.sh

git checkout HEAD ../src/main/resources/application.yml
sed -i \
  -e "s/\${EVENT_HUBS_NAMESPACE}/${eventhubs_namespace}/" "../src/main/resources/application.yml"
