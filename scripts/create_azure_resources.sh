#!/usr/bin/env bash
set -eux
cd "$(dirname "$0")"

main() {
  echo "main started."
  start_time=$(date +%s)

  source ./variables.sh

  # Prepare Azure CLI environment only needed for the first time.
  prepare_azure_cli_environment "${tenant}"

  create_resource_group "${subscription}" "${resource_group}" "${location}"
  create_eventhubs_namespace "${subscription}" "${resource_group}" "${location}" "${eventhubs_namespace}"
  create_eventhub "${subscription}" "${resource_group}" "${eventhubs_namespace}" "${eventhub}"
  create_schema_registry "${subscription}" "${resource_group}" "${eventhubs_namespace}" "${schema_registry}"
  assign_roles_to_current_user "${subscription}" "${resource_group}"

  end_time=$(date +%s)
  consumed_time=$((end_time-start_time))
  echo "main ended. Consumed time = ${consumed_time} seconds."
}

prepare_azure_cli_environment() {
  echo "prepare_azure_cli_environment started."
  tenant=$1
  az login --tenant "${tenant}"
  az upgrade
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

create_eventhubs_namespace() {
  echo "create_eventhubs_namespace started."
  subscription=$1
  resource_group=$2
  location=$3
  eventhubs_namespace=$4
  az eventhubs namespace create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --location "${location}" \
    --name "${eventhubs_namespace}"
  echo "create_eventhubs_namespace ended."
}

create_eventhub() {
  echo "create_eventhub started."
  subscription=$1
  resource_group=$2
  eventhubs_namespace=$3
  eventhub=$4
  az eventhubs eventhub create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --namespace-name "${eventhubs_namespace}" \
    --name "${eventhub}"
  echo "create_eventhub ended."
}

create_schema_registry() {
  echo "create_schema_registry started."
  subscription=$1
  resource_group=$2
  eventhubs_namespace=$3
  schema_registry=$4
  az eventhubs namespace schema-registry create \
    --subscription "${subscription}" \
    --resource-group "${resource_group}" \
    --namespace-name "${eventhubs_namespace}" \
    --name "${schema_registry}" \
    --schema-compatibility Backward \
    --schema-type Avro
  echo "create_schema_registry ended."
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
  echo "assign_roles_to_current_user ended."
}

main
