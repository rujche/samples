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
  file_share="${resource_name_prefix}fs${resource_name_suffix}"
  eventhubs_namespace="${resource_name_prefix}ehn${resource_name_suffix}"
  eventhub="${resource_name_prefix}eh${resource_name_suffix}"
  environment="${resource_name_prefix}env${resource_name_suffix}"
  container_app="${resource_name_prefix}ca${resource_name_suffix}"
  storage_mount="${resource_name_prefix}sm${resource_name_suffix}"

  # prepare_azure_cli_environment
  # create_resource_group "${subscription}" "${resource_group}" "${location}"
  # create_storage_account_and_file_share "${subscription}" "${resource_group}" "${location}" "${storage_account}" "${file_share}"
  # create_eventhub "${subscription}" "${resource_group}" "${location}" "${eventhubs_namespace}" "${eventhub}"
  # create_container_apps_environment "${subscription}" "${resource_group}" "${location}" "${environment}"
  # create_container_app "${subscription}" "${resource_group}" "${environment}" "${container_app}"
  # link_file_share_to_container_apps_environment "${subscription}" "${resource_group}" "${environment}" "${storage_account}" "${file_share}" "${storage_mount}"
  mount_file_share_to_container_apps "${subscription}" "${resource_group}" "${container_app}" "${storage_mount}"
  # assign_roles_to_current_user "${subscription}" "${resource_group}"
  # update_application_yml "${eventhubs_namespace}" "${eventhub}"
  # upload_test_files_to_file_share "${storage_account}" "${file_share}" "../test-files/var/log/system-a" "var/log/system-a"
  # build_and_deploy_container_app "${subscription}" "${resource_group}" "${location}" "${environment}" "${container_app}"
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
  subscription=$1
  resource_group_name=$2
  location=$3
  az group create \
    --name "${resource_group_name}" \
    --location "${location}"
  echo "create_resource_group ended."
}

create_storage_account_and_file_share() {
  echo "create_storage_account started."
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
  echo "create_storage_account ended."
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
    --name "${container_app}" \
    --image nginx \
    --min-replicas 1 \
    --max-replicas 1 \
    --target-port 80 \
    --ingress external \
    --query properties.configuration.ingress.fqdn
  echo "create_container_app ended."
}

link_file_share_to_container_apps_environment() {
  echo "link_file_share_to_container_apps_environment started."
  subscription=$1
  resource_group=$2
  environment=$3
  storage_account=$4
  file_share=$5
  storage_mount=$6
  storage_account_key="$(az storage account keys list -n "${storage_account}" --query "[0].value" -o tsv)"
  az containerapp env storage set \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${environment}" \
    --azure-file-account-name "${storage_account}" \
    --azure-file-share-name "${file_share}" \
    --azure-file-account-key "${storage_account_key}" \
    --storage-name "${storage_mount}" \
    --access-mode ReadWrite \
    --output table
  echo "link_file_share_to_container_apps_environment ended."
}

mount_file_share_to_container_apps() {
  echo "mount_file_share_to_container_apps started."
  subscription=$1
  resource_group=$2
  container_app=$3
  storage_mount=$4
  volume_name="volume${storage_mount}"
  rm mount_file_share_to_container_apps_*.yml || true
  get_container_app_configuration "${subscription}" "${resource_group}" "${container_app}" > mount_file_share_to_container_apps_1.yml
  sed -e "s/^    volumes: null$/    volumes:\n    - name: ${volume_name}\n      storageName: ${storage_mount}\n      storageType: AzureFile/g" \
    -e "s/^      name: ${container_app}$/      name: ${container_app}\n      volumeMounts:\n      - volumeName: ${volume_name}\n        mountPath: \/var\/log\/system-a/g" \
    mount_file_share_to_container_apps_1.yml \
    > mount_file_share_to_container_apps_2.yml
  az containerapp update \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --name "${container_app}" \
    --yaml mount_file_share_to_container_apps_2.yml \
    --output table
  rm mount_file_share_to_container_apps_*.yml || true # Uncomment this line when debug
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
      --account-name "${storage_account}" \
      --destination  "${file_share}" \
      --source "${source}" \
      --destination-path "${destination_path}"
  echo "upload_test_files_to_file_share ended."
}

update_application_yml() {
  echo "update_application_yml started."
  eventhubs_namespace=$1
  eventhub=$2
  file="../src/main/resources/application.yml"
  sed -i "s/\${EVENT_HUBS_NAMESPACE}/${eventhubs_namespace}/" "${file}"
  sed -i "s/\${EVENT_HUB_NAME}/${eventhub}/" "${file}"
  echo "update_application_yml ended."
}

build_and_deploy_container_app() {
  echo "build_and_deploy_container_app started."
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
  echo "build_and_deploy_container_app ended."
}

main "6c933f90-8115-4392-90f2-7077c9fa5dbd" "centralus" "rujche" "24062903"