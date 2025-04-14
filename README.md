# JAX-RPC Sample Project

This project demonstrates the usage of JAX-RPC (Java API for XML-based RPC) using Apache Axis as the implementation framework. The sample includes both a server-side web service and a client application.

## Project Structure

- `src/main/java/com/example/jaxrpc/CalculatorService.java`: Service interface defining arithmetic operations
- `src/main/java/com/example/jaxrpc/CalculatorServiceImpl.java`: Implementation of the service interface
- `src/main/java/com/example/jaxrpc/client/CalculatorClient.java`: Client application to consume the web service
- `src/main/webapp/WEB-INF/web.xml`: Web application deployment descriptor
- `src/main/webapp/WEB-INF/server-config.wsdd`: Axis service deployment descriptor
- `src/main/webapp/index.jsp`: Simple web page showing service information

## Prerequisites

- Java 8 or higher
- Maven 3.6.x or higher
- Apache Tomcat 8.x or 9.x (or any Java EE compatible web server)

## Building the Project

To build the project, run:

```bash
mvn clean package
```

This will create a WAR file (`jaxrpc-sample.war`) in the `target` directory.

## Deploying the Service

1. Deploy the WAR file to your Tomcat server (or any Java EE compatible server)
   - Copy the WAR file to the `webapps` directory of your Tomcat installation
   - Or use the Tomcat Maven Plugin with: `mvn tomcat7:run`

2. Start the server if not already running

3. Verify the service is up by accessing:
   - http://localhost:8080/jaxrpc-sample/

## Running the Client

To run the client application, execute:

```bash
mvn exec:java -Dexec.mainClass="com.example.jaxrpc.client.CalculatorClient"
```

Make sure the server is running before executing the client.

## Understanding JAX-RPC

JAX-RPC (Java API for XML-based RPC) is an older API for creating web services and clients that communicate using XML. It has been largely superseded by JAX-WS, but understanding JAX-RPC is valuable for maintaining legacy systems.

Key components:
- Service Interface: Defines the operations available on the web service
- Service Implementation: Implements the business logic
- WSDD (Web Service Deployment Descriptor): Configures how the service is exposed
- Client API: Used to invoke the remote service methods

## Notes

- JAX-RPC has been deprecated in favor of JAX-WS in newer Java EE versions
- This sample uses Apache Axis 1.4, which is a widely-used implementation of JAX-RPC
- The client uses dynamic invocation to call the service methods

## Troubleshooting

- Ensure all required JAR files are in the classpath
- Check that the endpoint URL in the client matches the deployed service URL
- Verify that the service is properly deployed by accessing the WSDL at:
  http://localhost:8080/jaxrpc-sample/services/CalculatorService?wsdl
