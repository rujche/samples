#!/usr/bin/env bash

export tenant="basictiertestoutlook.onmicrosoft.com"
export subscription="50328023-df85-46b6-96f5-c4566d7b063c"
export location="centralus"
export resource_name_prefix="rujche24070302"
export mount_path="\/var\/log\/system-a" # Escape to be used in sed.

export resource_group="${resource_name_prefix}rg"
export storage_account="${resource_name_prefix}sa"
export file_share="${resource_name_prefix}fs"
export eventhubs_namespace="${resource_name_prefix}ehn"
export eventhub="${resource_name_prefix}eh"
export storage_name="${resource_name_prefix}sn"
export container_apps_environment="${resource_name_prefix}env"
export container_registry="${resource_name_prefix}cr"
export container_image="${resource_name_prefix}ci"
export container_app_job="${resource_name_prefix}caj"
