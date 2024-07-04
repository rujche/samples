# DevOps Experience of Azure Container App Job

## 1. Purpose

This sample's purpose is demonstrating the devops experience of Azure Container App Job.

## 2. System diagram

> ![system-diagram](./pictures/system-diagram.png)

In this diagram:
1. The application run in Azure Container App Job.
2. The application's main task is to handle files.
3. The files needed to be handle is stored in Azure File Share.
4. After files been handled, they fill be moved into another folder.
5. For each line of text file, it will be converted and send to Azure Event Hub.

## 3. How to run this application

### 3.1. Prepare necessary Azure resources.

Use this script to create all necessary Azure resources:

```shell
./scripts/create_azure_resources.sh
```

### 3.2. Run this application in localhost

1. Use this script to prepare test files:
    ```shell
    ./scripts/restore_test_files.sh
    ```
2. Update `application.yml` by this script:
    ```shell
    ./scripts/update_application_yml_for_local_development.sh
    ```
3. Run this application by IDE. This is just a simple Spring Boot application. You should know how to run it in you IDE.

### 3.3. Run this application in Azure Container App Jobs

1. Build docker image and upload to Azure Container Registry:
    ```shell
    ./scripts/build_docker_image.sh
    ```
2. Just click `Run now` to trigger job execution.

> ![run-now](./pictures/run-now.png)

## 4. More information

For more information, please refer to related document:

1. [Azure Container App Job](https://learn.microsoft.com/en-us/azure/container-apps/jobs?tabs=azure-cli).
2. [Azure Container App Job Get Started](https://learn.microsoft.com/en-us/azure/container-apps/jobs-get-started-portal).
3. [Azure File Share](https://learn.microsoft.com/en-us/azure/storage/files/storage-files-introduction).
4. [Azure Event Hub](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-about).
5. [Azure Container Registry](https://learn.microsoft.com/en-us/azure/container-registry/).
