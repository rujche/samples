# Use [Azure Schema Registry](https://learn.microsoft.com/en-us/azure/event-hubs/schema-registry-overview) in Spring Boot Application.

This sample is aimed to demonstrate how to use Azure Schema Registry in [Spring Boot](https://spring.io/projects/spring-boot) Application.
The sending and receiving operation is through [spring-integration-kafka](https://docs.spring.io/spring-integration/reference/kafka.html).

## 1. How to run the sample

### 1.1. Create azure resources

1. Update these properties in `scripts/variables.sh`.
    ```shell
    export tenant="xxx.onmicrosoft.com"
    export subscription="xxx"
    export location="centralus"
    export resource_name_prefix="xxx"
    ```

2. Run `scripts/create_azure_resources.sh`
   ```shell
   ./scripts/create_azure_resources.sh
   ```

### 1.2. Update `application.yml`

1. Update `application.yml` for local development.

   ```shell
   ./scripts/update_application_yml_for_local_development.sh
   ```

### 1.3. Start `SampleApplication` in IDE

It's just a simple Spring Boot application. You know how to run it in IDE.

### 1.4. Send message in console

1. In console, there should output like this:
   ```text
   Please input a line to send message. input 'exit' to exit.
   ```
2. Input test message, then press enter.
   ```text
   test message 1
   ```

### 1.5. Validate result

1. In Run Console, validate there are logs like this:

   ```text
   ...
   ...: Sending message by EntryGateway. message = test message 1.
   ...
   ...: logMessageHandler1: Handle message: GenericMessage [payload={"name": "test message 1", "description": "Created in SampleApplication...
   ...
   ...: logMessageHandler2: Handle message: GenericMessage [payload={"name": "test message 1", "description": "Created in SampleApplication...
   ...   
   ```

2. In Azure Portal, validate corresponding schema has been created.

   > ![schema-created](./pictures/schema-created.png)

### 1.6. Stop application

1. Input `exit` to stop application.
   ```text
   exit
   Closing application context, please wait a few seconds...
   ...
   Process finished with exit code 0
   ```

## 2. More information
For more information, please read the source code of current project.

## 3. Support

If you have any question about this sample, welcome to [create a GitHub issue](https://github.com/rujche/samples/issues/new).
