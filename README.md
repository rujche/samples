# JAX-WS Sample Project

This project demonstrates the usage of JAX-WS (Java API for XML Web Services) using the Metro implementation. The sample includes both a server-side web service and a client application.

## Project Structure

- `src/main/java/com/example/jaxws/CalculatorService.java`: Service interface defining arithmetic operations
- `src/main/java/com/example/jaxws/CalculatorServiceImpl.java`: Implementation of the service interface
- `src/main/java/com/example/jaxws/client/CalculatorClient.java`: Client application to consume the web service
- `src/main/webapp/WEB-INF/web.xml`: Web application deployment descriptor
- `src/main/webapp/WEB-INF/sun-jaxws.xml`: JAX-WS endpoint configuration
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

This will create a WAR file (`jaxws-sample.war`) in the `target` directory.

## Deploying the Service

1. Deploy the WAR file to your Tomcat server (or any Java EE compatible server)
   - Copy the WAR file to the `webapps` directory of your Tomcat installation
   - Or use the Tomcat Maven Plugin with: `mvn tomcat7:run`

2. Start the server if not already running

3. Verify the service is up by accessing:
   - http://localhost:8080/jaxws-sample/

## Running the Client

To run the client application, execute:

```bash
mvn exec:java -Dexec.mainClass="com.example.jaxws.client.CalculatorClient"
```

Make sure the server is running before executing the client.

## Understanding JAX-WS

JAX-WS (Java API for XML Web Services) is a technology for building web services and clients that communicate using XML. JAX-WS is part of the Java EE platform and provides a simplified model for developing web services.

Key components:
- Service Interface: Defines the operations available on the web service
- Service Implementation: Implements the business logic
- Annotations: Used to configure the service (@WebService, @WebMethod, etc.)
- WSDL: Web Service Description Language file generated automatically
- Client API: Used to invoke the remote service methods

## Notes

- This sample uses Metro (the reference implementation of JAX-WS)
- The service follows a document/literal pattern for SOAP binding
- The client uses the standard JAX-WS client API to invoke service methods

## Troubleshooting

- Ensure all required JAR files are in the classpath
- Check that the endpoint URL in the client matches the deployed service URL
- Verify that the service is properly deployed by accessing the WSDL at:
  http://localhost:8080/jaxws-sample/services/CalculatorService?wsdl
