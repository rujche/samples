# Use [Azure Schema Registry](https://learn.microsoft.com/en-us/azure/event-hubs/schema-registry-overview) in Spring Boot Application.

This sample is aimed to demonstrate how to use Azure Schema Registry in [Spring Boot](https://spring.io/projects/spring-boot) Application.
The sending and receiving operation is through [Spring Cloud Stream Kafka Binder](https://docs.spring.io/spring-cloud-stream/docs/current/reference/html/spring-cloud-stream-binder-kafka.html).

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

### 1.4. Validate result

1. In Run Console, validate there are logs like this:

   ```text
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:38 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:39 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:40 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:41 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:42 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:43 CST 2024"}
   Consumed message. message = {"name": "Sample message", "description": "Sun Jul 07 22:14:44 CST 2024"}
   ```

2. In Azure Portal, validate corresponding schema has been created.

   > ![schema-created](./pictures/schema-created.png)

## 2. More information
For more information, please read the source code of current project.

## 3. Support

If you have any question about this sample, welcome to [create a GitHub issue](https://github.com/rujche/samples/issues/new).
