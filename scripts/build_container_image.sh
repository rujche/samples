#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

source ./variables.sh

# Update application.yml before build image
git checkout HEAD ../src/main/resources/application.yml
file="../src/main/resources/application.yml"
sed -i \
  -e "s/\${EVENT_HUBS_NAMESPACE}/${eventhubs_namespace}/" \
  -e "s/\${EVENT_HUB_NAME}/${eventhub}/" \
  "${file}"
sed -i -e "s/test-files/${mount_path}/" "${file}"

az acr build \
  --registry "${container_registry}" \
  --image "${container_image}" \
  ..