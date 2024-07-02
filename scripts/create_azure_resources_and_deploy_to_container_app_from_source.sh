#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

main() {
  echo "main started."
  start_time=$(date +%s)
  tenant="basictiertestoutlook.onmicrosoft.com"
  subscription="50328023-df85-46b6-96f5-c4566d7b063c"
  location="centralus"
  resource_name_prefix="rujche24070212"
  mount_path="\/var\/log\/system-a" # Escape to be used in sed.

  resource_group="${resource_name_prefix}rg"
  storage_account="${resource_name_prefix}sa"
  file_share="${resource_name_prefix}fs"
  eventhubs_namespace="${resource_name_prefix}ehn"
  eventhub="${resource_name_prefix}eh"
  environment="${resource_name_prefix}env"
  container_app_job="${resource_name_prefix}caj"
  storage_name="${resource_name_prefix}sn"

#  prepare_azure_cli_environment "${tenant}"
#
#  create_resource_group "${subscription}" "${resource_group}" "${location}"
#  create_storage_account_and_file_share "${subscription}" "${resource_group}" "${location}" "${storage_account}" "${file_share}"
#  create_eventhub "${subscription}" "${resource_group}" "${location}" "${eventhubs_namespace}" "${eventhub}"
#  create_container_apps_environment "${subscription}" "${resource_group}" "${location}" "${environment}"
#  create_container_app_job "${subscription}" "${resource_group}" "${environment}" "${container_app_job}"
#
#  assign_roles_to_current_user "${subscription}" "${resource_group}"
#  assign_system_assigned_managed_identity_to_container_app_job "${subscription}" "${resource_group}" "${container_app_job}"
  assign_roles_to_container_app_job_managed_identity "${subscription}" "${resource_group}" "${container_app_job}"
  upload_test_files_to_file_share "${storage_account}" "${file_share}" "../test-files/unprocessed/" "unprocessed/" # Note: Using "/unprocessed/2024-07-01/" as destination will upload failed.

  link_file_share_to_container_apps_environment "${subscription}" "${resource_group}" "${environment}" "${storage_account}" "${file_share}" "${storage_name}"
  mount_file_share_to_container_app_job "${subscription}" "${resource_group}" "${container_app_job}" "${storage_name}" "${mount_path}"

  restore_application_yml_and_test_files
  update_application_yml_about_event_hub "${eventhubs_namespace}" "${eventhub}"
  update_application_yml_about_log_directory "${mount_path}"
  # deploy_to_container_app_job_by_source "${subscription}" "${resource_group}" "${location}" "${environment}" "${container_app_job}"

  end_time=$(date +%s)
  runtime=$((end_time-start_time))
  echo "main ended. Consumed time = ${runtime} seconds."
}

prepare_azure_cli_environment() {
  echo "prepare_azure_cli_environment started."
  tenant=$1
  az login --tenant "${tenant}"
  az upgrade
  az extension add --name containerapp --upgrade --allow-preview true
#  az provider register --namespace Microsoft.App
#  az provider register --namespace Microsoft.OperationalInsights
  echo "prepare_azure_cli_environment ended."
}

create_resource_group() {
  echo "create_resource_group started."
  subscription=$1
  resource_group_name=$2
  location=$3
  az group create \
    --subscription "${subscription}" \
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

create_container_app_job() {
  echo "create_container_app_job started."
  subscription=$1
  resource_group=$2
  environment=$3
  container_app_job=$4
  az containerapp job create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --environment "${environment}" \
    --trigger-type Manual \
    --name "${container_app_job}" \
    --max-executions 1
  echo "create_container_app_job ended."
}

assign_system_assigned_managed_identity_to_container_app_job() {
  echo "assign_system_assigned_managed_identity_to_container_app_job started."
  az containerapp job identity assign \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app_job}" \
    --system-assigned
  echo "assign_system_assigned_managed_identity_to_container_app_job ended."
}

link_file_share_to_container_apps_environment() {
  echo "link_file_share_to_container_apps_environment started."
  subscription=$1
  resource_group=$2
  environment=$3
  storage_account=$4
  file_share=$5
  storage_name=$6
  storage_account_key="$(az storage account keys list --subscription "${subscription}" --account-name "${storage_account}" --query "[0].value" -o tsv)"
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

mount_file_share_to_container_app_job() {
  echo "mount_file_share_to_container_app_job started."
  subscription=$1
  resource_group=$2
  container_app_job=$3
  storage_name=$4
  mount_path=$5
  rm get_container_app_job_configuration_updated*.yml || true
  get_container_app_job_configuration "${subscription}" "${resource_group}" "${container_app_job}" > get_container_app_job_configuration.yml
  sed -e "s/^    volumes: null$/    volumes:\n    - name: ${storage_name}\n      storageName: ${storage_name}\n      storageType: AzureFile/g" \
    -e "s/^      name: ${container_app_job}$/      name: ${container_app_job}\n      volumeMounts:\n      - volumeName: ${storage_name}\n        mountPath: ${mount_path}/g" \
    get_container_app_job_configuration.yml \
    > get_container_app_job_configuration_updated.yml
  az containerapp job update \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app_job}" \
    --yaml get_container_app_job_configuration_updated.yml \
    --output table
  rm get_container_app_job_configuration*.yml || true # Uncomment this line when debug
  echo "mount_file_share_to_container_app_job ended."
}

get_container_app_job_configuration() {
  subscription=$1
  resource_group=$2
  container_app_job=$3
  az containerapp job show \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app_job}" \
    --output yaml
}

assign_roles_to_current_user() {
  echo "assign_roles_to_current_user started."
  subscription=$1
  resource_group=$2
  assignee="$(az ad signed-in-user show --query userPrincipalName -o tsv)"
  az role assignment create \
    --subscription "${subscription}" \
    --assignee "${assignee}" \
    --role "Azure Event Hubs Data Owner" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}"
  az role assignment create \
    --subscription "${subscription}" \
    --assignee "${assignee}" \
    --role "Storage Blob Data Owner" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}"
  az role assignment create \
    --subscription "${subscription}" \
    --assignee "${assignee}" \
    --role "Storage File Data SMB Share Contributor" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}"
  echo "assign_roles_to_current_user ended."
}

get_container_app_job_principal_id() { # Log should be avoided because its console output will be returned to outer function.
  subscription=$1
  resource_group=$2
  container_app_job=$3
  az containerapp job show \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app_job}" \
    --query "identity.principalId" \
  | sed -e "s/\"//g"
}

assign_roles_to_container_app_job_managed_identity() {
  echo "assign_roles_to_container_app_job_managed_identity started."
  subscription=$1
  resource_group=$2
  container_app_job=$3
  assignee="$(get_container_app_job_principal_id "${subscription}" "${resource_group}" "${container_app_job}")"
  az role assignment create \
    --subscription "${subscription}" \
    --assignee-principal-type ServicePrincipal \
    --assignee-object-id  "${assignee}" \
    --role "Azure Event Hubs Data Owner" \
    --scope "subscriptions/${subscription}/resourceGroups/${resource_group}"
  echo "assign_roles_to_container_app_job_managed_identity ended."
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

restore_application_yml_and_test_files() {
  echo "restore_application_yml_and_test_files started."
    git checkout HEAD ../src/main/resources/application.yml
    rm -rf ../test-files/*
    git checkout HEAD ../test-files
  echo "restore_application_yml_and_test_files ended."
}

update_application_yml_about_event_hub() {
  eventhubs_namespace=$1
  eventhub=$2
  file="../src/main/resources/application.yml"
  sed -i \
    -e "s/\${EVENT_HUBS_NAMESPACE}/${eventhubs_namespace}/" \
    -e "s/\${EVENT_HUB_NAME}/${eventhub}/" \
    "${file}"
}

update_application_yml_about_log_directory() {
  echo "update_application_yml_about_log_directory started."
  mount_path=$1
  file="../src/main/resources/application.yml"
  sed -i -e "s/test-files/${mount_path}/" "${file}"
  echo "update_application_yml_about_log_directory ended."
}

deploy_to_container_app_job_by_source() {
  echo "deploy_to_container_app_job_by_source started."
  subscription=$1
  resource_group=$2
  location=$3
  environment=$4
  container_app_job=$5
  az containerapp up \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --environment "${environment}" \
    --name "${container_app_job}" \
    --source ..
  echo "deploy_to_container_app_job_by_source ended."
}

main