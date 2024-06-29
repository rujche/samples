#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

main() {
  echo "main started."
  subscription=$1
  location=$2
  resource_name_prefix=$3
  resource_name_suffix=$4
  resource_group="${resource_name_prefix}rg${resource_name_suffix}"
  storage_account="${resource_name_prefix}sa${resource_name_suffix}"
  eventhub_namespace="${resource_name_prefix}ehn${resource_name_suffix}"
  eventhub="${resource_name_prefix}eh${resource_name_suffix}"
  environment="${resource_name_prefix}env${resource_name_suffix}"
  container_app="${resource_name_prefix}ca${resource_name_suffix}"

  # prepare_azure_cli_environment
  # create_resource_group "${location}" "${resource_group}"
  # create_storage_account "${subscription}" "${location}" "${resource_group}" "${storage_account}"
  # create_eventhub "${subscription}" "${location}" "${resource_group}" "${eventhub_namespace}" "${eventhub}"
  assign_event_hub_data_owner_and_storage_blob_data_owner_to_current_user "${subscription}" "${resource_group}"
  # build_and_deploy_container_app "${subscription}" "${location}" "${resource_group}" "${environment}" "${container_app}"
  echo "main ended."
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
  location=$1
  resource_group_name=$2
  az group create \
    --name "${resource_group_name}" \
    --location "${location}"
  echo "create_resource_group ended."
}

create_storage_account() {
  echo "create_storage_account started."
  subscription=$1
  location=$2
  resource_group=$3
  storage_account=$4
  az storage account create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${storage_account}" \
    --sku Standard_RAGRS \
    --kind StorageV2 \
    --min-tls-version TLS1_2 \
    --allow-blob-public-access false
  echo "create_storage_account ended."
}

create_eventhub() {
  echo "create_eventhub started."
  subscription=$1
  location=$2
  resource_group=$3
  eventhub_namespace=$4
  eventhub=$5
  az eventhubs namespace create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${eventhub_namespace}"
  az eventhubs eventhub create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --namespace-name "${eventhub_namespace}" \
    --name "${eventhub}"
}

build_and_deploy_container_app() {
  echo "build_and_deploy_container_app started."
  subscription=$1
  location=$2
  resource_group=$3
  environment=$4
  container_app=$5
  az containerapp up \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --environment "${environment}" \
    --name "${container_app}" \
    --source ..
  echo "build_and_deploy_container_app ended."
}

assign_event_hub_data_owner_and_storage_blob_data_owner_to_current_user() {
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
}

main "6c933f90-8115-4392-90f2-7077c9fa5dbd" "centralus" "rujche" "24062903"