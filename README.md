# DevOps Experience of Azure Container App Job

## 1. Purpose

This sample's purpose is demonstrating the devops experience of Azure Container App Job.

## 2. System diagram

> ![system-diagram](./pictures/system-diagram.png)

In this diagram:
1. The application run in [Azure Container App Job](https://learn.microsoft.com/en-us/azure/container-apps/jobs?tabs=azure-cli).
2. The application's main task is to handle text files.
3. The text files needed to be handled are stored in [Azure File Share](https://learn.microsoft.com/en-us/azure/storage/files/storage-files-introduction).
4. After files been handled, they fill be moved into another folder.
5. For each line of text file, if it's valid, it will be converted and send to [Azure Event Hub](https://learn.microsoft.com/en-us/azure/event-hubs/event-hubs-about).

## 3. How to run this application

### 3.1. Prepare necessary Azure resources.

Create all necessary Azure resources by this:

   ```shell
   ./scripts/create_azure_resources.sh
   ```

### 3.2. Run this application in localhost

1. Prepare test files by this:
    ```shell
    ./scripts/restore_test_files.sh
    ```
2. Update `application.yml` by this:
    ```shell
    ./scripts/update_application_yml_for_local_development.sh
    ```
3. Run this application by IDE. This sample project is just a simple Spring Boot project, you should know how to run it in your IDE.

### 3.3. Run this application in Azure Container App Jobs

1. Build docker image and upload to [Azure Container Registry](https://learn.microsoft.com/en-us/azure/container-registry/):
    ```shell
    ./scripts/build_docker_image.sh
    ```
2. Just click `Run now` to trigger job execution.

> ![run-now](./pictures/run-now.png)

## 4. Support

If you have any questions, welcome to [crete a GitHub issue](https://github.com/rujche/samples/issues/new).
