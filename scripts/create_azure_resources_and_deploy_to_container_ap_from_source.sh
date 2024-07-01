#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

main() {
  echo "main started."
  start_time=$(date +%s)
  subscription="6c933f90-8115-4392-90f2-7077c9fa5dbd"
  location="centralus"
  container_apps_location="westus2" # This is used for handling Security Policy used in this subscription: "6c933f90-8115-4392-90f2-7077c9fa5dbd"
  resource_name_prefix="rujche24070102"

  resource_group="${resource_name_prefix}rg"
  storage_account="${resource_name_prefix}sa"
  file_share="${resource_name_prefix}fs"
  eventhubs_namespace="${resource_name_prefix}ehn"
  eventhub="${resource_name_prefix}eh"
  environment="${resource_name_prefix}env"
  container_app="${resource_name_prefix}ca"
  storage_name="${resource_name_prefix}sn"

#  prepare_azure_cli_environment

  create_resource_group "${subscription}" "${resource_group}" "${location}"
  create_storage_account_and_file_share "${subscription}" "${resource_group}" "${location}" "${storage_account}" "${file_share}"
#  create_eventhub "${subscription}" "${resource_group}" "${location}" "${eventhubs_namespace}" "${eventhub}"
  create_container_apps_environment "${subscription}" "${resource_group}" "${container_apps_location}" "${environment}"
  create_container_app "${subscription}" "${resource_group}" "${environment}" "${container_app}"
  assign_roles_to_current_user "${subscription}" "${resource_group}"
  upload_test_files_to_file_share "${storage_account}" "${file_share}" "../test-files/unprocessed/2024-07-01/" "unprocessed/2024-07-01/" # Note: Using "/unprocessed/2024-07-01/" as destination will upload failed.

  add_storage_account_network_role "${subscription}" "${resource_group}" "${container_app}" "${storage_account}"
  link_file_share_to_container_apps_environment "${subscription}" "${resource_group}" "${environment}" "${storage_account}" "${file_share}" "${storage_name}"
  mount_file_share_to_container_apps "${subscription}" "${resource_group}" "${container_app}" "${storage_name}"
#
#  update_application_yml "${eventhubs_namespace}" "${eventhub}"
#  deploy_to_container_app_by_source "${subscription}" "${resource_group}" "${container_apps_location}" "${environment}" "${container_app}"

  end_time=$(date +%s)
  runtime=$((end_time-start_time))
  echo "main ended. Consumed time = ${runtime} seconds."
}

prepare_azure_cli_environment() {
  echo "prepare_azure_cli_environment started."
  az login
  az upgrade
  az extension add --name containerapp --upgrade --allow-preview true
  az provider register --namespace Microsoft.App
  az provider register --namespace Microsoft.OperationalInsights
  echo "prepare_azure_cli_environment ended."
}

create_resource_group() {
  echo "create_resource_group started."
  subscription=$1
  resource_group_name=$2
  location=$3
  az group create \
    --name "${resource_group_name}" \
    --location "${location}"
  echo "create_resource_group ended."
}

create_storage_account_and_file_share() {
  echo "create_storage_account_and_file_share started."
  subscription=$1
  resource_group=$2
  location=$3
  storage_account=$4
  file_share=$5
  az storage account create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${storage_account}" \
    --sku Standard_RAGRS \
    --kind StorageV2 \
    --min-tls-version TLS1_2 \
    --allow-blob-public-access false

  az storage share-rm create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --storage-account "${storage_account}" \
    --name "${file_share}" \
    --quota 1 \
    --enabled-protocols SMB \
    --output none
  echo "create_storage_account_and_file_share ended."
}

create_eventhub() {
  echo "create_eventhub started."
  subscription=$1
  resource_group=$2
  location=$3
  eventhubs_namespace=$4
  eventhub=$5
  az eventhubs namespace create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${eventhubs_namespace}"
  az eventhubs eventhub create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --namespace-name "${eventhubs_namespace}" \
    --name "${eventhub}"
}

create_container_apps_environment() {
  echo "create_container_apps_environment started."
  subscription=$1
  resource_group=$2
  location=$3
  environment=$4
  az containerapp env create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${environment}" \
    --query "properties.provisioningState"
  echo "create_container_apps_environment ended."
}

create_container_app() {
  subscription=$1
  resource_group=$2
  environment=$3
  container_app=$4
  echo "create_container_app started."
  az containerapp create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --environment "${environment}" \
    --name "${container_app}"
  echo "create_container_app ended."
}

link_file_share_to_container_apps_environment() {
  echo "link_file_share_to_container_apps_environment started."
  subscription=$1
  resource_group=$2
  environment=$3
  storage_account=$4
  file_share=$5
  storage_name=$6
  storage_account_key="$(az storage account keys list -n "${storage_account}" --query "[0].value" -o tsv)"
  az containerapp env storage set \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${environment}" \
    --azure-file-account-name "${storage_account}" \
    --azure-file-account-key "${storage_account_key}" \
    --azure-file-share-name "${file_share}" \
    --storage-name "${storage_name}" \
    --access-mode ReadWrite \
    --output table
  echo "link_file_share_to_container_apps_environment ended."
}

mount_file_share_to_container_apps() {
  echo "mount_file_share_to_container_apps started."
  subscription=$1
  resource_group=$2
  container_app=$3
  storage_name=$4
  rm azure_container_app_configuration*.yml || true
  get_container_app_configuration "${subscription}" "${resource_group}" "${container_app}" > azure_container_app_configuration.yml
  sed -e "s/^    volumes: null$/    volumes:\n    - name: ${storage_name}\n      storageName: ${storage_name}\n      storageType: AzureFile/g" \
    -e "s/^      name: ${container_app}$/      name: ${container_app}\n      volumeMounts:\n      - volumeName: ${storage_name}\n        mountPath: \/var\/log\/system-a/g" \
    azure_container_app_configuration.yml \
    > azure_container_app_configuration_updated.yml
  az containerapp update \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app}" \
    --yaml azure_container_app_configuration_updated.yml \
    --output table
  rm azure_container_app_configuration*.yml || true # Uncomment this line when debug
  echo "mount_file_share_to_container_apps ended."
}

get_container_app_configuration() {
  subscription=$1
  resource_group=$2
  container_app=$3
  az containerapp show \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app}" \
    --output yaml
}

get_container_app_outbound_ip_addresses() {
  subscription=$1
  resource_group=$2
  container_app=$3
  az containerapp show \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app}" \
    --query properties.outboundIpAddresses \
  | sed -e ':a;N;$!ba;s/[][ \"\n]//g' -e "s/,/ /g"
}

add_storage_account_network_role() {
  subscription=$1
  resource_group=$2
  container_app=$3
  storage_account=$4
  container_app_outbound_ip_address=$(get_container_app_outbound_ip_addresses "${subscription}" "${resource_group}" "${container_app}")
  # shellcheck disable=SC2086  # container_app_outbound_ip_address will be resolved as multiple parameters
  az storage account network-rule add \
    --resource-group "${resource_group}" \
    --account-name "${storage_account}" \
    --ip-address ${container_app_outbound_ip_address}
}

assign_roles_to_current_user() {
  echo "assign_roles_to_current_user started."
  subscription=$1
  resource_group=$2
  assignee="$(az ad signed-in-user show --query id -o tsv)"
  az role assignment create \
    --subscription "${subscription}" \
    --assignee "${assignee}" \
    --role "Azure Event Hubs Data Owner" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}" \
    || true # Ignore error: RoleAssignmentExists
  az role assignment create \
    --assignee "${assignee}" \
    --subscription "${subscription}" \
    --role "Storage Blob Data Owner" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}" \
    || true # Ignore error: RoleAssignmentExists
  az role assignment create \
    --assignee "${assignee}" \
    --subscription "${subscription}" \
    --role "Storage File Data SMB Share Contributor" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}" \
    || true # Ignore error: RoleAssignmentExists
  echo "assign_roles_to_current_user ended."
}

upload_test_files_to_file_share() {
  echo "upload_test_files_to_file_share started."
  storage_account=$1
  file_share=$2
  source=$3
  destination_path=$4
  az storage file upload-batch \
      --subscription "${subscription}" \
      --account-name "${storage_account}" \
      --destination  "${file_share}" \
      --source "${source}" \
      --destination-path "${destination_path}"
  echo "upload_test_files_to_file_share ended."
}

update_application_yml() {
  echo "uto_pdate_application_yml started."
  eventhubs_namespace=$1
  eventhub=$2
  file="../src/main/resources/application.yml"
  sed -i "s/\${EVENT_HUBS_NAMESPACE}/${eventhubs_namespace}/" "${file}"
  sed -i "s/\${EVENT_HUB_NAME}/${eventhub}/" "${file}"
  echo "update_application_yml ended."
}

deploy_to_container_app_by_source() {
  echo "deploy_to_container_app_by_source started."
  subscription=$1
  resource_group=$2
  location=$3
  environment=$4
  container_app=$5
  az containerapp up \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --environment "${environment}" \
    --name "${container_app}" \
    --source ..
  echo "deploy_to_container_app_by_source ended."
}

main