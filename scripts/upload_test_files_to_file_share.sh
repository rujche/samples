#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

source ./variables.sh

az storage file upload-batch \
    --subscription "${subscription}" \
    --account-name "${storage_account}" \
    --destination  "${file_share}" \
    --source "../test-files/unprocessed/" \
    --destination-path "unprocessed/" # Note: Using "/unprocessed/2024-07-01/" as destination will upload failed.
